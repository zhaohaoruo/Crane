package owl.man.ac.uk.Verification;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.ore.verification.ClassHierarchyReducer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class UseORECode {
	public static void main(String[] args) throws OWLOntologyCreationException{
		File f = new File("E:/SNOMED_CT/InferredResults/RemainingModulesInferred.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology remaining = manager.loadOntologyFromOntologyDocument(f);
		Set<OWLAxiom> remainingAxioms = new HashSet();
		remainingAxioms.addAll(remaining.getTBoxAxioms(true));
		remainingAxioms.addAll(remaining.getRBoxAxioms(true));
		
		File f1 = new File("E:/SNOMED_CT/InferredResults/ReADInferred.owl");
		
		File f2 = new File("E:/SNOMED_CT/InferredResults/HermiTInferred.owl");
		
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
		OWLOntology reADaLL = manager1.loadOntologyFromOntologyDocument(f1);
	
		
		
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		OWLOntology hermiT = manager2.loadOntologyFromOntologyDocument(f2);
		
		ClassHierarchyReducer reducer = new ClassHierarchyReducer(reADaLL);
		OWLOntology reADaLLReduced = reducer.createReducedOntology();
		Set<OWLAxiom> reADaLLAxioms = new HashSet();
		reADaLLAxioms.addAll(reADaLLReduced.getTBoxAxioms(true));
		reADaLLAxioms.addAll(reADaLLReduced.getRBoxAxioms(true));
		
		ClassHierarchyReducer reducer1 = new ClassHierarchyReducer(hermiT);
		OWLOntology hermiTReduced = reducer1.createReducedOntology();
		Set<OWLAxiom> hermiTAxioms = new HashSet();
		hermiTAxioms.addAll(hermiTReduced.getTBoxAxioms(true));
		hermiTAxioms.addAll(hermiTReduced.getRBoxAxioms(true));	
		
		System.out.println(reADaLLAxioms.size());
		System.out.println(hermiTAxioms.size());

		if(reADaLLAxioms.equals(hermiTAxioms)){
			System.out.println("they are the same");
		}
	}

}
