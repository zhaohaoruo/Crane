package owl.man.ac.uk.DivideAndConquer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.DeterministicClassification;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.DeterministicClassification.GraphNode;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.ReAD.ReADRecursiveGraph;
import owl.man.ac.uk.ReAD.ReADRecursiveInferredAxioms;
import owl.man.ac.uk.ReAD.ReADRecursiveWithR;
import owl.man.ac.uk.ReADFull.ReADRecursiveHermiTHierarchy;

/**
 * @author Haoruo Zhao
 * ***/

public class HierarchyStorage {

	public static Set<OWLAxiom> getGeneratedAxioms(Set<Module> maximalModules, Set<OWLClass> claAll, int hermiTBatchSize, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException{
		long currentCPUTime3 = System.currentTimeMillis();
		
		Set<OWLAxiom> axioms = new HashSet();
		
        List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
            
        for(Module module: maximalModules) {
        	
        	Set<OWLClass> can = ModuleRhapsody.getAllCanS(module.getAxioms());
			can.retainAll(claAll);
			if(!can.isEmpty()) {
				keys.add(can);
				depen.put(can, module.getAxioms());
				claAll.removeAll(can);
			}
							          		    	   		
    		if(keys.size()>hermiTBatchSize) {
    			axioms.addAll(getInferredAxioms(depen, keys, recursiveTaskThreshold));

    		}
        }

        if(!keys.isEmpty()) {
        	axioms.addAll(getInferredAxioms(depen, keys, recursiveTaskThreshold));
        } 		
        
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("checking all STs costs:"+st +"/s");
        
        return axioms;
	}
	
	public static IdentityMultiMap<OWLClass, OWLClass> getKnownRelation(Set<Module> maximalModules, Set<OWLClass> claAll, int hermiTBatchSize, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException{
		long currentCPUTime3 = System.currentTimeMillis();
		
		IdentityMultiMap<OWLClass, OWLClass> maps = new IdentityMultiMap<OWLClass, OWLClass>();
		
        List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
            
        for(Module module: maximalModules) {
        	
        	Set<OWLClass> can = ModuleRhapsody.getAllCanS(module.getAxioms());
			can.retainAll(claAll);
			if(!can.isEmpty()) {
				keys.add(can);
				depen.put(can, module.getAxioms());
				claAll.removeAll(can);
			}
							          		    	   		
    		if(keys.size()>hermiTBatchSize) {
    			System.out.println("we put "+keys.size()+ " modules into a batch");
    			maps.putAll(getSuperClasses(depen, keys, recursiveTaskThreshold));

    		}
        }

        if(!keys.isEmpty()) {
			System.out.println("we put "+keys.size()+ " modules into a batch");
        	maps.putAll(getSuperClasses(depen, keys, recursiveTaskThreshold));
        } 		
        
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("checking all STs costs:"+st +"/s");
        
        return maps;
	}
	
	public static Hierarchy<AtomicConcept> getHermiTHierarchy(Set<Module> maximalModules, Set<OWLClass> claAll, int hermiTBatchSize, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException{
		
		long currentCPUTime3 = System.currentTimeMillis();
		
		Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers = new HashMap();
		        
        List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
            
        for(Module module: maximalModules) {
        	
        	Set<OWLClass> can = ModuleRhapsody.getAllCanS(module.getAxioms());
			can.retainAll(claAll);
			if(!can.isEmpty()) {
				keys.add(can);
				depen.put(can, module.getAxioms());
				claAll.removeAll(can);
			}
							          		    	   		
    		if(keys.size()>hermiTBatchSize) {
    			allSubsumers.putAll(getAllSubsumers(depen, keys, recursiveTaskThreshold));

    		}
        }

        if(!keys.isEmpty()) {
        	allSubsumers.putAll(getAllSubsumers(depen, keys, recursiveTaskThreshold));
        } 		
        
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("checking all STs costs:"+st +"/s");
                
        return DeterministicClassification.buildHierarchy(AtomicConcept.THING,AtomicConcept.NOTHING,allSubsumers);
	}
	
	public static Set<Reasoner> getModifiedReasoners(Set<Module> maximalModules, Set<OWLClass> claAll, int hermiTBatchSize, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException{
		
		long currentCPUTime3 = System.currentTimeMillis();
		
		Set<Reasoner> reasoners = new HashSet();
		        
        List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
            
        for(Module module: maximalModules) {
        	
        	Set<OWLClass> can = ModuleRhapsody.getAllCanS(module.getAxioms());
			can.retainAll(claAll);
			if(!can.isEmpty()) {
				keys.add(can);
				depen.put(can, module.getAxioms());
				claAll.removeAll(can);
			}
							          		    	   		
    		if(keys.size()>hermiTBatchSize) {
    			System.out.println("we put "+keys.size()+ " modules into a batch");
    			reasoners.addAll(getReasoners(depen, keys, recursiveTaskThreshold));

    		}
        }

        if(!keys.isEmpty()) {
        	System.out.println("we put "+keys.size()+ " modules into a batch");
			reasoners.addAll(getReasoners(depen, keys, recursiveTaskThreshold));
        } 		
        
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("checking all STs costs:"+st +"/s");
        
        return reasoners;
	}
	
	private static Map<AtomicConcept,GraphNode<AtomicConcept>> getAllSubsumers(Map<Set<OWLClass>, Set<OWLAxiom>> depen, List<Set<OWLClass>> keys, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException {
		long currentCPUTime1 = System.currentTimeMillis();

		int cpuCore = Runtime.getRuntime().availableProcessors();
		ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);    
		System.out.println("classfication begins...");
		ReADRecursiveHermiTHierarchy reAD = new ReADRecursiveHermiTHierarchy(depen, keys, 0, depen.size(), recursiveTaskThreshold);		
            
		forkJoinPool.submit(reAD);
            
		Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers = reAD.get();
        long currentCPUTime2 = System.currentTimeMillis();

		long st = (currentCPUTime2 - currentCPUTime1)/1000;
		System.out.println("it costs:"+st +"/s");
		keys.clear();
		depen.clear();
		forkJoinPool.shutdown();
		
		return allSubsumers;
	}
	
	private static IdentityMultiMap<OWLClass, OWLClass> getSuperClasses(Map<Set<OWLClass>, Set<OWLAxiom>> depen, List<Set<OWLClass>> keys, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException {
        long currentCPUTime1 = System.currentTimeMillis();

		int cpuCore = Runtime.getRuntime().availableProcessors();
		ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);    
		System.out.println("classfication begins...");
		ReADRecursiveGraph reAD = new ReADRecursiveGraph(depen, keys, 0, depen.size(), recursiveTaskThreshold);		
            
		forkJoinPool.submit(reAD);
            
		IdentityMultiMap<OWLClass, OWLClass> inferredHierarchy = reAD.get();
        long currentCPUTime2 = System.currentTimeMillis();

		long st = (currentCPUTime2 - currentCPUTime1)/1000;
		System.out.println("it costs:"+st +"/s");

		keys.clear();
		depen.clear();
		forkJoinPool.shutdown();
		
		return inferredHierarchy;
	}
	
	private static Set<Reasoner> getReasoners(Map<Set<OWLClass>, Set<OWLAxiom>> depen, List<Set<OWLClass>> keys, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException{
		
		long currentCPUTime1 = System.currentTimeMillis();

		int cpuCore = Runtime.getRuntime().availableProcessors();
		ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);    
		System.out.println("classfication begins...");
		ReADRecursiveWithR reAD = new ReADRecursiveWithR(depen, keys, 0, depen.size(), recursiveTaskThreshold);		
            
		forkJoinPool.submit(reAD);
            
		Set<Reasoner> reasoners = new HashSet(reAD.get());
        long currentCPUTime2 = System.currentTimeMillis();

		long st = (currentCPUTime2 - currentCPUTime1)/1000;
		
		int STNumber = 0;
		for(Reasoner reasoner:reasoners) {
			STNumber = STNumber+ reasoner.getReADSetting().getSTNumber();
		}
		
		System.out.println("We checked " +STNumber +" STs costs:"+st +"/s");
		keys.clear();
		depen.clear();
		forkJoinPool.shutdown();
		
		return reasoners;
	}
	
	private static Set<OWLAxiom> getInferredAxioms(Map<Set<OWLClass>, Set<OWLAxiom>> depen, List<Set<OWLClass>> keys, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException {
        long currentCPUTime1 = System.currentTimeMillis();

		int cpuCore = Runtime.getRuntime().availableProcessors();
		ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);    
		System.out.println("classfication begins...");
		ReADRecursiveInferredAxioms reAD = new ReADRecursiveInferredAxioms(depen, keys, 0, depen.size(), recursiveTaskThreshold);		
                        
		Set<OWLAxiom> axioms = reAD.get();
        long currentCPUTime2 = System.currentTimeMillis();

		long st = (currentCPUTime2 - currentCPUTime1)/1000;
		System.out.println("it costs:"+st +"/s");

		keys.clear();
		depen.clear();
		forkJoinPool.shutdown();
		
		return axioms;
	}
	
}
