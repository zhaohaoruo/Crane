package owl.man.ac.uk.DivideAndConquer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owl.man.ac.uk.Crane.CoarsenedAtom;
import owl.man.ac.uk.CraneDevelopeExp.HalfSigSelector;
import owl.man.ac.uk.CraneDevelopeExp.RemainingT2Manager;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;


//DC means divide and conquer algorithm
public class CraneDC extends RecursiveTask<Set<CoarsenedAtom>>{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private RemainingT2Manager t2Manager;
	private Set<OWLAxiom> remainingModule;
	private int exampleModuleSize;
	private int exampleSeedSize;
	
	public CraneDC(RemainingT2Manager t2Manager, Set<OWLAxiom> remainingModule, int size, int exampleSeedSize){
		this.t2Manager = t2Manager;
		this.remainingModule = remainingModule;
		this.exampleModuleSize = size;
		this.exampleSeedSize = exampleSeedSize;
	}
	
	
	@Override
	protected Set<CoarsenedAtom> compute() {
		// TODO Auto-generated method stub
		Set<CoarsenedAtom> modules = new HashSet();
		
		if(t2Manager.getRemainingT2Axioms().size()<=exampleSeedSize || remainingModule.size()<= exampleModuleSize) {
			Set<OWLAxiom> seedAxioms = t2Manager.getRemainingT2Axioms();
			if(seedAxioms.size() ==1 ) {
				modules.add(new CoarsenedAtom(remainingModule));

			}
			else {
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				
		    	OWLOntology dlOnt = null;
				try {
					dlOnt = manager.createOntology(remainingModule);
				} catch (OWLOntologyCreationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
		    			    	
		    	for(OWLAxiom axiom:seedAxioms) {
		    		modules.add(
							new CoarsenedAtom(botModExtractor.extract(axiom.getSignature()))
							);
		    	}
			}
			
		}
		else {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
	    	OWLOntology dlOnt = null;
			try {
				dlOnt = manager.createOntology(remainingModule);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
	         	
	    	HalfSigSelector halfSigSelector = new HalfSigSelector(t2Manager.getRemainingT2Axioms());
	    	
	    	Set<OWLEntity> seedSigs = halfSigSelector.getSeedSigs();   	
	        Set<OWLAxiom> module = botModExtractor.extract(seedSigs);
	        
	        Set<OWLAxiom> smallT21 = new HashSet(halfSigSelector.getSelectedAxioms());
	        smallT21.retainAll(module);
	        
	        CraneDC left = new CraneDC(new RemainingT2Manager(smallT21), module, exampleModuleSize, exampleSeedSize);
	        
	        t2Manager.updateViaModule(module);         
	        Set<OWLAxiom> remainingRemainingModule = botModExtractor.extract(t2Manager.getRemainingT2Sigs());	    	
	    	Set<OWLAxiom> smallT22 = new HashSet(t2Manager.getRemainingT2Axioms());
	    	
	    	CraneDC right = new CraneDC(new RemainingT2Manager(smallT22), remainingRemainingModule, exampleModuleSize, exampleSeedSize);
	    	left.fork();
	    	right.fork();
	    	modules.addAll(left.join());
	    	modules.addAll(right.join());
	    
		}
		return modules;
		
	}
	
	

}
