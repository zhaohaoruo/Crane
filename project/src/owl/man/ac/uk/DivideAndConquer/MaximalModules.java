package owl.man.ac.uk.DivideAndConquer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.Crane.AtomUnionWithModule;
import owl.man.ac.uk.Crane.MOReDecomposer;
import owl.man.ac.uk.ReAD.ReADRecursive;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Haoruo Zhao
 * ***/

public class MaximalModules extends RecursiveTask<Set<Module>> {

	
	/*public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
				
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy7905_20210719.owl");
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithOnlyModifiedAnatomy.owl");
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SNOMED_20200731_DLtest_TR.owl");
		//File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy1000_20210718.owl");
		File f = new File(args[0]);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		long currentCPUTime1 = System.currentTimeMillis();
		MOReDecomposer moreDecomposer = new MOReDecomposer(ontology);

		
		Set<OWLAxiom> remainingModule = new HashSet(moreDecomposer.getMOReRemainingModule());
		Set<OWLAxiom> elModule = new HashSet(moreDecomposer.getMOReELModule());

    	System.out.println("big EL++ module via MORe approach has axioms:"+elModule.size());
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	System.out.println("unclassifiedAxioms part has axioms:"+moreDecomposer.getT2().size());
		
    	
    	
    	long currentCPUTime2 = System.currentTimeMillis();
    	long moreApproachTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("getting the big EL++ module and the remaining module (by MORe code) costs:"+moreApproachTime +"/s");
        
        
        MaximalModules max = new MaximalModules(moreDecomposer.getT2(), new Module(remainingModule), 100);
        
        Set<Module> maximalModules = ForkJoinPool.commonPool().invoke(max);
        System.out.println("we have:"+maximalModules.size()+" genuine modules");
    	long currentCPUTime3 = System.currentTimeMillis();
    	long maxTime = (currentCPUTime3 - currentCPUTime2)/1000;
        System.out.println("getting these genuine modules costs:"+maxTime +"/s");
        max =null;
        
		MaximalModulesManager.pushingELGenuineModulesIntoELModule(maximalModules, moreDecomposer.getELPartInRemainingModule(), elModule);		
        System.out.println("We push EL++ genuine modules into the EL module");

        System.out.println("after that, we still have genuine modules:"+maximalModules.size());
        System.out.println("now the EL++ module has axioms:"+elModule.size());

        MaximalModulesManager.removingNotMaximalGenuineModules(maximalModules);
        System.out.println("after removing not maximal Modules, we have maximal genuine modules:"+maximalModules.size());
        long currentCPUTime4 = System.currentTimeMillis();
        long st = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("dealing with these genuine modules costs:"+st +"/s");
        
        Set<OWLAxiom> unclassiedAxioms = moreDecomposer.getT2();
        unclassiedAxioms.removeAll(elModule);
		Set<OWLClass> claAll = ModuleRhapsody.getAllCanS(unclassiedAxioms);

        MaximalModulesManager.classify(maximalModules, claAll);
	}*/
	
	private Set<OWLAxiom> unclassifiedAxioms;
	private Module module;
	private int sizeThreshold;
	
	public MaximalModules(Set<OWLAxiom> unclassifiedAxioms, Module module, int sizeThreshold) {
		this.unclassifiedAxioms = unclassifiedAxioms;
		this.module = module;
		this.sizeThreshold = sizeThreshold;
	}

	@Override
	protected Set<Module> compute() {
		// TODO Auto-generated method stub
		Set<Module> maximalModules = new HashSet();
		if(unclassifiedAxioms.size()<=sizeThreshold) {
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology dlOnt = null;
			try {
				dlOnt = manager.createOntology(module.getAxioms());
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);

			Set<OWLAxiom> seeds = new HashSet(unclassifiedAxioms);
			for(OWLAxiom axiom:unclassifiedAxioms) {
				if(seeds.isEmpty()) {
					break;
				}
				if(seeds.contains(axiom)) {
					Set<OWLAxiom> genuineModule = botModExtractor.extract(axiom.getSignature());
					
					seeds.removeAll(genuineModule);
					maximalModules.add(new Module(genuineModule));

					//Module genuineM = new Module(genuineModule);
					//MaximalModulesManager.addNewMaximalModule(maximalModules, genuineM);
				}
			}
			dlOnt = null;
			botModExtractor = null;
		}
		else {
			TwoModules split = new TwoModules();
			split.splitModules(unclassifiedAxioms, module);
			
			MaximalModules left = new MaximalModules(split.getUnclaPart1(), split.getM1(), sizeThreshold);
			MaximalModules right = new MaximalModules(split.getUnclaPart2(), split.getM2(), sizeThreshold);
		    left.fork();
	    	right.fork();
	    	//addNewMaximalModule(maximalModules, left.join());
	    	//addNewMaximalModule(maximalModules, right.join());

	    	maximalModules.addAll(left.join());
	    	maximalModules.addAll(right.join());

	    	split = null;
	    	
	    }	
		return maximalModules;
	}
	
	
	
}
