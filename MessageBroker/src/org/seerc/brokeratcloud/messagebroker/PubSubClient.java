package org.seerc.brokeratcloud.messagebroker;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;

public class PubSubClient {
	// MB configuration properties
	protected Properties wso2_mb_properties = new Properties();
	protected static String wso2_mb_properties_path = "/properties/wso2_mb.properties";
	protected static String mbWsURL;
	protected static String mbUsername;
	protected static String mbPassword;

    private BrokerClient brokerClient;
    private static PubSubClient instance;
    
    public static PubSubClient getInstance()
    {
    	if(instance == null)
    	{
    		instance = new PubSubClient();
    	}
    	
    	return instance;
    }
    
	private PubSubClient()
    {
    	try {
    		// load the WSO2 MB related properties
    		this.readWSO2MBProperties();

    		this.setupClientKeystore();
			this.brokerClient = new BrokerClient(mbWsURL, mbUsername, mbPassword);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (AuthenticationExceptionException e) {
			e.printStackTrace();
		}
    }

	private void readWSO2MBProperties()
	{
		try {
			wso2_mb_properties.load(this.getClass().getResourceAsStream(wso2_mb_properties_path));
			mbWsURL = wso2_mb_properties.getProperty("mbWsURL");
			mbUsername = wso2_mb_properties.getProperty("mbUsername");
			mbPassword = wso2_mb_properties.getProperty("mbPassword");
		} catch (IOException e) {
			System.err.println("Could not load properties file from: " + wso2_mb_properties_path);
			e.printStackTrace();
		}
	}

	private void setupClientKeystore() {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = this.getClass().getResourceAsStream("/keystore/wso2carbon.jks");
			ks.load(resourceAsStream, "wso2carbon".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, "wso2carbon".toCharArray());
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLContext.setDefault(ctx); 
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}
	
    public BrokerClient getBrokerClient() {
		return brokerClient;
	}

}