package org.seerc.brokeratcloud.messagebroker;

import javax.jms.MessageListener;

public class SDRemoveSubscriber extends MessageBrokerSubscriber{

	public SDRemoveSubscriber(String clientId, String topicName) {
		super(clientId, topicName, new SDRemoveListener(), false);
	}

	public static void main(String[] args) {
		final SDRemoveSubscriber sdrs = new SDRemoveSubscriber("SDRemoveSubscriber", "SDRemove");
		sdrs.subscribeToTopic();
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	sdrs.releaseResources();
		    }

		});
	}
}
