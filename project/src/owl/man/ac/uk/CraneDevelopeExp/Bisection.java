package owl.man.ac.uk.CraneDevelopeExp;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.Crane.MOReDecomposer;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class Bisection {

	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy7905_20210719.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
    	
		new Bisection(ontology);

	}

	public Bisection(OWLOntology ontology ) throws OWLOntologyCreationException {
		MOReDecomposer moreDecomposer = new MOReDecomposer(ontology);
    	
		RemainingT2Manager t2Manager = new RemainingT2Manager(moreDecomposer.getT2());
		
    	System.out.println("t2 has axioms:"+t2Manager.getRemainingT2Axioms().size());
    	
    	Set<OWLAxiom> remainingModule = new HashSet(moreDecomposer.getMOReRemainingModule());
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	System.out.println("******************");

    	bisectionModuleExtraction(t2Manager, remainingModule);
		
	}
	
	private void bisectionModuleExtraction(RemainingT2Manager t2Manager, Set<OWLAxiom> remainingModule) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
    	OWLOntology dlOnt = manager.createOntology(remainingModule);
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
         	
    	HalfSigSelector halfSigSelector = new HalfSigSelector(t2Manager.getRemainingT2Axioms());
    	
    	Set<OWLEntity> seedSigs = halfSigSelector.getSeedSigs();   	
        Set<OWLAxiom> module = botModExtractor.extract(seedSigs);
        
        int i=0;
        for(OWLAxiom axiom:module) {
        	if(t2Manager.getRemainingT2Axioms().contains(axiom)) {
        		i++;
        	}
        }
        System.out.println("we use: "+ halfSigSelector.getSelectedAxioms().size()+" axioms for module extraction and get "
        		+i +" axioms in T2");    

        
        Set<OWLAxiom> smallT21 = new HashSet(halfSigSelector.getSelectedAxioms());
        smallT21.retainAll(module);
        
        System.out.println("module has axioms:"+module.size());    
        t2Manager.updateViaModule(module);         
       
    	
    	if(module.size()>2000&&smallT21.size()>50) {
            System.out.println("t21 size:"+smallT21.size());    

        	bisectionModuleExtraction(new RemainingT2Manager(smallT21), module);
        	
    	}
    	
    	Set<OWLAxiom> remainingRemainingModule = botModExtractor.extract(t2Manager.getRemainingT2Sigs());
    	System.out.println("remaining module has axioms:"+remainingRemainingModule.size());
    	
    	Set<OWLAxiom> smallT22 = new HashSet(t2Manager.getRemainingT2Axioms());
    	
    	if(remainingRemainingModule.size()>2000&&smallT22.size()>50) {
    		System.out.println("t22 size:"+smallT22.size());    
    		
        	bisectionModuleExtraction(new RemainingT2Manager(smallT22), remainingRemainingModule);
        	
    	}
    	t2Manager = null;
    	dlOnt = null;
    	manager = null;
	}

}
