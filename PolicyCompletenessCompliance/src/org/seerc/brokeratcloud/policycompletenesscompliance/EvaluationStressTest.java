package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class EvaluationStressTest {

	public static void main(String[] args) {
		EvaluationStressTest est = new EvaluationStressTest();
		est.normalEvaluate();
		//est.performStressTest();
	}

	private void normalEvaluate()
	{
		try {
			PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

			// validate broker policy first
			pc.validateBrokerPolicy(PolicyCompletenessCompliance.brokerPolicyStressTestResources);
			//pc.validateBrokerPolicy(brokerPolicyPath);
			
			// Get broker policy in Java object structure
			pc.getBrokerPolicy(PolicyCompletenessCompliance.brokerPolicyStressTestResources);

			// Perform completeness check
			pc.validateSDForCompletenessCompliance(PolicyCompletenessCompliance.serviceDescriptionStressTestResources);

			//pc.performStressTest();
			
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	private void performStressTest() 
	{
		/*
		 * For each triple (T) in model:
		 * 		For all three elements (E) in triple (T):
		 * 			1) Make a typo in (E)
		 * 			2) Run validation mechanism. You should get Exception (X).
		 * 			3) if(X)
		 * 					correct typo in (E)
		 * 					continue loop
		 * 			   else
		 * 					report that failure has not been caught.
		 * 			
		 */
		
		// nullify System.out
		System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));
		
		//OntModel cachedModel = this.modelMem;
		//int numOfTriples = this.modelMem.getGraph().size();
		//List<Triple> triplesList = this.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
		//numOfTriples = triplesList.size();
		int problemNumber = 0;
		int okTriples = 0;
		int totalOK = 0;
		
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

		//loadTriples(triplesList, pc);
		
		//InputStream is = convertTriplesToInputStream(pc);
		
			// load BP first
			try {
				pc.addDataToJenaModel(PolicyCompletenessCompliance.brokerPolicyStressTestResources);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//get triples
			List<Triple> bpTriplesList = pc.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
			InputStream bpIs = null;
			System.err.println("Total number of triples: " + bpTriplesList.size());
			System.err.println("Triples changed that did not create problem:");
			for(Triple t:bpTriplesList)
			{
				Triple erroredT = null;
				try {
					// reset model
					pc.modelMem.removeAll();
					// add data
					pc.addDataToJenaModel(PolicyCompletenessCompliance.brokerPolicyStressTestResources);
					// delete triple
					pc.modelMem.getGraph().delete(t);
					// create erroredT
					erroredT = createErroredT(t);
					// add erroredT
					pc.modelMem.getGraph().add(erroredT);
					// convert to InputStream
					bpIs = convertTriplesToInputStream(pc);
					// reset model
					pc.modelMem.removeAll();
					// validate broker policy
					pc.validateBrokerPolicy(bpIs);
					
					// no exception with erroredT, this is a problem
					if(okTriples > 0)
					{
						totalOK += okTriples;
						System.err.println("... in the meantime " + okTriples + " OK ...");
						okTriples = 0;
					}
					System.err.println(++problemNumber + ") " + t);
					/*System.err.println(t);
					System.err.println("to:");
					System.err.println(erroredT);
					System.err.println("did not cause a problem!");
					System.err.println("-------------------------------------------------------------------");
					System.err.println();*/
				} catch (BrokerPolicyException | CompletenessException | ComplianceException e) {
					okTriples++;
				} catch (Exception e) {
					// other exception with erroredT, this is a problem
					if(okTriples > 0)
					{
						totalOK += okTriples;
						System.err.println("... in the meantime " + okTriples + " OK ...");
						okTriples = 0;
					}
					System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
				}
			}

			System.err.println("Total number of triples that caused problem: " + (totalOK + okTriples));
			System.err.println("Total number of triples that did not cause problem: " + problemNumber);
		
		/*// reset model
		pc.modelMem.removeAll();

		// now load SD
		pc.addDataToJenaModel(serviceDescriptionStressTestResources);
		//get triples
		List<Triple> sdTriplesList = pc.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
		// convert to InputStream
		InputStream sdIs = convertTriplesToInputStream(pc);
		// reset model
		pc.modelMem.removeAll();

		// reset and add the bpIs
		bpIs.reset();
		pc.addDataToJenaModel(bpIs);
		// Perform completeness/compliance check
		pc.validateSDForCompletenessCompliance(sdIs);*/

		int i=0;
	}

	private Triple createErroredT(Triple t) {
		Node erroredSubject = createErroredNode(t.getSubject());
		Node erroredPredicate = createErroredNode(t.getPredicate());
		Node erroredObject = createErroredNode(t.getObject());
		Triple erroredT = new Triple(erroredSubject, erroredPredicate, erroredObject);
		return erroredT;
	}

	private Node createErroredNode(Node node) {
		Node erroredNode = null;
		if(node.isLiteral())
		{
			erroredNode = Node.createLiteral(node.toString() + "1");
		}
		else if(node.isURI())
		{
			erroredNode = Node.createURI(node.toString() + "1");
		}
		else if(node.isVariable())
		{
			erroredNode = Node.createVariable(node.toString() + "1");
		}
		return erroredNode;
	}

	private InputStream convertTriplesToInputStream(PolicyCompletenessCompliance pc) {
		ByteArrayOutputStream outA = new ByteArrayOutputStream();
		pc.modelMem.write(outA, "TURTLE");
		InputStream decodedInput=new ByteArrayInputStream(outA.toByteArray());
		return decodedInput;
	}

	private void loadTriples(List<Triple> triplesList,
			PolicyCompletenessCompliance pc) {
		for(Triple t:triplesList)
		{
			//PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
			pc.modelMem.getGraph().add(t);
			// initially set the same model
			//pc.modelMem = cachedModel;
			// delete the current triple
			//pc.modelMem.getGraph().delete(t);
			// add the "errored" with typo triple
			//Triple erroredT = Triple.create(t.getSubject(), t.getPredicate(), t.getObject());

		}
	}
}
