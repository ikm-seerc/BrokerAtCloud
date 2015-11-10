package org.seerc.brokeratcloud.messagebroker;

import javax.jms.MessageListener;

public class SDUpdateSubscriber extends MessageBrokerSubscriber{

	public SDUpdateSubscriber(String clientId, String topicName) {
		super(clientId, topicName, new SDUpdateListener(), false);
	}

	public static void main(String[] args) {
		final SDUpdateSubscriber sdus = new SDUpdateSubscriber("SDUpdateSubscriber", "SDUpdate");
		sdus.subscribeToTopic();
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	sdus.releaseResources();
		    }

		});
	}
}
