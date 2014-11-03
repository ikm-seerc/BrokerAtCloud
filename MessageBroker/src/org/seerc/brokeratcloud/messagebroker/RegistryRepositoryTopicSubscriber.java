package org.seerc.brokeratcloud.messagebroker;

import javax.jms.MessageListener;

/*
 * This class will subscribe to PubSub for "SDReport" - will get report and set a report property of the resource at serviceDescriptionPath
 */
public class RegistryRepositoryTopicSubscriber {

	MessageBrokerSubscriber sdReportSubscriber;
	WSO2GREGClient gregClient;
	
	public static void main(String[] args) {
		RegistryRepositoryTopicSubscriber rrts = new RegistryRepositoryTopicSubscriber();
	}
	
	public RegistryRepositoryTopicSubscriber()
	{
		super();

		// instantiate Greg Client
		gregClient = new WSO2GREGClient();
		
		// instantiate the subscriber and override message listener
		sdReportSubscriber = new MessageBrokerSubscriber("RegistryRepositoryTopicSubscriber", "SDReport", new RegistryRepositorySDReportListener(gregClient));
		
		//subscribe to topic
		sdReportSubscriber.subscribeToTopic();
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	sdReportSubscriber.releaseResources();
		    }

		});

	}
}
