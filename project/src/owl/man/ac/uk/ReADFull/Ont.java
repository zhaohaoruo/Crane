package owl.man.ac.uk.ReADFull;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Ont {
	private OWLOntology dlont;
	
	public Ont(String s ) throws OWLOntologyCreationException{
		File file = new File(s);
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
		dlont = manager2.loadOntologyFromOntologyDocument(file);
	}
	
	public OWLOntology getOnt(){
		return dlont;
	}
}
