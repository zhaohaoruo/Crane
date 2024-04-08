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
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.Crane.AtomUnionWithModule;
import owl.man.ac.uk.Crane.CoarsenedAtom;
import owl.man.ac.uk.Crane.MOReDecomposer;
import owl.man.ac.uk.CraneDevelopeExp.HalfSigSelector;
import owl.man.ac.uk.CraneDevelopeExp.RemainingT2Manager;
import owl.man.ac.uk.ReAD.ReADRecursive;
import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Haoruo Zhao
 * ***/

public class CranePAD extends RecursiveTask<Set<AtomUnionWithModule>> {

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

    	CranePAD pad = new CranePAD(moreDecomposer.getT2(), remainingModule, 100);
        System.out.println("CPU core:" + Runtime.getRuntime().availableProcessors());
        //KeyAxs keyAxs = ForkJoinPool.commonPool().invoke(pad);
        Set<AtomUnionWithModule> atomUnionWithModule = ForkJoinPool.commonPool().invoke(pad);
    	long currentCPUTime3 = System.currentTimeMillis();
       

        long decompositionTime = (currentCPUTime3 - currentCPUTime2)/1000;
        System.out.println("decomposition time:"+decompositionTime +"/s");
        System.out.println("we have "+ atomUnionWithModule.size() +" coarsened atom with its modules.");

        /*Set<OWLAxiom> answerUnclassifiedAxioms = new HashSet();
        for(AtomUnionWithModule a:atomUnionWithModule) {
        	answerUnclassifiedAxioms.addAll(a.getUnionOfAtoms());
            System.out.println("we have coarsened axioms "+ a.getUnionOfAtoms().size());
            System.out.println("with a module "+ a.getModule().size());


        }
        
        if(answerUnclassifiedAxioms.equals(moreDecomposer.getT2())) {
            System.out.println("that's right");

        }*/
        
        pad = null;
        new ModuleRhapsody(atomUnionWithModule, moreDecomposer.getT2(), 10);
        
    	long currentCPUTime4 = System.currentTimeMillis();
    	long claTime = (currentCPUTime4 - currentCPUTime3)/1000;
        System.out.println("all cla time:"+claTime +"/s");
	}

	private Set<OWLAxiom> unclassifiedAxioms;
	private Set<OWLAxiom> module;
	private int sizeThreshold;

	public CranePAD(Set<OWLAxiom> unclassifiedAxioms, Set<OWLAxiom> module, int sizeThreshold) {
		this.unclassifiedAxioms = unclassifiedAxioms;
		this.module = module;
		this.sizeThreshold = sizeThreshold;
	}
	
	@Override
	protected Set<AtomUnionWithModule> compute() {
		// TODO Auto-generated method stub
		//KeyAxs keyAxs = new KeyAxs();
		Set<AtomUnionWithModule> atomsWithModule = new HashSet();		
		if(unclassifiedAxioms.size()<=sizeThreshold) {
			
			atomsWithModule.add(new AtomUnionWithModule(unclassifiedAxioms, module));
		}
		else {
			TwoModules split = new TwoModules();
			split.splitModules(unclassifiedAxioms, module);
			
	    	CranePAD left = new CranePAD(split.getUnclaPart1(), split.getModule1(), sizeThreshold);
		    CranePAD right = new CranePAD(split.getUnclaPart2(), split.getModule2(), sizeThreshold);
		    left.fork();
	    	right.fork();
	    	
	    	atomsWithModule.addAll(left.join()); 
	    	atomsWithModule.addAll(right.join());
	    	split = null;
	    	/*if(atomsWithModule.size()>Runtime.getRuntime().availableProcessors()) {
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
	    		
	    	}	*/
	    }		
		return atomsWithModule;
	}

	
}
