package owl.man.ac.uk.SnomedCT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
import owl.man.ac.uk.ReADFull.ReADSetting;

public class SNOMED8000ModuleTime {
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
		  for(Set<OWLClass> cla :bigDepen.keySet()){
			  ReADSetting reAD = new ReADSetting(cla);
			  Set<OWLAxiom> axioms = bigDepen.get(cla);
			  Set<OWLClass> sigC = new HashSet();
			  for(OWLAxiom ax:axioms){
				  sigC.addAll(ax.getClassesInSignature());
			  }
			  
			  OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			  OWLOntology ontology = man.createOntology(axioms);
			  
			  long currentCPUTime1 = System.currentTimeMillis();
			  OWLReasoner reasoner = new Reasoner(new Configuration(), ontology, reAD);			
			  reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);			  
			  long currentCPUTime2 = System.currentTimeMillis();
			  long ctTime = currentCPUTime2 - currentCPUTime1;
			  
			  reasoner = null;
			  
			  long currentCPUTime3 = System.currentTimeMillis();
			  OWLReasoner hermit = new Reasoner(new Configuration(), ontology, null);			
			  hermit.precomputeInferences(InferenceType.CLASS_HIERARCHY);	
			  long currentCPUTime4 = System.currentTimeMillis();
			  long hermiTctTime = currentCPUTime4 - currentCPUTime3;
			  
			  hermit = null;
			  String[] row = {					  
					  String.valueOf(axioms.size()),
					  String.valueOf(sigC.size()),
					  String.valueOf(cla.size()),
					  String.valueOf(reAD.getCheckedNumber()),
					  String.valueOf(reAD.getSTNumber()),
					  String.valueOf(ctTime),
					  String.valueOf(hermiTctTime)
			  };
			  writer.writeNext(row);
		  }
		  writer.close();
	        
	}
}
