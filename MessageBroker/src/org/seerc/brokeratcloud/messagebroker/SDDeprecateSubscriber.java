package org.seerc.brokeratcloud.messagebroker;

import javax.jms.MessageListener;

public class SDDeprecateSubscriber extends MessageBrokerSubscriber{

	public SDDeprecateSubscriber(String clientId, String topicName) {
		super(clientId, topicName, new SDDeprecateListener(), false);
	}

	public static void main(String[] args) {
		final SDDeprecateSubscriber sdds = new SDDeprecateSubscriber("SDDeprecateSubscriber", "SDDeprecate");
		sdds.subscribeToTopic();
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	sdds.releaseResources();
		    }

		});
	}
}
