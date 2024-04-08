package owl.man.ac.uk.ReADFull;

import java.io.File;
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

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.OntStorage.ADStorage;
import owl.man.ac.uk.ReAD.ReADRecursiveGraph;


public class GraphADFull {

	private IdentityMultiMap<OWLClass, OWLClass> reADMaps;
	
	public GraphADFull(ADParsing s) throws OWLOntologyCreationException, InterruptedException, ExecutionException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		/*File f = new File("E:/SNOMED_CT/DLOnt/SNOMED_20200731_DLtest_TR.owl");
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(f);
		Set<OWLClass> clas = ontology.getClassesInSignature(true);	*/	
		
		long currentCPUTime1 = System.currentTimeMillis();
        OWLOntology elOnt = manager.createOntology(s.getELModules());
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		IdentityMultiMap<OWLClass, OWLClass> elkG = new IdentityMultiMap();
		for(OWLClass atomic:elOnt.getClassesInSignature(true)){
			Set<OWLClass> superClass = new HashSet();
			superClass.addAll(elkReasoner.getSuperClasses(atomic, false).getFlattened());	
			superClass.addAll(elkReasoner.getEquivalentClasses(atomic).getEntities());
			elkG.putAll(atomic, superClass);
		}
		
		int i = 0;
		for(OWLClass cla:elkG.keySet()){
			i = i+elkG.get(cla).size();
		}
		System.out.println("el++part size:"+i);
		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		ReADRecursiveGraph reAD = new ReADRecursiveGraph(bigDepen, s.getBigAtomSigs(), 0, bigDepen.size(), 7);		
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(reAD);
        reADMaps = reAD.get();
        forkJoinPool.shutdown();
        reADMaps.putAll(elkG);
    	long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		System.out.println(ctTime +"ms");
        System.out.println(ctTime/1000 +"/s");
	}
	
	public IdentityMultiMap<OWLClass, OWLClass> getReADG(){
		return reADMaps;
	}
}
