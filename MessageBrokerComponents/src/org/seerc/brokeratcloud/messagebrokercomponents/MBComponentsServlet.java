package org.seerc.brokeratcloud.messagebrokercomponents;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.seerc.brokeratcloud.messagebroker.EvaluationComponentSDSubscriber;
import org.seerc.brokeratcloud.messagebroker.OutOfRangeSLAViolationChecker;
import org.seerc.brokeratcloud.messagebroker.RegistryRepositoryTopicSubscriber;
import org.seerc.brokeratcloud.messagebroker.SDDeprecateSubscriber;
import org.seerc.brokeratcloud.messagebroker.SDRemoveSubscriber;
import org.seerc.brokeratcloud.messagebroker.SDUpdateSubscriber;
import org.seerc.brokeratcloud.messagebroker.VttsPubSubBridge;
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
		//SDUpdateSubscriber.main(null);
		//SDDeprecateSubscriber.main(null);
		//SDRemoveSubscriber.main(null);
		OutOfRangeSLAViolationChecker.main(null);
		
        try {
			System.out.println("Server Addresses: " + getEndPoints());
			VttsPubSubBridge.main(getEndPoints().toArray(new String[0]));
		} catch (UnknownHostException | MalformedObjectNameException | AttributeNotFoundException | InstanceNotFoundException | NullPointerException | MBeanException | ReflectionException e) {
			e.printStackTrace();
		}
		
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

	List<String> getEndPoints() throws MalformedObjectNameException,
	NullPointerException, UnknownHostException, AttributeNotFoundException,
	InstanceNotFoundException, MBeanException, ReflectionException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
				Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		String hostname = InetAddress.getLocalHost().getHostName();
		InetAddress[] addresses = InetAddress.getAllByName(hostname);
		ArrayList<String> endPoints = new ArrayList<String>();
		for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
			ObjectName obj = i.next();
			String scheme = mbs.getAttribute(obj, "scheme").toString();
			String port = obj.getKeyProperty("port");
			for (InetAddress addr : addresses) {
				String host = addr.getHostAddress();
				String ep = scheme + "://" + host + ":" + port;
				endPoints.add(ep);
			}
		}
		return endPoints;
	}
}
