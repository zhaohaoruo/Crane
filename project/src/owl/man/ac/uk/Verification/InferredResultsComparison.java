package owl.man.ac.uk.Verification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class InferredResultsComparison {
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
		Set<OWLAxiom> reADaLLAxioms = new HashSet();
		reADaLLAxioms.addAll(reADaLL.getTBoxAxioms(true));
		reADaLLAxioms.addAll(reADaLL.getRBoxAxioms(true));
		
		
		OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		OWLOntology hermiT = manager2.loadOntologyFromOntologyDocument(f2);
		Set<OWLAxiom> hermiTAxioms = new HashSet();
		hermiTAxioms.addAll(hermiT.getTBoxAxioms(true));
		hermiTAxioms.addAll(hermiT.getRBoxAxioms(true));
		
		if(hermiTAxioms.equals(reADaLLAxioms)){
			System.out.println("they are the same");
		}
		
		System.out.println(reADaLLAxioms.size());

		System.out.println(hermiTAxioms.size());
		//hermiTAxioms.removeAll(remainingAxioms);
		System.out.println(remainingAxioms.size());
		
		hermiTAxioms.retainAll(remainingAxioms);
		remainingAxioms.removeAll(hermiTAxioms);
		System.out.println(remainingAxioms.size());
		
		
		
	}
	
	
}
