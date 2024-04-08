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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.Crane.AtomUnionWithModule;
import owl.man.ac.uk.Crane.MOReDecomposer;
import owl.man.ac.uk.CraneDevelopeExp.HalfSigSelector;
import owl.man.ac.uk.ReAD.ReADRecursive;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class CoarsenedAtomsComputation {

	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException, ExecutionException {
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
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	System.out.println("unclassifiedAxioms has axioms:"+moreDecomposer.getT2().size());
		
    	
    	
    	long currentCPUTime2 = System.currentTimeMillis();
    	long moreApproachTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("more ApproachTime time:"+moreApproachTime +"/s");

        CoarsenedAtomsComputation pad = new CoarsenedAtomsComputation(moreDecomposer.getT2(), remainingModule);
        System.out.println("CPU core:" + Runtime.getRuntime().availableProcessors());
        //KeyAxs keyAxs = ForkJoinPool.commonPool().invoke(pad);
        Set<AtomUnionWithModule> atomUnionWithModule = pad.computeUnionAtoms();
    	if(atomUnionWithModule.size()>0) {
            System.out.println("we have "+ atomUnionWithModule.size() +" coarsened atom with its modules.");
            pad.quickClassify(atomUnionWithModule );
    	}
        long currentCPUTime3 = System.currentTimeMillis();
       

        long decompositionTime = (currentCPUTime3 - currentCPUTime2)/1000;
        System.out.println("decomposition time:"+decompositionTime +"/s");

        
	}

	private Set<OWLAxiom> unclassifiedAxioms;
	private Set<OWLAxiom> module;
	
	
	public CoarsenedAtomsComputation(Set<OWLAxiom> unclassifiedAxioms, Set<OWLAxiom> module) {
		this.unclassifiedAxioms = unclassifiedAxioms;
		this.module = module;
	}
	
	public Set<AtomUnionWithModule> computeUnionAtoms(){
		Set<AtomUnionWithModule> atomsWithModule = new HashSet();		
		if(unclassifiedAxioms.size()<=10) {
			
			atomsWithModule.add(new AtomUnionWithModule(unclassifiedAxioms, module));
		}
		else {
			TwoModules split = new TwoModules();
			split.splitModules(unclassifiedAxioms, module);
	    	CoarsenedAtomsComputation left = new CoarsenedAtomsComputation(split.getUnclaPart1(), split.getModule1());
	    	CoarsenedAtomsComputation right = new CoarsenedAtomsComputation(split.getUnclaPart2(), split.getModule2());		    			    	
		    	
		    atomsWithModule.addAll(left.computeUnionAtoms()); 
		    atomsWithModule.addAll(right.computeUnionAtoms());
		    split = null;
		    left =null;
		    right =null;
		    if(atomsWithModule.size()>(Runtime.getRuntime().availableProcessors()*100)) {
			    	//System.gc();
		    	System.out.println(atomsWithModule.size());
		    	try {
					quickClassify(atomsWithModule );
				} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	atomsWithModule.clear();
		    		
		    }		    		 	    	 	    	 		
		}		
		return atomsWithModule;
	}
	
	public void quickClassify(Set<AtomUnionWithModule> atomsWithModule ) throws InterruptedException, ExecutionException {
		List<Set<OWLClass>> keys =  new ArrayList();
		Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
		
		Set<OWLClass> classifiedCla = new HashSet();
		for(AtomUnionWithModule atomUnionWithModule:atomsWithModule) {
			Set<OWLClass> can = atomUnionWithModule.getAllCanS();
			can.removeAll(classifiedCla);
			keys.add(can);
			depen.put(can, atomUnionWithModule.getModule());
			classifiedCla.addAll(can);		
		}
        System.out.println("classfication begins...");
        int cpuCore = Runtime.getRuntime().availableProcessors();
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
