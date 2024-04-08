package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Haoruo Zhao
 * ***/

public class MergedOntExpressivity {

	//this file we check that the not modified two ontologies are in DL or EL
	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		File f = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/anatomy_20210128_TR.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology anatomy = manager.loadOntologyFromOntologyDocument(f);
		Set<OWLAxiom> anatomyTR = new HashSet(anatomy.getTBoxAxioms(true));
		anatomyTR.addAll(anatomy.getRBoxAxioms(true));
		
		OWLOntologyManager man1 = OWLManager.createOWLOntologyManager();
		File snomedFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SNOMED_2021Jan_TR.owl");
		OWLOntology snomedOnt = man1.loadOntologyFromOntologyDocument(snomedFile);
		
		anatomyTR.addAll(snomedOnt.getTBoxAxioms(true));
		anatomyTR.addAll(snomedOnt.getRBoxAxioms(true));

		boolean isELK = true;
		for(OWLAxiom axiom:anatomyTR){
			ELKAxiomVisitor vistor = new ELKAxiomVisitor();
			axiom.accept(vistor);
			isELK = vistor.isInFragment();
			if(!isELK){
				System.out.println(axiom);
			}
		}
		
		System.out.println("finish");

	}

}
