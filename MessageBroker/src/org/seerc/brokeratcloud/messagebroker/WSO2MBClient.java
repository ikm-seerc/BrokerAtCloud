package org.seerc.brokeratcloud.messagebroker;

import java.util.ArrayList;
import java.util.List;
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
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class WSO2MBClient {

	private InitialContext ctx;
	private String mbClientId;
	private String mbUsername = "admin";
	private String mbPassword = "admin";
	private String mbVirtualHostName = "carbon"; // fixed
	private String mbDefaultHostName = "localhost";
	private String mbDefaultPort = "5673";
	private String connectionFactoryName = "qpidConnectionfactory"; // fixed
	private String topicName; // the topic should be predefined by the
										// administrator of the MB

	public static void main(String[] args) {
		WSO2MBClient mbc = new WSO2MBClient();
		mbc.getAllTopics();
	}

	public WSO2MBClient() {
		initializeContext(mbUsername, mbPassword);
	}

	// This sets ctx with the InitialContext object which contains all the information (opaque) required
	// to connect to the message broker
	private void initializeContext(String userName, String password) {
		Properties properties = new Properties();
		properties.put("java.naming.factory.initial",
				"org.wso2.andes.jndi.PropertiesFileInitialContextFactory"); // InitialContextFactory
		/*properties.put("connectionfactory." + connectionFactoryName,
				getTCPConnectionURL(userName, password)); // ConnectionFactory
*/		//properties.put("topic.*", "*");// Topic
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

	public void getAllTopics() {
		try {
			List<Topic> topics = new ArrayList<Topic>(); 
			scanJndiForQueues(topics, "");
			int i=0;
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void scanJndiForQueues(List<Topic> out, String path) throws NamingException {
	    //InitialContext context = new InitialContext();
	    Object resource = ctx.lookup(path);
	    if (isSubContext(resource)) {
	        NamingEnumeration<NameClassPair> list = ctx.list(path);
	        while (list.hasMoreElements()) {
	        	NameClassPair binding = list.nextElement();
	            scanJndiForQueues(out, path + "/" + binding.getName());
	        }
	    } else if (resource instanceof Topic) {
	        out.add((Topic) resource);
	    } // else ignore Topics
	}

	private boolean isSubContext(Object object) {
	    return javax.naming.Context.class.isAssignableFrom(object.getClass());
	}
}
