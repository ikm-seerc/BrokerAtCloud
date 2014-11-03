package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

//This class is the template of a topic publisher for WSO2 Message Broker
public class MessageBrokerPublisher {
	protected InitialContext ctx;
	private String mbClientId;
	private String mbUsername = "admin";
	private String mbPassword = "admin";
	private String mbVirtualHostName = "carbon"; // fixed
	private String mbDefaultHostName = "localhost";
	private String mbDefaultPort = "5673";
	protected String connectionFactoryName = "qpidConnectionfactory"; // fixed
	protected String topicName; // the topic should be predefined by the
										// administrator of the MB

	public MessageBrokerPublisher(String clientId, String topicName) {
		if (clientId != null) {
			this.mbClientId = clientId;
			this.topicName = topicName;
			initializeContext(mbUsername, mbPassword);
		} else {
			throw new NullPointerException("clientId must not be null");
		}
	}

	// This sets ctx with the InitialContext object which is all that is
	// required to connect to the message broker
	private void initializeContext(String userName, String password) {
		Properties properties = new Properties();
		properties.put("java.naming.factory.initial",
				"org.wso2.andes.jndi.PropertiesFileInitialContextFactory"); // InitialContextFactory
		properties.put("connectionfactory." + connectionFactoryName,
				getTCPConnectionURL(userName, password)); // ConnectionFactory
		properties.put("topic." + topicName, topicName);// Topic
		try {
			ctx = new InitialContext(properties);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private String getTCPConnectionURL(String username, String password) {
		// amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
		return new StringBuffer().append("amqp://").append(username)
				.append(":").append(password).append("@").append(mbClientId)
				.append("/").append(mbVirtualHostName)
				.append("?brokerlist='tcp://").append(mbDefaultHostName)
				.append(":").append(mbDefaultPort).append("'").toString();
	}


	public void publishBytesMessageFromFileToTopic(String pathToFile) {
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
			
			//Standard java code for publishing each byte of the file residing in pathToFile
			File fileToPublish = new File(pathToFile);
			BytesMessage topicMessage = tSession.createBytesMessage();
			FileInputStream fis = new FileInputStream(fileToPublish);
			BufferedInputStream inBuf = new BufferedInputStream(fis);
			int i;
			while ((i = inBuf.read()) != -1) {
				topicMessage.writeInt(i);
			}

			// adding an eof
			topicMessage.writeInt(-1);
			
			inBuf.close(); // close BufferedInputStream
			fis.close(); // close FileInputStream
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
