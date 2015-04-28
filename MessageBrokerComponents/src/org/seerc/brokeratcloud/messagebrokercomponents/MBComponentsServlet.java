package org.seerc.brokeratcloud.messagebrokercomponents;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.seerc.brokeratcloud.messagebroker.EvaluationComponentSDSubscriber;
import org.seerc.brokeratcloud.messagebroker.OutOfRangeSLAViolationChecker;
import org.seerc.brokeratcloud.messagebroker.RegistryRepositoryTopicSubscriber;
import org.seerc.brokeratcloud.messagebroker.WSO2GREGClient;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class MBComponentsServlet extends HttpServlet {

	public void init() throws ServletException
    {
		try {
			this.createNeededPathsInGReg();
		} catch (RegistryException e) {
			e.printStackTrace();
		}
		RegistryRepositoryTopicSubscriber.main(null);
		EvaluationComponentSDSubscriber.main(null);
		OutOfRangeSLAViolationChecker.main(null);
		
        System.out.println("----------");
        System.out.println("---------- MBComponentsServlet Servlet Initialized successfully ----------");
        System.out.println("----------");
    }

	private void createNeededPathsInGReg() throws RegistryException {
		String brokerAtCloudFolderPath = "/brokerAtCloud";
		String brokerPoliciesFolderPath = "/brokerAtCloud/brokerPolicies";
		String serviceDescriptionsFolderPath = "/brokerAtCloud/serviceDescriptions";
		
		WSO2GREGClient greg = new WSO2GREGClient();
		
		if(!greg.getRemote_registry().resourceExists(brokerAtCloudFolderPath))
		{
			Collection brokerAtCloudFolder = greg.getRemote_registry().newCollection();
			greg.putWithRetryHack(brokerAtCloudFolderPath, brokerAtCloudFolder);
		}
		
		if(!greg.getRemote_registry().resourceExists(brokerPoliciesFolderPath))
		{
			Collection brokerPoliciesFolder = greg.getRemote_registry().newCollection();
			greg.putWithRetryHack(brokerPoliciesFolderPath, brokerPoliciesFolder);
		}
		
		if(!greg.getRemote_registry().resourceExists(serviceDescriptionsFolderPath))
		{
			Collection serviceDescriptionsFolder = greg.getRemote_registry().newCollection();
			greg.putWithRetryHack(serviceDescriptionsFolderPath, serviceDescriptionsFolder);
		}
		
	}
}
