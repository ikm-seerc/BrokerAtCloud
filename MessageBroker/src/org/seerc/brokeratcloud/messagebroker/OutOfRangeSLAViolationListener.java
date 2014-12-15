package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.EvaluationReport;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.seerc.brokeratcloud.policycompletenesscompliance.QuantitativeValue;
import org.seerc.brokeratcloud.policycompletenesscompliance.QuantitativeValueInstance;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class OutOfRangeSLAViolationListener implements MessageListener {

	public static void main(String[] args) {
		OutOfRangeSLAViolationListener test = new OutOfRangeSLAViolationListener();
		try {
			Boolean result = test.qvIsInRange("http://www.broker-cloud.eu/service-descriptions/CAS/broker#AllowedAvailabilityValue", 99.7f);
			int i=0;
		} catch (RegistryException | SecurityException
				| IllegalArgumentException | NoSuchMethodException
				| ClassNotFoundException | InstantiationException
				| IllegalAccessException | InvocationTargetException
				| IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message message) {
		// message received from PubSub
		try {
			System.out.println("OutOfRangeSLAViolationListener received the message with ID==> "
					+ message.getJMSMessageID());
			
			BytesMessage bm = (BytesMessage) message;
			
			// get its bytes
			ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
			int i;
			while((i=bm.readInt())!=-1){
				bOutput.write(i);
			}

			// make it a String
			String contents = new String(bOutput.toByteArray(), "UTF-8");
			
			System.out.println(contents);
			
			// get QV to check from JSON
			String qvToCheck = this.parseQVFromJsonMonitoringEvent(contents);

			// get QV value from JSON
			Float qvValue = this.parseQVValueFromJsonMonitoringEvent(contents);
			
			// check BPs for QV and if it's in range
			if(!this.qvIsInRange(qvToCheck, qvValue))
			{	// not in range
				// send event to SLA violation errors topic
				System.out.println("QV " + qvToCheck + " with value " + qvValue + " was found out of range. Sending event to SLA violation errors topic.");
				this.sendMonitoringEventToSLAViolationsTopic(contents);
			}
			
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (RegistryException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMonitoringEventToSLAViolationsTopic(String contents)
	{
		MessageBrokerStringPublisher slaViolationReporter = new MessageBrokerStringPublisher("slaViolationReporter", WSO2MBClient.slaViolationErrorReportingTopic);
		slaViolationReporter.publishStringToTopic(contents);
	}

	private boolean qvIsInRange(String qvToCheck, Float qvValue) throws RegistryException, SecurityException, IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException
	{
		// user greg client to fetch BPs
		WSO2GREGClient greg = new WSO2GREGClient();
		// the BPs' folder
		Collection bpFolder = (Collection) greg.getRemote_registry().get(greg.getBrokerPoliciesFolder());
		
		// for each BP in BPs' folder
		for(int i=0;i<bpFolder.getChildCount();i++)
		{
			String bpPath = bpFolder.getChildren()[i];
			// get BP
			Resource bp = greg.getRemote_registry().get(bpPath);
			// Create a PCC and load BP
			PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
			pcc.getBrokerPolicy(bp.getContentStream());
			for(QuantitativeValue qv:pcc.getBP().getQuantitativeValueMap().values())
			{
				if(qv.getUri().equals(qvToCheck))
				{	// we found the QV
					return pcc.qvIsInRange(qvToCheck, qvValue);
					/*for(QuantitativeValueInstance qvi:qv.getInstanceMap().values())
					{
						if(qvValue.floatValue() >= qvi.getMinValue().floatValue() && qvValue.floatValue() <= qvi.getMaxValue().floatValue())
						{	// OK it's in range
							return true;
						}
					}*/
					
					// oops, not in range
					//throw new RegistryException("QV " + qvToCheck + " was not in range in BP at " + bpPath);
				}
			}
		}
		// oops, did not find QV in BPs
		throw new RegistryException("QV " + qvToCheck + " was not in range in BPs at " + bpFolder.getPath());
	}

	private Float parseQVValueFromJsonMonitoringEvent(String contents)
	{
		// TODO parse QV value from JSON
		return 108.7f;
	}

	private String parseQVFromJsonMonitoringEvent(String contents)
	{
		// TODO parse QV from JSON
		return "http://www.broker-cloud.eu/service-descriptions/CAS/broker#AllowedAvailabilityValue";
	}

}
