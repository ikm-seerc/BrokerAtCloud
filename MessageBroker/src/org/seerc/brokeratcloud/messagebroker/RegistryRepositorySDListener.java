package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RegistryRepositorySDListener implements MessageListener {

	WSO2GREGClient wso2gregClient;
	
	// we need this to extract the service instance URI
	PolicyCompletenessCompliance pcc;
	
	public RegistryRepositorySDListener(WSO2GREGClient wso2gregClient) {
		this.wso2gregClient = wso2gregClient;
		this.pcc = new PolicyCompletenessCompliance();
	}

	@Override
	public void onMessage(Message message) {
		try {
			// message received from PubSub
			System.out.println("RegistryRepositorySDListener received the message with ID==> "
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

			// the service instance URI
			URI siUri = null;
			try {
				siUri = new URI(pcc.getSDServiceInstanceURI(sdis));
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
			
			bOutput.close(); // close ByteArrayOutputStream
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RegistryException e) {
			e.printStackTrace();
		}
	}
}
