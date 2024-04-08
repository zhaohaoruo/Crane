package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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

public class buildAnatomyWithRewrite {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		// TODO Auto-generated method stub
		File f = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/anatomy_20210128_TR.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology anatomy = manager.loadOntologyFromOntologyDocument(f);
		
		AnatomyConverter converter = new AnatomyConverter(anatomy, manager.getOWLDataFactory());
		
		Set<OWLAxiom> axioms = new HashSet(converter.getNotModifiedAxioms());
		axioms.addAll(converter.getModifiedAxioms());
		
		File ontFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/anatomy_20210128_TR_DL_modifiedPart.owl");
		
		OWLOntology modifiedAnatomy = manager.createOntology(axioms);
		manager.saveOntology(modifiedAnatomy, IRI.create(ontFile));
	}

}
