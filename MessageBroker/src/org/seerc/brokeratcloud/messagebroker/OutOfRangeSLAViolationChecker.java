package org.seerc.brokeratcloud.messagebroker;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class OutOfRangeSLAViolationChecker{

	/*private WSO2MBClient mb; 
	private List<OutOfRangeSLAViolationSubscriber> qvTopicSubscribers;*/
	
	public OutOfRangeSLAViolationChecker()
	{
		/*
		 * In order to avoid double out-of-range subscribers across VMs,
		 * this component will PUT all BPs, which will trigger all validations of BPs,
		 * which in turn cause all subscribers for QV topics to be initiated only
		 * in the .CAR application inside GREG.
		 */
		
		System.out.println("PUT all BPs to validate and generate out-of-range subscribers");
		WSO2GREGClient greg = new WSO2GREGClient(); 
		greg.putAllBrokerPolicies();
		
		/*mb = new WSO2MBClient();
		qvTopicSubscribers = new ArrayList<OutOfRangeSLAViolationSubscriber>();
		
		
		 * 1) Subscribe to all monitoringTopics
		 * 2) The listener of those subscribers should check the payload for out-of-range.
		 *    If it is found out of range, it should send the message to the error topic. 
		 
		
		try {
			List<String> allTopics = mb.getAllTopics();
			for(String topicName:allTopics)
			{
				if(topicName.startsWith(WSO2MBClient.monitoringTopicPrefix))
				{ // monitoring Topic
					String subscriberName = "subscriberFor_" + topicName;
					OutOfRangeSLAViolationSubscriber qvSubscriber = new OutOfRangeSLAViolationSubscriber(subscriberName, topicName);
					qvTopicSubscribers.add(qvSubscriber);
					qvSubscriber.subscribeToTopic();
					System.out.println("Created QV out of range subscriber " + subscriberName);
				}
			}
			
		} catch (RegistryException e) {
			e.printStackTrace();
		}
		
		// cleanup on JVM shutdown - will only run when running from command-line, NOT from within IDE.
		Runtime.getRuntime().addShutdownHook(new Thread() {

		    @Override
		    public void run() {
		    	for(OutOfRangeSLAViolationSubscriber mbs:qvTopicSubscribers)
		    	{
		    		mbs.releaseResources();
		    	}
		    }

		});		*/
	}

	public static void main(String[] args) {
		new OutOfRangeSLAViolationChecker();
	}
}
