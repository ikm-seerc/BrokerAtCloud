package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.seerc.brokeratcloud.policycompletenesscompliance.ComplianceException;
import org.seerc.brokeratcloud.policycompletenesscompliance.EvaluationReport;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

public abstract class AbstractSDEvaluationListener implements MessageListener {

	// Holds a completeness/compliance evaluator
	WSO2GREGEvaluator gregEvaluator;
	
	// the Gson POJO de/serializer
	Gson gson;
	
	// the evaluation report object
	EvaluationReport ep;
	
	// Holds an SD report publisher
	MessageBrokerStringPublisher mbsp;
	
	WSO2GREGClient wso2gregClient;
	
	// The Fuseki client
	FusekiClient fc;
	
	// The ServiceLifecyclePublisher for triggering events on create and update
	ServiceLifecyclePublisher slp;

	public AbstractSDEvaluationListener()
	{
		try {
			gregEvaluator = new WSO2GREGEvaluator();

			gson = new Gson();
			
			// Create the SD report publisher
			mbsp = new MessageBrokerStringPublisher("EvaluationComponentSDReportPublisher", "SDReport");
			
			this.wso2gregClient = new WSO2GREGClient();
			
			fc = new FusekiClient();
			
			slp = new ServiceLifecyclePublisher();
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	/*
	 * Given an SD's input stream, find the BP (if any) that this SD should be evaluated against.
	 * This BP should have the same URI as the SD's gr:hasMakeAndModel association's target.
	 * Will throw a CompletenessException if it doesn't find any.
	 */
	public InputStream getBpForSd(InputStream sdis) throws IOException, RegistryException, CompletenessException {
		// get the broker policy URI defined in this SD
		String brokerPolicyUri = gregEvaluator.getPcc().getSDIsVariantOfURI(sdis);
		// find BP in GREG that has the same URI 
		InputStream bpIs = this.findBpWithUri(brokerPolicyUri);
		
		return bpIs;
	}

	// for each BP in brokerPoliciesFolder
	// see if its URI is brokerPolicyUri
	private InputStream findBpWithUri(String brokerPolicyUri) throws RegistryException, IOException, CompletenessException 
	{
		// The folder (Collection) where BPs are stored
		Collection brokerPoliciesCollection = (Collection) this.gregEvaluator.getRemote_registry().get(this.gregEvaluator.getBrokerPoliciesFolder());
		// traverse files in that folder
		for(int i=0;i<brokerPoliciesCollection.getChildCount();i++)
		{
			// The path to BP in GReg
			String childPath = brokerPoliciesCollection.getChildren()[i];
			// The BP resource
			Resource child = this.gregEvaluator.getRemote_registry().get(childPath);
			// The URI of this BP
			String bpUri = null;
			try {
				bpUri = gregEvaluator.getPcc().getBPInstanceUri(child.getContentStream());
			} catch (BrokerPolicyException e) {
				e.printStackTrace();
			}
			// if the BP's URI is the one we are looking for
			if(bpUri != null && bpUri.equals(brokerPolicyUri))
			{
				// return the BP as a stream
				return child.getContentStream();
			}
		}
		
		// Reaching here means we didn't find a BP with the URI we are looking for
		// Throw an exception announcing this.
		throw new CompletenessException("Could not find Broker Policy file with URI " + brokerPolicyUri);
	}

	public void evaluateCompletenessCompliance(InputStream sdis) throws IOException, CompletenessException, ComplianceException 
	{
		gregEvaluator.getPcc().validateSDForCompletenessCompliance(sdis);
		ep.getCompletenessReport().setStatus("OK");
		ep.getComplianceReport().setStatus("OK");

		/*
		// Perform completeness check
		List<ClassInstancePair> qvPairList = null;
		qvPairList = gregEvaluator.getPcc().completenessCheck(sdis);
		ep.getCompletenessReport().setStatus("OK");

		// reset InputStream in order to reuse it
		sdis.reset();
		
		// Perform compliance check
		if (qvPairList != null) {
			gregEvaluator.getPcc().complianceCheck(sdis, qvPairList);
			ep.getComplianceReport().setStatus("OK");
		}
		*/
	}

}
