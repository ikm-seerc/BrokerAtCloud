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

public abstract class AbstractSDLifecycleListener implements MessageListener{

	ServiceLifecyclePublisher slp;

	public AbstractSDLifecycleListener()
	{
		slp = new ServiceLifecyclePublisher();
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			// message received from PubSub
			System.out.println(this.getClass().getSimpleName() + " received the message with ID==> "
					+ message.getJMSMessageID());
			
			BytesMessage bm = (BytesMessage) message;
			
			// get its bytes
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			int i;
			while((i=bm.readInt())!=-1){
				bOutput.write(i);
			}
			
			// The service instance URI as an InpuStream
			InputStream sdis=new ByteArrayInputStream(((ByteArrayOutputStream) bOutput).toByteArray());
			
			// TODO: Convert InputStream to String and use the service instance URI to inform about deprecation event
			String serviceID = "";
			// TODO: first check if service exists, if not inform who?
			performLifecycleEventForService(serviceID);
			
			bOutput.close(); // close ByteArrayOutputStream
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	protected abstract void performLifecycleEventForService(String serviceID);
}
