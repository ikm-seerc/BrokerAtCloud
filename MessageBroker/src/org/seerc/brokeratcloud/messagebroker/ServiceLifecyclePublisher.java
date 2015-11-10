package org.seerc.brokeratcloud.messagebroker;

import com.google.gson.JsonObject;

public class ServiceLifecyclePublisher {

	private static String serviceOnboardedTopic = "serviceOnboardedTopic"; 
	private static String serviceUpdatedTopic = "serviceUpdatedTopic"; 
	private static String serviceDeprecatedTopic = "serviceDeprecatedTopic"; 
	private static String serviceRemovedTopic = "serviceRemovedTopic"; 

	MessageBrokerStringPublisher onboardedPublisher;
	MessageBrokerStringPublisher updatedPublisher;
	MessageBrokerStringPublisher deprecatedPublisher;
	MessageBrokerStringPublisher removedPublisher;
	
	public ServiceLifecyclePublisher()
	{
		this.onboardedPublisher = new MessageBrokerStringPublisher("onboardedPublisher", serviceOnboardedTopic);
		this.updatedPublisher = new MessageBrokerStringPublisher("updatedPublisher", serviceUpdatedTopic);
		this.deprecatedPublisher = new MessageBrokerStringPublisher("deprecatedPublisher", serviceDeprecatedTopic);
		this.removedPublisher = new MessageBrokerStringPublisher("removedPublisher", serviceRemovedTopic);
	}
	
	public static void main(String[] args) {
		ServiceLifecyclePublisher slp = new ServiceLifecyclePublisher();
		
		slp.serviceOnboarded("http://the.onboarded.service/completeURI#theFragment");
		slp.serviceUpdated("http://the.updated.service/completeURI#theFragment");
		slp.serviceDeprecated("http://the.deprecated.service/completeURI#theFragment");
		slp.serviceRemoved("http://the.removed.service/completeURI#theFragment");
	}
	
	private String constructJSONServiceEvent(String serviceID)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("serviceID", serviceID);
		
		return jsonObject.toString();
	}

	public void serviceOnboarded(String serviceID) {
		this.onboardedPublisher.publishStringToTopic(constructJSONServiceEvent(serviceID));
	}

	public void serviceUpdated(String serviceID) {
		this.updatedPublisher.publishStringToTopic(constructJSONServiceEvent(serviceID));
	}

	public void serviceDeprecated(String serviceID) {
		this.deprecatedPublisher.publishStringToTopic(constructJSONServiceEvent(serviceID));
	}

	public void serviceRemoved(String serviceID) {
		this.removedPublisher.publishStringToTopic(constructJSONServiceEvent(serviceID));
	}

}
