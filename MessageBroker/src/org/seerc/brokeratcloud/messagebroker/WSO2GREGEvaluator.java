package org.seerc.brokeratcloud.messagebroker;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.seerc.brokeratcloud.policycompletenesscompliance.ClassInstancePair;
import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.seerc.brokeratcloud.policycompletenesscompliance.ComplianceException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class WSO2GREGEvaluator extends WSO2GREGClient {
	
	PolicyCompletenessCompliance pcc;
	
	public static void main(String[] args) {
		try {
			WSO2GREGEvaluator gregDemo = new WSO2GREGEvaluator();

			// Get broker policy in Java object structure
			gregDemo.getPcc().getBrokerPolicy(brokerPolicyPath);

			// Perform completeness check
			List<ClassInstancePair> qvPairList = null;
			try {
				qvPairList = gregDemo.getPcc()
						.completenessCheck(serviceDescriptionPath);
			} catch (CompletenessException e) {
				System.err
						.println("Error - The Service Description is incomplete");
			}

			// Perform compliance check
			if (qvPairList != null) {
				try {
					gregDemo.getPcc().complianceCheck(
							serviceDescriptionPath,
							qvPairList);
				} catch (ComplianceException e) {
					System.err
							.println("Error - The Service Description is uncompliant");
				}
			}

		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public WSO2GREGEvaluator() {
		super();
		// instantiate the PolicyCompletenessCompliance object
		pcc = new PolicyCompletenessCompliance() {
			/*
			 * This overrides addDataFromFile(String file) in PolicyCompletenessCompliance in order to
			 * fetch resources from GREG.
			 */
			protected void addDataFromFile(String resourcePath) {
				try {
					Resource r = remote_registry.get(resourcePath);
					InputStream in = r.getContentStream();
					this.modelMem.read(in, null, "TTL");
					in.close();
				} catch (RegistryException e) {
					System.err.println("Could not get resource from " + resourcePath);
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Error closing InputStream for " + resourcePath);
					e.printStackTrace();
				}
			}
		};
	}

	public PolicyCompletenessCompliance getPcc() {
		return pcc;
	}
}
