package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
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

import com.opencsv.CSVWriter;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReAD.ReADRecursiveGraph;

public class ReADParallelnoGExp {
	//private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	//private static String depePath = "E:/SNOMED_CT/Storage";

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
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

		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		System.out.println("There are " +bigDepen.size() +" complex modules");
		ReADRecursive reAD = new ReADRecursive(bigDepen, s.getBigAtomSigs(), 0, bigDepen.size(), 7);		
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(reAD);
        System.out.println("We checked " +reAD.get() +" STs");
        forkJoinPool.shutdown();
        
    	long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		System.out.println(ctTime +"ms");
        System.out.println(ctTime/1000 +"/s");	}

}
