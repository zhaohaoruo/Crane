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
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReAD.ReADRecursiveGraph;
import owl.man.ac.uk.ReAD.ReADRecursiveInferredAxioms;
import owl.man.ac.uk.ReAD.ReADRecursiveWithR;
import owl.man.ac.uk.ReADFull.ReADRecursiveHermiTHierarchy;

/**
 * @author Haoruo Zhao
 * ***/

public class MaximalModulesManager {
	
	public static void addNewMaximalModule(Set<Module> maximalModules, Module genuineM) {
		boolean aNewOne = true;
		
		Set<Module> smallerModules = new HashSet();
		for(Module maximalModule: maximalModules) {
			if(maximalModule.containsModule(genuineM)) {
				aNewOne = false;
				break;
			}
			
			if(genuineM.containsModule(maximalModule)) {
				smallerModules.add(maximalModule);
			}
		}
		if(aNewOne) {
			maximalModules.add(genuineM);
		}
		maximalModules.removeAll(smallerModules);
		smallerModules.clear();
	}
	
	public static  void addNewMaximalModule(Set<Module> maximalModules, Set<Module> genuineModules) {
		for(Module genuineM:genuineModules) {
			addNewMaximalModule(maximalModules, genuineM);
		}
	}
	
	
	public static void pushingELGenuineModulesIntoELModule(Set<Module> maximalModules, Set<OWLAxiom> OntEL, Set<OWLAxiom> elModule) {
        Set<Module> elModules = new HashSet();
        for(Module module: maximalModules) {
        	if(OntEL.containsAll(module.getAxioms())) {
        		elModules.add(module);
        		elModule.addAll(module.getAxioms());
        	}
        }
        maximalModules.removeAll(elModules);
        elModules.clear();
	}
	
	
	public static void removingNotMaximalGenuineModules(Set<Module> modules) {
        Set<Module> fixedMaximalModules = new HashSet();
		
		for(Module module: modules) {
			addNewMaximalModule(fixedMaximalModules, module);
        }
		
		modules.clear();
		modules.addAll(fixedMaximalModules);
		fixedMaximalModules.clear();
	}
	
	
	public static void classify(Set<Module> maximalModules, Set<OWLClass> claAll, int hermiTBatchSize, int recursiveTaskThreshold) throws InterruptedException, ExecutionException {
    	long currentCPUTime3 = System.currentTimeMillis();

		
        int above90 =0;
        int above50Below90 =0;
        int above10Below50 =0;
        int below =0;
        
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
				
			
      
    		/*double per = (cla.size()*100)/m.size();
    		
    		if(per>=90) {
    			above90++;
    		}
    		else if(per>=50) {
    			above50Below90++;
    		}
    		else if(per>=10) {
    			above10Below50++;
    		}
    		else{
    			below++;
    		}*/
    		
    		
    		if(keys.size()>hermiTBatchSize) {
    			System.out.println("we put "+keys.size()+ " modules into a batch");
    			checkingSTs(depen, keys, recursiveTaskThreshold);

    		}
        }

        if(!keys.isEmpty()) {
        	System.out.println("we put "+keys.size()+ " modules into a batch");
        	checkingSTs(depen, keys, recursiveTaskThreshold);
        }
 		

        /*System.out.println(" above90:"+above90);
        System.out.println(" above50Below90:"+above50Below90);

        System.out.println(" above10Below50:"+above10Below50);

        System.out.println(" below10:"+below);*/
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("checking all STs costs:"+st +"/s");
	}
	
	
	private static void checkingSTs(Map<Set<OWLClass>, Set<OWLAxiom>> depen, List<Set<OWLClass>> keys, int recursiveTaskThreshold) 
			throws InterruptedException, ExecutionException {
        long currentCPUTime1 = System.currentTimeMillis();

		int cpuCore = Runtime.getRuntime().availableProcessors();
		ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);    
		System.out.println("classfication begins...");
		ReADRecursive reAD = new ReADRecursive(depen, keys, 0, depen.size(), recursiveTaskThreshold);		
            
		forkJoinPool.submit(reAD);
            
		int STNumber = reAD.get();
        long currentCPUTime2 = System.currentTimeMillis();

		long st = (currentCPUTime2 - currentCPUTime1)/1000;
		System.out.println("We checked " +STNumber +" STs costs:"+st +"/s");
		keys.clear();
		depen.clear();
		forkJoinPool.shutdown();
	}
}
