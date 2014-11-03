package org.seerc.brokeratcloud.messagebroker;

import javax.jms.MessageListener;

public class EvaluationComponentSDSubscriber extends MessageBrokerSubscriber{

	public EvaluationComponentSDSubscriber(String clientId, String topicName) {
		super(clientId, topicName, new SDEvaluationListener());
	}

	public static void main(String[] args) {
		final EvaluationComponentSDSubscriber ecsds = new EvaluationComponentSDSubscriber("EvaluationComponentSDSubscriber", "SD");
		ecsds.subscribeToTopic();
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	ecsds.releaseResources();
		    }

		});
	}
}
