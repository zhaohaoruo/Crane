package owl.man.ac.uk.Verification;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ReasonerForEntailment {
	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		File f = new File("E:/SNOMED_CT/InferredResults/RemainingModulesInferred.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology remaining = manager.loadOntologyFromOntologyDocument(f);
		
		
		File f1 = new File("E:/SNOMED_CT/InferredResults/ReADInferred.owl");
		
		
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
		OWLOntology reADaLL = manager1.loadOntologyFromOntologyDocument(f1);
	
		
		
		
		Set<OWLAxiom> reADinferredAxioms = getELKInferredRelation(reADaLL);	

		File f2 = new File("E:/SNOMED_CT/InferredResults/HermiTInferred.owl");
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		OWLOntology hermiT = manager2.loadOntologyFromOntologyDocument(f2);
		Set<OWLAxiom> hermiTferredAxioms = getELKInferredRelation(hermiT);	
		
		Set<OWLAxiom> remainingAxioms = getELKInferredRelation(remaining);
		
		System.out.println(hermiTferredAxioms.size());
		System.out.println(reADinferredAxioms.size());
		

		if(reADinferredAxioms.containsAll(hermiTferredAxioms)){
			System.out.println("it contains");
		}
		Set<OWLAxiom> reADRedudantAxiom = new HashSet(reADinferredAxioms);
		reADinferredAxioms.retainAll(hermiTferredAxioms);
		System.out.println("we have same inferrence " + reADinferredAxioms.size());
		reADRedudantAxiom.removeAll(reADinferredAxioms);
		for(OWLAxiom ax:reADRedudantAxiom){
			System.out.println("reAD NOT same axiom " + ax);

		}
		hermiTferredAxioms.removeAll(reADinferredAxioms);
		for(OWLAxiom ax:hermiTferredAxioms){
			System.out.println("hermiT NOT same axiom " + ax);

		}
		

	}
	
	public static Set<OWLAxiom> getELKInferredRelation(OWLOntology ont) throws OWLOntologyCreationException{
		OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(ont);
		
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		OWLOntologyManager manager3 = OWLManager.createOWLOntologyManager();

		Set<OWLAxiom> inferredAxioms = ReADCorrectness.getInferredAxioms(manager3, elkReasoner);
		return inferredAxioms;
	}

}
