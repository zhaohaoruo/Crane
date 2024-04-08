package owl.man.ac.uk.SnomedCT;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.OntStorage.ADParsing;

public class SNOMED8000Analysis {
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		
		  System.out.println("begin the experiment ");

		  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		  ADParsing s = new ADParsing(args[0], args[1]);
		  System.out.println("finish ");
		  
		  //question 2 what's the percentage of EL Modules
		  Set<OWLAxiom> elaxioms = s.getELModules();
		  System.out.println("EL Modules size:" + elaxioms.size());
		  
		  System.out.println("we have complex modules number:" +s.getbigMaps().keySet().size());
		  Set<OWLAxiom> complexModules = new HashSet();
		  for(Set<OWLAxiom> module:s.getbigMaps().values()){
			  complexModules.addAll(module);			  
		  }
		  System.out.println("complexModules size:" +complexModules.size());

		  complexModules.addAll(elaxioms);
		  System.out.println("whole ontology has TBox axioms:" +complexModules.size());

		  long per = (elaxioms.size()*100)/(complexModules.size());
		  System.out.println("the percentage of EL Modules:" +per);
		  
		  
	        
	}
}
