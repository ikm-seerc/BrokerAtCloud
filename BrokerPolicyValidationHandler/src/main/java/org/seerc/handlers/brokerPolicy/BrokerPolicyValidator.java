package org.seerc.handlers.brokerPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.seerc.brokeratcloud.messagebroker.FusekiClient;
import org.seerc.brokeratcloud.messagebroker.MessageBrokerSubscriber;
import org.seerc.brokeratcloud.messagebroker.OutOfRangeSLAViolationListener;
import org.seerc.brokeratcloud.messagebroker.OutOfRangeSLAViolationSubscriber;
import org.seerc.brokeratcloud.messagebroker.WSO2GREGClient;
import org.seerc.brokeratcloud.messagebroker.WSO2MBClient;
import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicy;
import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyClass;
import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.seerc.brokeratcloud.policycompletenesscompliance.ComplianceException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.seerc.brokeratcloud.policycompletenesscompliance.Subproperty;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

public class BrokerPolicyValidator extends Handler {

	WSO2MBClient mb;
	
	// The Fuseki client
	FusekiClient fc;
	
	WSO2GREGClient wso2gregClient;

	public BrokerPolicyValidator()
	{
		this.mb = new WSO2MBClient();
		this.fc = new FusekiClient();
		this.wso2gregClient = new WSO2GREGClient();
	}
	
	public static void main(String[] args) 
	{
		BrokerPolicyValidator bpv = new BrokerPolicyValidator();
		File initialFile = new File("/media/bob/Data/SEERC/git-seerc/BrokerAtCloud/PolicyCompletenessCompliance/Ontologies/ForReview/CAS-broker-policies.ttl");
	    try {
			InputStream targetStream = new FileInputStream(initialFile);
			PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
			pcc.getBrokerPolicy(targetStream);
			bpv.createMonitoringTopics(pcc.getBP());
		} catch (SecurityException | IllegalArgumentException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | IOException | ComplianceException | CompletenessException e) {
			e.printStackTrace();
		}
	}
	
	public void put(RequestContext requestContext) throws RegistryException{
		InputStream newBpIS = requestContext.getResource().getContentStream();
		PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
		try {
			pcc.validateBrokerPolicy(newBpIS);
			
			// flag to indicate whether this is an update or a creation of a BP
			boolean bpUpdated = false;
			
			InputStream currentBP = null;
			
			if(this.wso2gregClient.getRemote_registry().resourceExists(requestContext.getResourcePath().getPath()))
			{	// BP exists
				currentBP = this.wso2gregClient.getRemote_registry().get(requestContext.getResourcePath().getPath()).getContentStream();

				// this is a service update
				bpUpdated = true;
			}

			// reuse stream
			newBpIS.reset();

			// send to Fuseki
			System.out.println("Evaluation went OK, sending received BP to Fuseki.");
			if(bpUpdated)
			{
				// delete old from Fuseki
				fc.deleteInputStreamFromFuseki(currentBP);
			}
			fc.addInputStreamToFuseki(newBpIS);
			
			System.out.println("Broker Policy went OK. Create Monitoring Topics for Quantitative Values.");
			this.createMonitoringTopics(pcc.getBP());
		} catch (Exception e)
		{
			e.printStackTrace();
			
			throw new RegistryException(e.getMessage(), e);			
		}
		super.put(requestContext);
	}

	private void createMonitoringTopics(BrokerPolicy bp)
	{
		int i=0;
		for(BrokerPolicyClass bpc:bp.getExpressionVariableMap().values())
		{
			for(Subproperty sp:bpc.getPropertyMap().values())
			{
				String candidateQV = sp.getRangeUri();
				// is QV a QuantitativeValue (not Qualitative)
				if(bp.getQuantitativeValueMap().keySet().contains(candidateQV))
				{ // it is QuantitativeValue, create topic
					try {
						String topicName = WSO2MBClient.monitoringTopicPrefix + new URI(candidateQV).getFragment();
						// new topic create it and create new out-of-range subscriber
						WSO2MBClient.createTopic(topicName);
						// also create a OutOfRangeSLAViolationListener for the new QV monitoring topic
						String subscriberName = "subscriberFor_" + topicName;
						final OutOfRangeSLAViolationSubscriber qvSubscriber = new OutOfRangeSLAViolationSubscriber(subscriberName, topicName);
						System.out.println("Created QV out of range subscriber " + subscriberName);
						qvSubscriber.subscribeToTopic();
						// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
						Runtime.getRuntime().addShutdownHook(new Thread() {

						    @Override
						    public void run() {
						    	qvSubscriber.releaseResources();
						    }

						});		

						System.out.println("Created topic " + topicName + ".");
					} catch (URISyntaxException e) {
						System.out.println("QV in Broker Policy did not have a proper URI name. Cannot create topic.");						
						e.printStackTrace();
					}
				}
			}
		}
	}
}