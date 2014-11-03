package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

public class MessageBrokerStringPublisher extends MessageBrokerPublisher {

	public MessageBrokerStringPublisher(String clientId, String topicName) {
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
			
			//Standard java code for publishing each byte of the string report
			BytesMessage topicMessage = tSession.createBytesMessage();
			InputStream stream = new ByteArrayInputStream(stringMessage.getBytes());
			BufferedInputStream inBuf = new BufferedInputStream(stream);
			int i;
			while ((i = inBuf.read()) != -1) {
				topicMessage.writeInt(i);
			}

			// adding an eof
			topicMessage.writeInt(-1);

			inBuf.close(); // close BufferedInputStream
			stream.close(); // close ByteArrayInputStream
			
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
