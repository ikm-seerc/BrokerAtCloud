package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class VttsPubSubBridge {

	static String serverAddress = "";
	
	MessageBrokerStringPublisher vttsValidationReportPublisher;
	MessageBrokerSubscriber vttsValidationSubscriber;
	
	public VttsPubSubBridge()
	{
		vttsValidationReportPublisher = new MessageBrokerStringPublisher("vttsValidationReportPublisher", "vttsValidationReport");
		vttsValidationSubscriber = new MessageBrokerSubscriber("vttsValidationSubscriber", "vttsValidation", new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				// message received from PubSub
				try {
					System.out.println("vttsValidationSubscriber received the message with ID==> "
							+ message.getJMSMessageID());
					BytesMessage bm = (BytesMessage) message;
					
					// get its bytes
					ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
					int i;
					while((i=bm.readInt())!=-1){
						bOutput.write(i);
					}
					
					// The document as a String
					String document = bOutput.toString();
					
					// send for validation
					String response = HttpPostClient.postMessageToEndpoint(serverAddress + "/vtts/rest/VTTSEngine/validation", document);
					
					// publish report to PubSub
					vttsValidationReportPublisher.publishStringToTopic(response);

				} catch (JMSException | IOException e) {
					e.printStackTrace();
					vttsValidationReportPublisher.publishStringToTopic(e.getMessage());
				}
			}
		}, false);
		vttsValidationSubscriber.subscribeToTopic();
	}
	
	public static void main(String[] args) {
		serverAddress = args[0];
		VttsPubSubBridge vttsPbBridge = new VttsPubSubBridge();
	}

}
