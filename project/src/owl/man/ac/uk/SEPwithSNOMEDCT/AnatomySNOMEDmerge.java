package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * @author Haoruo Zhao
 * ***/

public class AnatomySNOMEDmerge {
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
		File f = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/anatomy_20210718_TR_DL_1000.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology anatomy = manager.loadOntologyFromOntologyDocument(f);
		
		//File snomedFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/SNOMED_2021Jan_TR.owl");
		//OWLOntology snomedOnt = manager.loadOntologyFromOntologyDocument(snomedFile);

		Set<OWLAxiom> axioms = new HashSet();
        axioms.addAll(anatomy.getTBoxAxioms(true));
    	axioms.addAll(anatomy.getRBoxAxioms(true));
    	
    	//axioms.addAll(snomedOnt.getTBoxAxioms(true));
    	//axioms.addAll(snomedOnt.getRBoxAxioms(true));
    	System.out.println(axioms.size());
    	
    	/*File ontFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/SnomedWithModifiedAnatomy.owl");
		
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();

		OWLOntology modifiedAnatomy = manager1.createOntology(axioms);
		manager.saveOntology(modifiedAnatomy, IRI.create(ontFile));*/
	}
	
	
}
