package org.seerc.brokeratcloud.messagebroker;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class MessageBrokerSubscriber {

	private InitialContext ctx;
	private TopicConnection tConnection;
	private TopicSession tSession;
	private TopicSubscriber tSubscriber;
	private String mbClientId;
	private String mbUsername = "admin";
	private String mbPassword = "admin";
	private String mbVirtualHostName = "carbon"; // fixed
	private String mbDefaultHostName = "localhost";
	private String mbDefaultPort = "5673";
	private String connectionFactoryName = "qpidConnectionfactory"; // fixed
	protected String topicName; // the topic should be predefined by the
										// administrator of the MB

	private MessageListener messageListener;
	private boolean isDurable;
	
	//constructor - very similar to the corresponding publisher constructor
	public MessageBrokerSubscriber(String clientId, String topicName, MessageListener messageListener, boolean isDurable) {
		if (clientId != null) {
			this.mbClientId = clientId;
			this.topicName = topicName;
			this.messageListener = messageListener;
			this.isDurable = isDurable;
			initializeContext(mbUsername, mbPassword);
		} else {
			throw new NullPointerException("clientId must not be null");
		}
	}

	// This sets ctx with the InitialContext object which contains all the information (opaque) required
	// to connect to the message broker
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

	//creates a string which is used by the above method (line 52) in order to create the initial context
	private String getTCPConnectionURL(String username, String password) {
		// amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
		return new StringBuffer().append("amqp://").append(username)
				.append(":").append(password).append("@").append(mbClientId)
				.append("/").append(mbVirtualHostName)
				.append("?brokerlist='tcp://").append(mbDefaultHostName)
				.append(":").append(mbDefaultPort).append("'").toString();
	}

	public void subscribeToTopic() {
		try {
			TopicConnectionFactory tConnectionFactory = (TopicConnectionFactory) ctx
					.lookup(connectionFactoryName);
			tConnection = tConnectionFactory.createTopicConnection();
			tConnection.start();
			tSession = tConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			
			Topic topic = (Topic) ctx.lookup(topicName);
			if(this.isDurable)
			{	// durable subscriber
				tSubscriber = tSession.createDurableSubscriber(topic, this.mbClientId);				
			}
			else
			{	// non-durable subscriber
				tSubscriber = tSession.createSubscriber(topic); //if the topic does not exist it is created at runtime by creating a subscriber for it (qpid default behavior, can't find a way to change this) 
			}

			//activates/attaches the message listener (callback function) 
			//this listener is called by the pub/sub server (message broker - mb) in order to inform the 
			//subscriber that a new message is pending for consumption
			tSubscriber.setMessageListener(this.messageListener);
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	public void unsubscribe()
	{
		try {
			TopicConnectionFactory tConnectionFactory = (TopicConnectionFactory) ctx
					.lookup(connectionFactoryName);
			tConnection = tConnectionFactory.createTopicConnection();
			tConnection.start();
			tSession = tConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.tSession.unsubscribe(this.mbClientId);
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public void releaseResources() {

		try {
			System.out
					.println("Releasing resources in Message Broker reserved by the Topic Listener...");

			// tConnection.close(); This suspends Thread execution, will see if it's needed...
			tSession.close();
			tSubscriber.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}

	}
}
