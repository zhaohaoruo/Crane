package owl.man.ac.uk.SnomedCT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.opencsv.CSVWriter;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReAD.ReADRecursive;

public class SNOMED8000CTtimeAnalysis {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		
		  System.out.println("begin the experiment ");

		  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		  ADParsing s = new ADParsing(args[0], args[1]);
		  System.out.println("finish ");
		  
		  File outFile = new File("E:/SnomedCT_Extension/SNOMED8000Analysis/ModuleTime.csv");
		  CSVWriter writer = new CSVWriter(new FileWriter(outFile));
		  String[] head = {"moduleSize(#TRBoxAxioms)", 
				  "ModuleConceptNumber", 
				  "CanS size", 
				  "CheckCanS", 
				  "ReADSTNumber", 
				  "ReADModuleTime/ms", 
				  "HermiTModuleTime/ms"};
		  writer.writeNext(head);
		  
		  Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		  ReADRecursive reAD = new ReADRecursive(bigDepen, s.getBigAtomSigs(), 0, bigDepen.size(), 7);		
		  ForkJoinPool forkJoinPool = new ForkJoinPool(8);
		  forkJoinPool.submit(reAD);
		  System.out.println("We checked " +reAD.get() +" STs");
		  forkJoinPool.shutdown();
	        
		  writer.close();
	        
	}

}
