package org.seerc.brokeratcloud.messagebroker;

import java.util.ArrayList;
import java.util.List;

public class OutOfRangeSLAViolationSubscriber extends MessageBrokerSubscriber {

	/*
	 * This is a special subscriber. Many can create it, but only one per topic is
	 * going to be "activated". This is because the errors that flow to the out-of-range
	 * error topic should not be duplicated.
	 */
	
	private static List<String> alreadyActivatedTopics = new ArrayList<String>();
	
	// if this is true then the subscribeToTopic() should do nothing
	private boolean deactivated = false;
	
	public OutOfRangeSLAViolationSubscriber(String clientId, String topicName) {
		super(clientId, topicName, new OutOfRangeSLAViolationListener());
		
		// if this topic is already activated, then this subscriber should be deactivated
		if(alreadyActivatedTopics.contains(topicName))
		{
			this.deactivated = true;
		}
		else
		{	// now it is activated for the first time, add it to static List
			alreadyActivatedTopics.add(topicName);
		}
	}

	public void subscribeToTopic() {
		if(this.deactivated)
		{
			// do nothing
			System.out.println("A OutOfRangeSLAViolationSubscriber is already activated for topic " + this.topicName + ". Will not subscribe again...");
		}
		else
		{	// subscribe to topic normally
			super.subscribeToTopic();
		}
	}	
}
