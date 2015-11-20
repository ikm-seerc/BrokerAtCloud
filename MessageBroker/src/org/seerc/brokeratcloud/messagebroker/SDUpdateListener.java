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
import org.seerc.brokeratcloud.policycompletenesscompliance.WSO2GREGClient;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

public class SDUpdateListener extends AbstractSDEvaluationListener{

	@Override
	public void onMessage(Message message) {
		try {
			// message received from PubSub
			System.out.println("SDUpdateListener received the message with ID==> "
					+ message.getJMSMessageID());
			
			// (re)instantiate evaluation report object
			ep = new EvaluationReport();

			BytesMessage bm = (BytesMessage) message;
			
			// get its bytes
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			int i;
			while((i=bm.readInt())!=-1){
				bOutput.write(i);
			}
			
			// The SD as an InpuStream
			InputStream sdis=new ByteArrayInputStream(((ByteArrayOutputStream) bOutput).toByteArray());
			
			// the service instance URI
			URI siUri = null;
			try {
				siUri = new URI(gregEvaluator.getPcc().getSDServiceInstanceURI(sdis));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			// Path to put the SD resource
			// That is serviceDescriptionsFolder + name from URI + .ttl
			String pathToPutSiUri = WSO2GREGClient.getServiceDescriptionsFolder() + WSO2GREGClient.createNameFromUri(siUri) + ".ttl";
			
			// reset stream to reuse it
			sdis.reset();
			
			InputStream currentSD = null;
			
			if(!this.wso2gregClient.getRemote_registry().resourceExists(pathToPutSiUri))
			{	// resource does not exist, throw CompletenessException
				System.out.println("An SD with namespace " + siUri + " does not exist.");
				throw new CompletenessException("An SD with namespace " + siUri + " does not exist.");
			}
			
			currentSD = this.wso2gregClient.getRemote_registry().get(pathToPutSiUri).getContentStream();

			// send SD to Registry Repository
			System.out.println("Sending updated SD to repository at " + pathToPutSiUri);
			
			Resource sdResource = this.wso2gregClient.getRemote_registry().newResource();
			sdResource.setContentStream(sdis);
			sdResource.setMediaType("text/plain");
			this.wso2gregClient.putWithRetryHack(pathToPutSiUri, sdResource);
			
			// reuse stream
			sdis.reset();

			// The service instance URI
			String si = WSO2GREGClient.createNameFromUri(new URI(gregEvaluator.getPcc().getSDServiceInstanceURI(sdis)));
			
			// reuse stream
			sdis.reset();

			ep.setServiceInstance(si.toString());
			
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
			
			// reuse stream
			sdis.reset();

			// send to Fuseki
			System.out.println("Evaluation went OK, sending updated SD to Fuseki and generating lifecycle events.");
			// delete old from Fuseki
			fc.deleteInputStreamFromFuseki(currentSD);

			// add current to Fuseki
			fc.addInputStreamToFuseki(sdis);
			
			// update of service
			this.slp.serviceUpdated(siUri.toString());
			
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
			// An exception here denotes that completeness check went OK, set status
			ep.getCompletenessReport().setStatus("OK");

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
}
