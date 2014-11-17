package org.seerc.brokeratcloud.messagebroker;

public class MessageBrokerDemo {

	public static void main(String[] args) {
		
		MessageBrokerPublisher mbp = new MessageBrokerPublisher("publisher1", "SD");
		MessageBrokerSubscriber mbs = new MessageBrokerSubscriber("subscriber1", "SD", new BytesMessageListener());
		
		mbs.subscribeToTopic();
		//mbp.publishBytesMessageFromFileToTopic("files/SAP_HANA_Cloud_Apps_SD_test.ttl");
		mbp.publishBytesMessageFromFileToTopic("files/CAS-AddressApp.ttl");
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mbs.releaseResources();
	}
}
