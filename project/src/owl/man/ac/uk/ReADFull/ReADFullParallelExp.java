package owl.man.ac.uk.ReADFull;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReAD.ReADRecursive;

public class ReADFullParallelExp {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		 System.out.println("begin the experiment.");
		ADParsing s = new ADParsing(atomPath, depePath);
		GraphADFull g = new GraphADFull(s);
		IdentityMultiMap<OWLClass, OWLClass> elkG = g.getReADG();
		int i = 0;
		for(OWLClass cla:elkG.keySet()){
			i = i+elkG.get(cla).size();
		}
		System.out.println("reAD-Full:" +i);
	}

}
