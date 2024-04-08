package owl.man.ac.uk.SnomedCT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OverviewForSNOMED {

	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		File file = new File(args[0]);
		
		
		for(File f:file.listFiles()){
			System.out.println(f.getName());
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);	
			
			List<OWLAxiom> traxioms = new ArrayList();
			traxioms.addAll(ontology.getTBoxAxioms(true));
			traxioms.addAll(ontology.getRBoxAxioms(true));
	    	
    		System.out.println("ontology has "+traxioms.size()+" axioms");

    		Set<OWLAxiom> number = new HashSet(traxioms);
    		System.out.println("ontology has "+number.size()+" axioms");
		}
	}

}
