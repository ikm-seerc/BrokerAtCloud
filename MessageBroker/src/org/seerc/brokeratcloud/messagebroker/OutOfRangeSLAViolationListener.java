package org.seerc.brokeratcloud.messagebroker;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.seerc.brokeratcloud.policycompletenesscompliance.EvaluationReport;

public class OutOfRangeSLAViolationListener implements MessageListener {

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
			
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
