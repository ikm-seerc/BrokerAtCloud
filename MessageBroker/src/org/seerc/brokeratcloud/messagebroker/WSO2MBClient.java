package org.seerc.brokeratcloud.messagebroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


public class WSO2MBClient {

	public static final String monitoringTopicPrefix = "monitoringTopic-";
	public static final String slaViolationErrorReportingTopic = "slaViolationErrorReportingTopic";
	
	WSO2GREGClient gregInMB = new WSO2GREGClient(){

		protected void readWSO2GREGProperties() 
		{
			// additional GReg in MB properties
			Properties wso2_gregInMB_properties = new Properties();

			try {
				wso2_greg_properties.load(this.getClass().getResourceAsStream(wso2_greg_properties_path));
				wso2_gregInMB_properties.load(this.getClass().getResourceAsStream("/properties/wso2_mb.properties"));
				gregRepoURL = wso2_gregInMB_properties.getProperty("gregInMBRepoURL");
				gregRepoUsername = wso2_greg_properties.getProperty("gregRepoUsername");
				gregRepoPassword = wso2_greg_properties.getProperty("gregRepoPassword");
				brokerPolicyPath = wso2_greg_properties.getProperty("brokerPolicyPath");
				brokerPoliciesFolder = wso2_greg_properties.getProperty("brokerPoliciesFolder");
				serviceDescriptionPath = wso2_greg_properties.getProperty("serviceDescriptionPath");
				serviceDescriptionsFolder = wso2_greg_properties.getProperty("serviceDescriptionsFolder");
			} catch (IOException e) {
				System.err.println("Could not load properties file from: " + wso2_greg_properties_path);
				e.printStackTrace();
			}
		}
	};
	
	public static void main(String[] args) {
		WSO2MBClient mbc = new WSO2MBClient();
		try {
			mbc.getAllTopics();
		} catch (RegistryException e) {
			e.printStackTrace();
		}
	}

	public WSO2MBClient() {
	}

	public List<String> getAllTopics() throws RegistryException {
		List<String> result = new ArrayList<String>();
		Collection topicsFolder = (Collection)gregInMB.getRemote_registry().get("/_system/governance/event/topics");
		for(String s:topicsFolder.getChildren())
		{
			String topicName = s.substring(s.lastIndexOf("/") + 1);
			result.add(topicName);
		}
		
		return result;
	}

	public static void createTopic(String topicName) {
		/*
		 * Create the topic if it doesn't exist by subscribing to it
		 */
		MessageBrokerSubscriber mbs = new MessageBrokerSubscriber(UUID.randomUUID().toString(), topicName, new MessageListener() {
			// Empty MessageListener, just need to create the topic.
			@Override
			public void onMessage(Message arg0) {
				// do nothing
			}
		}, false);
		// create the topic by subscribing
		mbs.subscribeToTopic();
		// release, these resources should not be needed anymore
		mbs.releaseResources();
	}


}
