package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

public class BytesMessageListener implements javax.jms.MessageListener {


	/**
	 * Override this method and add the operation which is needed to be done
	 * when a message is arrived
	 * 
	 * @param message
	 *            - the next received message
	 */
	@Override
	//message = entire message to be received
	public void onMessage(Message message){

		try {
			System.out.println("BytesMessageListener received the message with ID==> "
					+ message.getJMSMessageID());

			BytesMessage bm = (BytesMessage) message;

			String pathToSaveFile = "files/received_file.ttl";
			File file = new File(pathToSaveFile);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream outBuf = new BufferedOutputStream(fos);
			int i;
			while((i=bm.readInt())!=-1){
			   outBuf.write(i);
			}
			outBuf.close(); // close BufferedInputStream
			fos.close(); // close FileOutputStream
			
			System.out.println("Saved the file contained in the message to==> "
					+ pathToSaveFile);
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}

	}
}
