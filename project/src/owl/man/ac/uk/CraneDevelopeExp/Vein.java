package owl.man.ac.uk.CraneDevelopeExp;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import owl.man.ac.uk.Crane.MOReDecomposer;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class Vein {

	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy7905_20210719.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
    	
		new Vein(ontology);

	}
	
	
	public Vein(OWLOntology ontology) throws OWLOntologyCreationException{
		MOReDecomposer moreDecomposer = new MOReDecomposer(ontology);
    	
		RemainingT2Manager t2Manager = new RemainingT2Manager(moreDecomposer.getT2());
		
    	System.out.println("t2 has axioms:"+t2Manager.getRemainingT2Axioms().size());
    	
    	Set<OWLAxiom> remainingModule = new HashSet(moreDecomposer.getMOReRemainingModule());
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	System.out.println("******************");

    	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
    	OWLOntology dlOnt = manager.createOntology(remainingModule);
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
         	
    	for(OWLAxiom remainingT2Axiom:moreDecomposer.getT2()){
    		   		
    		if(t2Manager.getRemainingT2Sigs().isEmpty()) {
    			break;
    		}
    		
        	if(t2Manager.getRemainingT2Axioms().contains(remainingT2Axiom)){
        		Set<OWLEntity> seedSigs = oneLeafVeinModule(remainingT2Axiom, t2Manager.getRemainingT2Axioms());   
                Set<OWLAxiom> module = botModExtractor.extract(seedSigs);
                System.out.println(module.size());
                t2Manager.updateViaModule(module);    
                
                remainingModule = botModExtractor.extract(t2Manager.getRemainingT2Sigs());
            	System.out.println("remaining module has axioms:"+remainingModule.size());
            	dlOnt = manager.createOntology(remainingModule);
        	}
               
        }      
	}
	
	
	
	private Set<OWLEntity> oneLeafVeinModule(OWLAxiom axiom, Set<OWLAxiom> t2) {
		SyntacticLocalityEvaluator sle = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
		Set<OWLEntity> seedSigs = new HashSet(axiom.getSignature());
		Set<OWLAxiom> seedAxioms = new HashSet();
		seedAxioms.add(axiom);
		OWLAxiom rootAxiom = axiom;
		
		for(OWLAxiom moduleAxiom: t2) {
			if(sle.isLocal(moduleAxiom, seedSigs)) {
				
				//that means root axiom is not local w.r.t. the signature of module axiom
				/*if(!sle.isLocal(rootAxiom, moduleAxiom.getSignature()
						)){
					seedSigs.addAll(moduleAxiom.getSignature()); 
					seedAxioms.add(moduleAxiom);	
					rootAxiom = moduleAxiom;				
				}*/
				
				//
				if(isInverseNotLocal(moduleAxiom, seedAxioms)) {
					seedSigs.addAll(moduleAxiom.getSignature()); 
					seedAxioms.add(moduleAxiom);	
				}
				
			}
			else {
				//that means the module axiom is non-local w.r.t. seed signature
				seedSigs.addAll(moduleAxiom.getSignature()); 
				seedAxioms.add(moduleAxiom);
			}
		}	
		
		return seedSigs;
	}
	
	private boolean isInverseNotLocal(OWLAxiom moduleAxiom, Set<OWLAxiom> seedAxioms){
		boolean nonLocal = false;
		SyntacticLocalityEvaluator sle = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
		Set<OWLEntity> moduleAxiomSigs = new HashSet(moduleAxiom.getSignature());
		
		for(OWLAxiom seedAxiom: seedAxioms) {
			if(!sle.isLocal(seedAxiom, moduleAxiomSigs)) {
				nonLocal = true;
				break;
			}
			
		}	
		return nonLocal;
	}
}
