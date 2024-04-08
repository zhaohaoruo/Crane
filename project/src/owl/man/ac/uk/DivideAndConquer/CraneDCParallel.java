package owl.man.ac.uk.DivideAndConquer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.Crane.CoarsenedAtom;
import owl.man.ac.uk.Crane.MOReDecomposer;
import owl.man.ac.uk.CraneDevelopeExp.Bisection;
import owl.man.ac.uk.CraneDevelopeExp.RemainingT2Manager;
import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReADFull.ToolPackage;

public class CraneDCParallel {

	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy7905_20210719.owl");
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithOnlyModifiedAnatomy.owl");
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SNOMED_20200731_DLtest_TR.owl");
		File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy1000_20210718.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		long currentCPUTime1 = System.currentTimeMillis();
		MOReDecomposer moreDecomposer = new MOReDecomposer(ontology);

		
        CraneDCParallel crane = new CraneDCParallel(moreDecomposer);
		long currentCPUTime2 = System.currentTimeMillis();
        long decompositionTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("decomposition time:"+decompositionTime +"/s");

        crane.classify(moreDecomposer.getT2());
		long currentCPUTime3 = System.currentTimeMillis();
        long reasoningTime = (currentCPUTime3 - currentCPUTime2)/1000;
        System.out.println("reasoning time:"+reasoningTime +"/s");

	} 
	
	private Set<CoarsenedAtom> modules;
	
	public CraneDCParallel(MOReDecomposer moreDecomposer) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		this(moreDecomposer, 10);
	}
	
	public CraneDCParallel(MOReDecomposer moreDecomposer, int exampleSeedSize) throws 
	OWLOntologyCreationException, InterruptedException, ExecutionException {
		
		RemainingT2Manager t2Manager = new RemainingT2Manager(moreDecomposer.getT2());	
    	System.out.println("t2 has axioms:"+t2Manager.getRemainingT2Axioms().size());
    	
    	Set<OWLAxiom> remainingModule = new HashSet(moreDecomposer.getMOReRemainingModule());
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	
    	System.out.println("example module size:"+moreDecomposer.getExampleModuleSize(exampleSeedSize));
    	System.out.println("******************");
    	
    	Set<OWLAxiom> OntEL = getELOntologyPart(moreDecomposer.getMOReRemainingModule());

    	
    	CraneDC crane = new CraneDC(t2Manager, remainingModule, moreDecomposer.getExampleModuleSize(exampleSeedSize), exampleSeedSize);
    	
    	modules = ForkJoinPool.commonPool().invoke(crane);
    	System.out.println(modules.size());  

    	checkT2Completeness(moreDecomposer);

    	Set<OWLAxiom> elModules = removeELModules(OntEL);  	
    	System.out.println("after remove el modules, we have:"+modules.size()+" and elmodules:"+elModules.size());  
    	
    	System.out.println("Finish Decomposition");
    	System.out.println("******************");
    	
	}
	
	
	
	
	//we check that the union of all modules contain T2 or not 
	private void checkT2Completeness(MOReDecomposer moreDecomposer){
		Set<OWLAxiom> allModules = new HashSet();
		
		for(CoarsenedAtom module:modules) {
    		allModules.addAll(module.getAxioms());
    	}
		assert allModules.containsAll(moreDecomposer.getT2());
	}
	
	// remove el modules from decomposition and put them together into elModules waiting for ELK
	private Set<OWLAxiom> removeELModules(Set<OWLAxiom> OntEL) {
		Set<OWLAxiom> elModules = new HashSet();
		Set<CoarsenedAtom> ms = new HashSet();
    	for(CoarsenedAtom module:modules) {
    		if(OntEL.containsAll(module.getAxioms())) {
    			ms.add(module);
    			elModules.addAll(module.getAxioms());
    		}
    	}
    	modules.removeAll(ms);  	
    	return elModules;
	}
	
	private Set<OWLAxiom> getELOntologyPart(Set<OWLAxiom> module) {
		Set<OWLAxiom> OntEL = new HashSet();
		for(OWLAxiom axiom:module) {
			if(ToolPackage.isInFragment(axiom)){
				OntEL.add(axiom);
			}
		}
		return OntEL;
	}
	
	public void classify(Set<OWLAxiom> t2) throws InterruptedException, ExecutionException {
		RemainingT2Manager t2Manager = new RemainingT2Manager(t2);	

		Set<OWLClass> remainingCla= t2Manager.getRemainingT2Cla();
		
		readApproach(remainingCla);
    
	}
	
	private void craneApproach(Set<OWLClass> remainingCla) throws InterruptedException, ExecutionException {
		List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
		int threshold = 0;
		for(CoarsenedAtom module:modules) {
			Set<OWLClass> claForModule = module.getAllCanS();
			claForModule.retainAll(remainingCla);
			if(!claForModule.isEmpty()) {
				keys.add(claForModule);
				depen.put(claForModule, module.getAxioms());
				threshold++;
				remainingCla.removeAll(claForModule);
			}
			if(threshold==34) {
				ReADRecursive reAD = new ReADRecursive(depen, keys, 0, depen.size(), 7);		
		        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
		        forkJoinPool.submit(reAD);
		        
		        int STNumber = reAD.get();
		        //System.out.println("We checked " +STNumber +" STs");
		        forkJoinPool.shutdown();
		        keys.clear();
		        depen.clear();	
		        threshold =0;
			}
		}
		
		if(!keys.isEmpty()) {
			ReADRecursive reAD = new ReADRecursive(depen, keys, 0, depen.size(), 7);		
	        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
	        forkJoinPool.submit(reAD);
	        
	        int STNumber = reAD.get();
	        //System.out.println("We checked " +STNumber +" STs");
	        forkJoinPool.shutdown();
		}
	}
	
	private void readApproach(Set<OWLClass> remainingCla) throws InterruptedException, ExecutionException {
		List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
		for(CoarsenedAtom module:modules) {
			Set<OWLClass> claForModule = module.getAllCanS();
			claForModule.retainAll(remainingCla);
			keys.add(claForModule);
			depen.put(claForModule, module.getAxioms());
			remainingCla.removeAll(claForModule);
			
			
		}
        System.out.println("classfication begins...");

		ReADRecursive reAD = new ReADRecursive(depen, keys, 0, depen.size(), 7);		
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(reAD);
        
        int STNumber = reAD.get();
        System.out.println("We checked " +STNumber +" STs");
        forkJoinPool.shutdown();
        keys.clear();
        depen.clear();
	}
}
