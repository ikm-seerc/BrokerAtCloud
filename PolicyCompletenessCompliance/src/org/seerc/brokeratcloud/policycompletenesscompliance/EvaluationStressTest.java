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

	String[] elementsThatMakeTriplesIgnored = 
		{
			"@rdfs:label",
			"@http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter",
			"@http://www.broker-cloud.eu/service-descriptions/CAS/broker#measuredBy",
			"@http://purl.org/goodrelations/v1#lesser",
			"@http://purl.org/goodrelations/v1#greater",
			"@http://www.w3.org/2004/02/skos/core#inScheme",
			"@http://www.w3.org/2004/02/skos/core#broader",
			"@http://www.w3.org/2004/02/skos/core#altLabel",
			"@http://www.w3.org/2004/02/skos/core#prefLabel",
			"@rdfs:comment",
			"@http://purl.org/goodrelations/v1#taxID",
			"@http://purl.org/goodrelations/v1#legalName",
			"@http://xmlns.com/foaf/0.1/logo",
			"@http://xmlns.com/foaf/0.1/homepage",
			"@http://www.w3.org/2004/02/skos/core#narrower",
			"@http://www.w3.org/2004/02/skos/core#altLabel",
			"@http://www.w3.org/2004/02/skos/core#prefLabel",
			"@owl:versionInfo",
			"@http://purl.org/dc/terms/modified",
			"@http://purl.org/dc/terms/created",
			"@http://purl.org/dc/terms/description",
			"@http://purl.org/dc/terms/title",
			"owl:Ontology",
			"/broker-pref-attr#",
			"@http://www.w3.org/2004/02/skos/core#topConceptOf",
			"@http://www.w3.org/2004/02/skos/core#skos:prefLabel",
			"@http://purl.org/dc/terms/publisher",
			"@http://purl.org/dc/terms/creator",
			"@http://www.w3.org/2004/02/skos/core#hasTopConcept"
		};
	
	private static Object[] bpResources = PolicyCompletenessCompliance.brokerPolicyStressTestResources;
	private static Object[] sdResources = PolicyCompletenessCompliance.serviceDescriptionStressTestResources;
	
	public static void main(String[] args) {
		EvaluationStressTest est = new EvaluationStressTest();
		est.normalEvaluate();
		est.performStressTest();
	}

	private void normalEvaluate()
	{
		try {
			PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

			// validate broker policy first
			pc.validateBrokerPolicy(bpResources);
			//pc.validateBrokerPolicy(brokerPolicyPath);
			
			// Get broker policy in Java object structure
			pc.getBrokerPolicy(bpResources);

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
		int totalIgnored = 0;
		
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

		//loadTriples(triplesList, pc);
		
		//InputStream is = convertTriplesToInputStream(pc);
		
			// load BP first
			try {
				pc.addDataToJenaModel(bpResources);
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
				if(tripleShouldBeIgnored(t))
				{
					//System.err.println("Ignoring " + t);
					totalIgnored++;
					continue;
				}
				
				Triple erroredT = null;
				try {
					// reset model
					pc.modelMem.removeAll();
					// add data
					pc.addDataToJenaModel(bpResources);
					// delete triple
					pc.modelMem.getGraph().delete(t);
					// create erroredSubjectT
					erroredT = createErroredSubjectT(t);
					// add erroredT
					pc.modelMem.getGraph().add(erroredT);
					// convert to InputStream
					bpIs = convertTriplesToInputStream(pc);
					// reset model
					pc.modelMem.removeAll();
					// validate broker policy
					pc.validateBrokerPolicy(bpIs);
					
					// reset model
					pc.modelMem.removeAll();
					// add data
					pc.addDataToJenaModel(bpResources);
					// delete triple
					pc.modelMem.getGraph().delete(t);
					// create erroredPredicateT
					erroredT = createErroredPredicateT(t);
					// add erroredT
					pc.modelMem.getGraph().add(erroredT);
					// convert to InputStream
					bpIs = convertTriplesToInputStream(pc);
					// reset model
					pc.modelMem.removeAll();
					// validate broker policy
					pc.validateBrokerPolicy(bpIs);
					
					// reset model
					pc.modelMem.removeAll();
					// add data
					pc.addDataToJenaModel(bpResources);
					// delete triple
					pc.modelMem.getGraph().delete(t);
					// create erroredObjectT
					erroredT = createErroredObjectT(t);
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
						//System.err.println("... in the meantime " + okTriples + " OK ...");
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
						//System.err.println("... in the meantime " + okTriples + " OK ...");
						okTriples = 0;
					}
					System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
					e.printStackTrace();
				}
			}

			System.err.println("Total number of triples that caused problem: " + (totalOK + okTriples));
			System.err.println("Total number of triples that did not cause problem: " + problemNumber);
			System.err.println("Total number of triples ignored: " + totalIgnored);
		
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

	private boolean tripleShouldBeIgnored(Triple t)
	{
		for(String element:elementsThatMakeTriplesIgnored)
		{
			if(t.toString().contains(element))
			{
				return true;				
			}
		}
		
		return false;
	}

	private Triple createErroredSubjectT(Triple t) {
		Node erroredSubject = createErroredNode(t.getSubject());
		Triple erroredT = new Triple(erroredSubject, t.getPredicate(), t.getObject());
		return erroredT;
	}

	private Triple createErroredPredicateT(Triple t) {
		Node erroredPredicate = createErroredNode(t.getPredicate());
		Triple erroredT = new Triple(t.getSubject(), erroredPredicate, t.getObject());
		return erroredT;
	}

	private Triple createErroredObjectT(Triple t) {
		Node erroredObject = createErroredNode(t.getObject());
		Triple erroredT = new Triple(t.getSubject(), t.getPredicate(), erroredObject);
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
