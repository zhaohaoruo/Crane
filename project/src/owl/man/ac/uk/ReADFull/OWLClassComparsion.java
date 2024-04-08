package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.OntStorage.ADParsing;

public class OWLClassComparsion {
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		File f = new File("E:/SNOMED_CT/ELOnt/SNOMED_20200731_TR.owl");
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		
        
        
		File file = new File("E:/SNOMED_CT/DLOnt/SNOMED_20200731_DLtest_TR.owl");
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		OWLOntology dlont = manager2.loadOntologyFromOntologyDocument(file);
		
		Set<OWLClass> clas = dlont.getClassesInSignature(true);
		System.out.println("parsing finished");
        for(OWLClass cla:ontology.getClassesInSignature(true)){
        	if(!clas.contains(cla)){
        		System.out.println(cla);
        	}
        }
	}
}
