package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
import org.seerc.brokeratcloud.policycompletenesscompliance.ClassInstancePair;
import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.seerc.brokeratcloud.policycompletenesscompliance.ComplianceException;
import org.seerc.brokeratcloud.policycompletenesscompliance.EvaluationReport;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

public class SDEvaluationListener implements MessageListener {

	// Holds a completeness/compliance evaluator
	WSO2GREGEvaluator gregEvaluator;
	
	// the Gson POJO de/serializer
	Gson gson;
	
	// the evaluation report object
	EvaluationReport ep;
	
	// Holds an SD report publisher
	MessageBrokerStringPublisher mbsp;
	
	WSO2GREGClient wso2gregClient;

	public SDEvaluationListener()
	{
		try {
			gregEvaluator = new WSO2GREGEvaluator();

			gson = new Gson();
			
			// Create the SD report publisher
			mbsp = new MessageBrokerStringPublisher("EvaluationComponentSDReportPublisher", "SDReport");
			
			this.wso2gregClient = new WSO2GREGClient();
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			// message received from PubSub
			System.out.println("SDEvaluationListener received the message with ID==> "
					+ message.getJMSMessageID());
			
			BytesMessage bm = (BytesMessage) message;
			
			// get its bytes
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			int i;
			while((i=bm.readInt())!=-1){
				bOutput.write(i);
			}
			
			// The SD as an InpuStream
			InputStream sdis=new ByteArrayInputStream(((ByteArrayOutputStream) bOutput).toByteArray());
			
			// First send the SD to Registry using the RegistryRepositorySDListener
			// the service instance URI
			URI siUri = null;
			try {
				siUri = new URI(gregEvaluator.getPcc().getSDServiceInstanceURI(sdis));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			// Path to put the SD resource
			// That is serviceDescriptionsFolder + URI fragment (part after #) + .ttl
			String pathToPutSiUri = WSO2GREGClient.getServiceDescriptionsFolder() + siUri.getFragment() + ".ttl";
			
			// reset stream to reuse it
			sdis.reset();
			
			// send SD to Registry Repository
			System.out.println("Sending received SD to repository at " + pathToPutSiUri);
			
			if(this.wso2gregClient.getRemote_registry().resourceExists(pathToPutSiUri))
			{	// resource exists, delete it
				this.wso2gregClient.getRemote_registry().delete(pathToPutSiUri);
			}
			
			Resource sdResource = this.wso2gregClient.getRemote_registry().newResource();
			sdResource.setContentStream(sdis);
			sdResource.setMediaType("text/plain");
			this.wso2gregClient.putWithRetryHack(pathToPutSiUri, sdResource);
			
			// reuse stream
			sdis.reset();

			// The service instance URI
			String si = new URI(gregEvaluator.getPcc().getSDServiceInstanceURI(sdis)).getFragment();
			
			// reuse stream
			sdis.reset();

			// (re)instantiate evaluation report object
			ep = new EvaluationReport();

			ep.setServiceInstance(si);
			
			// get the BrokerPolicy for this SD by looking in GReg BPs folder
			InputStream bpInputStream = this.getBpForSd(sdis);
			
			// reuse stream
			sdis.reset();

			// set the broker policy of the PCC
			gregEvaluator.getPcc().getBrokerPolicy(bpInputStream);
					
			// perform evaluation
			this.evaluateCompletenessCompliance(sdis);

			// evaluation went OK, publish serialized report
			mbsp.publishStringToTopic(gson.toJson(ep));
			
			bOutput.close(); // close ByteArrayOutputStream
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompletenessException e) {
			System.err
			.println("Error - The Service Description is incomplete");
			ep.getCompletenessReport().setStatus("Error");
			ep.getCompletenessReport().setEvaluationDescription("The Service Description is incomplete - " + e.getMessage());
			// evaluation had problems in completeness, publish problem
			mbsp.publishStringToTopic(gson.toJson(ep));
		} catch (ComplianceException e) {
			System.err
			.println("Error - The Service Description is uncompliant");
			ep.getComplianceReport().setStatus("Error");
			ep.getComplianceReport().setEvaluationDescription("The Service Description is uncompliant - " + e.getMessage());
			// evaluation had problems in compliance, publish problem
			mbsp.publishStringToTopic(gson.toJson(ep));
		} catch (RegistryException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Given an SD's input stream, find the BP (if any) that this SD should be evaluated against.
	 * This BP should have the same URI as the SD's gr:hasMakeAndModel association's target.
	 * Will throw a CompletenessException if it doesn't find any.
	 */
	private InputStream getBpForSd(InputStream sdis) throws IOException, RegistryException, CompletenessException {
		// get the broker policy URI defined in this SD
		String brokerPolicyUri = gregEvaluator.getPcc().getSDMakeAndModelURI(sdis);
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

	private void evaluateCompletenessCompliance(InputStream sdis) throws IOException, CompletenessException, ComplianceException 
	{
		gregEvaluator.getPcc().validateSDForCompletenessCompliance(sdis);
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
