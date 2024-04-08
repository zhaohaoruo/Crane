package owl.man.ac.uk.DivideAndConquer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

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

import owl.man.ac.uk.Crane.AtomUnionWithModule;
import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReADFull.ReADSetting;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ModuleRhapsody {
 
	
	public ModuleRhapsody(Set<AtomUnionWithModule> atomUnionWithModule, Set<OWLAxiom> unclassifiedAxioms, int sizeThreshold
			) throws InterruptedException, ExecutionException {
		
		Set<AtomUnionWithModule> realAtomUnionWithModule = new HashSet();
		
		int moduleFurtherComputationTime = 0;
		int checkingSTsTimes =0;
		for(AtomUnionWithModule a:atomUnionWithModule) {
			
			Set<OWLAxiom> module = a.getModule();
			Set<OWLAxiom> computedAxioms = a.getUnionOfAtoms();

			   
			Set<OWLAxiom> realAtomAxioms = new HashSet(unclassifiedAxioms);
			realAtomAxioms.retainAll(module);
			//System.out.println("this module has atom axioms"+ realAtomAxioms.size());

			if(!realAtomAxioms.isEmpty()) {
				System.out.println("a less than 100 axioms coarsened atoms:" +realAtomAxioms.size() + " with a module size "+module.size());

				if(realAtomAxioms.size()<computedAxioms.size()) {
					// that means part axioms from computedAxioms are checked in other module.
					//so we can zoom out the size of module again.
					module = furtherComputeModule(realAtomAxioms, module);
					moduleFurtherComputationTime++;
					
					
				}
				realAtomUnionWithModule.addAll(furtherModuleRhapsody(realAtomAxioms, module, sizeThreshold));

								
				unclassifiedAxioms.removeAll(module);
				
				if(realAtomUnionWithModule.size()>3000) {
					allClassify(realAtomUnionWithModule);
					realAtomUnionWithModule.clear();
					checkingSTsTimes++;
				}
				System.out.println("finish this part.");

			}
			
        }
		
		System.out.println("we use the parrell system:"+ checkingSTsTimes +" times");

		System.out.println("we further compute module:"+ moduleFurtherComputationTime +" times");
		atomUnionWithModule.clear();
		System.out.println("we now have coarsened atoms with module:"+ realAtomUnionWithModule.size());
		allClassify(realAtomUnionWithModule);
		realAtomUnionWithModule.clear();

	}
	
	public Set<AtomUnionWithModule> furtherModuleRhapsody(Set<OWLAxiom> unclassifiedAxioms, Set<OWLAxiom> remainingModule, int sizeThreshold) 
			throws InterruptedException, ExecutionException{
		
		long currentCPUTime1 = System.currentTimeMillis();		
		CranePAD pad = new CranePAD(unclassifiedAxioms, remainingModule, sizeThreshold);
		
        Set<AtomUnionWithModule> atomUnionWithModule = ForkJoinPool.commonPool().invoke(pad);		
		
		Set<AtomUnionWithModule> realAtomUnionWithModule = new HashSet();
        
		int less1 = 0;
		int morethan90 = 0;
		int middleNum = 0;
		
		for(AtomUnionWithModule a:atomUnionWithModule) {
			
			Set<OWLAxiom> module = a.getModule();
			Set<OWLAxiom> computedAxioms = a.getUnionOfAtoms();

			  
			Set<OWLAxiom> realAtomAxioms = new HashSet(unclassifiedAxioms);
			realAtomAxioms.retainAll(module);

			if(!realAtomAxioms.isEmpty()) {

				if(realAtomAxioms.size()<computedAxioms.size()) {
					// that means part axioms from computedAxioms are checked in other module.
					//so we can zoom out the size of module again.
					module = furtherComputeModule(realAtomAxioms, module);

				}
				
				double per = (realAtomAxioms.size()*100)/module.size();
				if(per>=90) {
					classify(realAtomAxioms, module);
					morethan90++;
				}
				/*else if(per<=1) {
					realAtomUnionWithModule.addAll(getGenuineModules(realAtomAxioms, module));
					less1++;
				}*/
				else {
					realAtomUnionWithModule.add(new AtomUnionWithModule(realAtomAxioms, module));
					middleNum++;
				}				

				unclassifiedAxioms.removeAll(module);

			}
			
        }
        pad = null;
		atomUnionWithModule.clear();
		
		System.out.println("less than 1:"+ less1 +", more than 90:"+morethan90+", middle:"+middleNum);
		long currentCPUTime2 = System.currentTimeMillis();
        long decompositionTime = (currentCPUTime2 - currentCPUTime1)/1000;
		//System.out.println("this module has atom axioms"+ unclassifiedAxioms.size()+" with module "+remainingModule.size());
		System.out.println("decomposition costs:"+ decompositionTime +" s");
		
		return realAtomUnionWithModule;
		
		
	}
	
	/*public void furtherModuleRhapsody(Set<OWLAxiom> unclassifiedAxioms, Set<OWLAxiom> remainingModule, int sizeThreshold) throws InterruptedException, ExecutionException{
		long currentCPUTime1 = System.currentTimeMillis();

		CranePAD pad = new CranePAD(unclassifiedAxioms, remainingModule, sizeThreshold);

		Set<AtomUnionWithModule> realAtomUnionWithModule = furtherModuleRhapsody(unclassifiedAxioms, remainingModule, pad);
		allClassify(realAtomUnionWithModule);
		realAtomUnionWithModule.clear();
		
		long currentCPUTime2 = System.currentTimeMillis();
        long decompositionTime = (currentCPUTime2 - currentCPUTime1)/1000;
		//System.out.println("this module has atom axioms"+ unclassifiedAxioms.size()+" with module "+remainingModule.size());
		System.out.println("decomposition and checking STs costs:"+ decompositionTime +" s");

		//System.gc();
	}*/
	
	private Set<AtomUnionWithModule> getGenuineModules(Set<OWLAxiom> realAtomAxioms, Set<OWLAxiom> module) throws InterruptedException, ExecutionException {
		//long currentCPUTime1 = System.currentTimeMillis();
		
		Set<AtomUnionWithModule> realAtomUnionWithModule = new HashSet();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology dlOnt = null;
		try {
			dlOnt = manager.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);

		Set<OWLAxiom> seeds = new HashSet(realAtomAxioms);
		for(OWLAxiom axiom:realAtomAxioms) {
			if(seeds.contains(axiom)) {
				Set<OWLAxiom> genuineModule = botModExtractor.extract(axiom.getSignature());
				
				Set<OWLAxiom> uncla = new HashSet(seeds);
				uncla.retainAll(genuineModule);
								
				realAtomUnionWithModule.add(new AtomUnionWithModule(uncla, genuineModule));
				seeds.removeAll(genuineModule);
			}
			

		}
		//allClassify(realAtomUnionWithModule);
		//realAtomUnionWithModule.clear();
		dlOnt = null;
		botModExtractor = null;
		
		//long currentCPUTime2 = System.currentTimeMillis();
        //long decompositionTime = (currentCPUTime2 - currentCPUTime1)/1000;
		//System.out.println("this module has atom axioms (per less than 0.1) "+ seeds.size()+" with module "+module.size());
		//System.out.println("decomposition and checking STs costs:"+ decompositionTime +" s");
		
		return realAtomUnionWithModule;
	}
	
	private Set<OWLAxiom> furtherComputeModule(Set<OWLAxiom> realAtomAxioms, Set<OWLAxiom> module) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology dlOnt = null;
		try {
			dlOnt = manager.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
    	module.clear();
    	module = botModExtractor.extract(AxiomsSig.getSigs(realAtomAxioms));
    	botModExtractor = null;
    	dlOnt = null;
    	return module;
	}
	
	public static void allClassify(Set<AtomUnionWithModule> atomsWithModule ) throws InterruptedException, ExecutionException {
		long currentCPUTime1 = System.currentTimeMillis();		

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
        ForkJoinPool forkJoinPool = new ForkJoinPool(cpuCore);
        forkJoinPool.submit(reAD);
        
        int STNumber = reAD.get();
        System.out.println("We checked " +STNumber +" STs");
        forkJoinPool.shutdown();
        keys.clear();
        depen.clear();
        
        long currentCPUTime2 = System.currentTimeMillis();
        long decompositionTime = (currentCPUTime2 - currentCPUTime1)/1000;
		//System.out.println("this module has atom axioms"+ unclassifiedAxioms.size()+" with module "+remainingModule.size());
		System.out.println("checking STs costs:"+ decompositionTime +" s");
	}
	
	private int classify(Set<OWLAxiom> realAtomAxioms, Set<OWLAxiom> module) {
		int num =0;
		Set<OWLClass> cla = new HashSet();
		for(OWLAxiom axiom:realAtomAxioms){
			cla.addAll(axiom.getClassesInSignature());
		}
		ReADSetting reAD = new ReADSetting(cla);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = null;
		try {
			ontology = man.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OWLReasoner reasoner = new Reasoner(new Configuration(), ontology, reAD);			
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		num = reAD.getSTNumber(); 
		ontology = null;
        System.out.println("a single module with " +module.size() +" axioms and "+realAtomAxioms.size()+" checkedAxioms");

		return num;
	}
	
	public static Set<OWLClass> getAllCanS(Set<OWLAxiom> axioms){
		Set<OWLClass> cla = new HashSet();
		for(OWLAxiom axiom:axioms){
			cla.addAll(axiom.getClassesInSignature());
		}
		return cla;
	}
	
	private void findSuitableUnclassifiedAxioms(Set<AtomUnionWithModule> atomUnionWithModule, Set<OWLAxiom> unclassifiedAxioms) {
		
		
		
		Set<OWLAxiom> unclassifiedAxiomsCopy = new HashSet(unclassifiedAxioms);
		Set<AtomUnionWithModule> realAtomUnionWithModule = new HashSet();
		
		int suitableSize = 0;
		Set<OWLAxiom> suitableModule = new HashSet();
		while(!unclassifiedAxiomsCopy.isEmpty()) {
			for(AtomUnionWithModule a:atomUnionWithModule) {
				Set<OWLAxiom> module = a.getModule();
				
				Set<OWLAxiom> realAtomAxioms = new HashSet(unclassifiedAxiomsCopy);
				realAtomAxioms.retainAll(module);
				if(realAtomAxioms.size()>suitableSize) {
					suitableSize = realAtomAxioms.size();
					suitableModule = new HashSet(module);
				}
			}
			Set<OWLAxiom> realAtomAxioms = new HashSet(unclassifiedAxiomsCopy);
			realAtomAxioms.retainAll(suitableModule);
			
			realAtomUnionWithModule.add(new AtomUnionWithModule(realAtomAxioms, suitableModule));
			System.out.println("a less than 100 axioms coarsened atoms" +realAtomAxioms.size() + " with a module size "+suitableModule.size());

			unclassifiedAxiomsCopy.removeAll(suitableModule);
			suitableSize =0;
			suitableModule.clear();
		}
		
		
	}
}
