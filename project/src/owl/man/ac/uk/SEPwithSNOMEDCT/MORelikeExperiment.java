package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReAD.ReADRecursive;

/*******
 * this one is for getting a el module (union of all possible el modules)
 * and a complex module (union of all possible complex modules) and classifying them one by one.
 * 
 * *****************/

public class MORelikeExperiment {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		// TODO Auto-generated method stub
		 System.out.println("begin the experiment ");

		 OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		 ADParsing s = new ADParsing(args[0], args[1]);
		 System.out.println("finish ");

		 long currentCPUTime1 = System.currentTimeMillis();
		 OWLOntology elOnt = manager.createOntology(s.getELModules());
		 manager = null;
		 OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		 elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		 Set<OWLAxiom> complexModules = new HashSet();
		 for(Set<OWLAxiom> module:s.getbigMaps().values()){
			 complexModules.addAll(module);			  
		 }
	     
		 OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		 OWLOntology dlOnt = man.createOntology(complexModules);
		 
		 OWLReasoner reasoner = new Reasoner(new Configuration(), dlOnt, null);			
		 reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		 
		 long currentCPUTime3 = System.currentTimeMillis();
		 long ctTime = currentCPUTime3 - currentCPUTime1;
		 System.out.println(ctTime +"ms");
		 System.out.println(ctTime/1000 +"/s");	
	}

}
