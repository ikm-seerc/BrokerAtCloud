package org.seerc.brokeratcloud.messagebrokercomponents;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.seerc.brokeratcloud.messagebroker.EvaluationComponentSDSubscriber;
import org.seerc.brokeratcloud.messagebroker.RegistryRepositoryTopicSubscriber;

public class MBComponentsServlet extends HttpServlet {

	public void init() throws ServletException
    {
		RegistryRepositoryTopicSubscriber.main(null);
		EvaluationComponentSDSubscriber.main(null);
		
        System.out.println("----------");
        System.out.println("---------- MBComponentsServlet Servlet Initialized successfully ----------");
        System.out.println("----------");
    }
}
