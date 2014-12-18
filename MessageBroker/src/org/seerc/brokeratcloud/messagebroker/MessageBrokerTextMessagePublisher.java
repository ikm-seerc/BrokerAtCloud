package org.seerc.brokeratcloud.messagebroker;

import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

public class MessageBrokerTextMessagePublisher extends MessageBrokerPublisher {

	public MessageBrokerTextMessagePublisher(String clientId, String topicName) {
		super(clientId, topicName);
	}

	public void publishStringToTopic(String stringMessage) {
		try {
			//----------------------------------------------------------------------
			//Plumbing code for establishing a connection with an existing topic.
			//The topic is created in the first place via the WSO2 GUI.
			//This code is not really required - we only require to know how to call this method
			//I.e. we only need to know the parameters that this method accepts.
			TopicConnectionFactory tConnectionFactory = (TopicConnectionFactory) ctx
					.lookup(connectionFactoryName);
			TopicConnection tConnection = tConnectionFactory
					.createTopicConnection();
			tConnection.start();
			TopicSession tSession = tConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = (Topic) ctx.lookup(topicName);
			//----------------------------------------------------------------------
			
			//Creates a publisher object (tPublisher)
			javax.jms.TopicPublisher tPublisher = tSession
					.createPublisher(topic);
			
			//Standard java code for publishing the text message
			TextMessage topicMessage = tSession.createTextMessage();
			topicMessage.setText(stringMessage);
			
			tPublisher.publish(topicMessage);// actual publishing

			tConnection.close();
			tSession.close();
			tPublisher.close();

		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}
}
