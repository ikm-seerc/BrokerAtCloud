package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.EvaluationReport;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

public class RegistryRepositorySDReportListener implements MessageListener {
	
	WSO2GREGClient wso2gregClient;
	
	// the Gson POJO de/serializer
	Gson gson;
	
	public RegistryRepositorySDReportListener(WSO2GREGClient wso2gregClient)
	{
		this.wso2gregClient = wso2gregClient;
		this.gson = new Gson();
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			// message received from PubSub
			System.out.println("RegistryRepositorySDReportListener received the message with ID==> "
					+ message.getJMSMessageID());
			
			BytesMessage bm = (BytesMessage) message;
			
			// get its bytes
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			int i;
			while((i=bm.readInt())!=-1){
				bOutput.write(i);
			}
			
			// The SD Report as a String
			String sdReport = bOutput.toString();
			System.out.println("Got SD report: " + sdReport);
			
			// The evaluation report POJO 
			EvaluationReport ep = gson.fromJson(sdReport, EvaluationReport.class);
			
			// Update resource property of SD at Registry Repository
			String sdResourcePath = WSO2GREGClient.getServiceDescriptionsFolder() + ep.getServiceInstance() + ".ttl";
					
			System.out.println("Setting property of resource at " + sdResourcePath);
			Resource sdResource = this.wso2gregClient.getRemote_registry().get(sdResourcePath);
			sdResource.setProperty("SD evaluation report", sdReport);
			sdResource.setMediaType("text/plain");
			this.wso2gregClient.putWithRetryHack(sdResourcePath, sdResource);
			
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
