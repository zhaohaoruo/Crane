package owl.man.ac.uk.DivideAndConquer;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.CraneDevelopeExp.HalfSigSelector;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Haoruo Zhao
 * ***/

public class TwoModules {

	private Set<OWLAxiom> unclaPart1;
	private Set<OWLAxiom> unclaPart2;
	private Set<OWLAxiom> module1;
	private Set<OWLAxiom> module2;

	public Set<OWLAxiom> getUnclaPart1(){
		return unclaPart1;
	}
	
	public Set<OWLAxiom> getUnclaPart2(){
		return unclaPart2;
	}
	
	public Set<OWLAxiom> getModule1(){
		return module1;
	}
	
	public Set<OWLAxiom> getModule2(){
		return module2;
	}
	
	public Module getM1() {
		return new Module(module1);
	}
	
	public Module getM2() {
		return new Module(module2);
	}
	
	public void splitModules(Set<OWLAxiom> unclassifiedAxioms, Module module) {
		splitModules(unclassifiedAxioms, module.getAxioms());
	}
	
	public void splitModules( Set<OWLAxiom> unclassifiedAxioms, Set<OWLAxiom> module) {
		HalfSigSelector halfSigSelector = new HalfSigSelector(new HashSet(unclassifiedAxioms));
    	Set<OWLAxiom> t1 = halfSigSelector.getSelectedAxioms();
    	Set<OWLAxiom> t2 = new HashSet(unclassifiedAxioms);
    	t2.removeAll(t1);
		
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology dlOnt = null;
		try {
			dlOnt = manager.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
        
    	module1 = botModExtractor.extract(AxiomsSig.getSigs(t1));
    	module2 = botModExtractor.extract(AxiomsSig.getSigs(t2));

    	Set<OWLAxiom> module3 = new HashSet(module2);
    	module3.retainAll(module1);
    	
    	
    	
    	//boolean divide = true;
    	//boolean thirdModule = false;
		
		/*if(module1.equals(module2)) {
		divide = false;
		//now module = module1 = module2
		Set<OWLAxiom> possibleBigAtomAxioms = new HashSet(unclassifiedAxioms);
		Set<OWLAxiom> coarsenedAtomAxioms = new HashSet();
		if(t1.size()==1) {
			possibleBigAtomAxioms.removeAll(t1);
			coarsenedAtomAxioms.addAll(t1);
		}
		if(t2.size()==1) {
			possibleBigAtomAxioms.removeAll(t2);    //supervised by RuiRui :)
			coarsenedAtomAxioms.addAll(t2);
		}
		Set<OWLAxiom> checkedAxioms = new HashSet();
		for(OWLAxiom ax:possibleBigAtomAxioms) {
	    	if(!checkedAxioms.contains(ax)) {
	    		Set<OWLAxiom> moduleChunk = botModExtractor.extract(ax.getSignature());
		    	if(moduleChunk.equals(module)) {
		    		coarsenedAtomAxioms.add(ax);
		    		checkedAxioms.add(ax);
		    	}
		    	else { //now it means moduleChunk is smaller than module
		    		
		    		Set<OWLAxiom> atomAxioms = new HashSet(possibleBigAtomAxioms);
		    		atomAxioms.retainAll(moduleChunk);
		    		//now moduleChunk is a genuine module and atomAxioms is an atom.
		    		atomsWithModule.add(new AtomUnionWithModule(atomAxioms, moduleChunk));
		    		
		    		//if any axiom in moduleChunk and in possibleBigAtomAxioms, 
		    		//it means this axiom's M is smaller than module, so we don;t extact module for these axioms
		    		checkedAxioms.addAll(moduleChunk);
		    	}
	    	}
		}
		atomsWithModule.add(new AtomUnionWithModule(coarsenedAtomAxioms, module));    		
	}*/
	
    	
    	if(module2.containsAll(module1)&&!module1.containsAll(module2)) {
	    	
    		unclaPart1 = new HashSet(unclassifiedAxioms);
    		unclaPart1.retainAll(module1);
    		unclaPart2 = new HashSet(unclassifiedAxioms);
    		unclaPart2.removeAll(module1);


    	}
    	else if(!module2.containsAll(module1)&&module1.containsAll(module2)) {
    		unclaPart1 = new HashSet(unclassifiedAxioms);
    		unclaPart1.removeAll(module2);
    		unclaPart2 = new HashSet(unclassifiedAxioms);
    		unclaPart2.retainAll(module2);

    	}
    	
    	else {
    		unclaPart1 = t1;
    		unclaPart2 = t2;
    	}
    	
    	
    	/*else if(module3.isEmpty()||module1.equals(module2)) {
    		unclaPart1 = t1;
    		unclaPart2 = t2;
    	}
    	else {
    		unclaPart1 = new HashSet(unclassifiedAxioms);
    		unclaPart1.retainAll(module1);
    		unclaPart2 = new HashSet(unclassifiedAxioms);
    		unclaPart2.removeAll(module1);
    		
    		if(unclaPart1.isEmpty()||unclaPart2.isEmpty()) {
    			System.out.println("error");
    		}
    		
    	}*/
    	
    	
    	//this one is from a very old version design method
    	/*else if(module3.isEmpty()) {
    		unclaPart1 = t1;
    		unclaPart2 = t2;
    	}
    	else {
    		unclaPart1 = new HashSet(unclassifiedAxioms);
    		unclaPart1.removeAll(module3);
    		unclaPart2 = new HashSet(unclassifiedAxioms);
    		unclaPart2.removeAll(module3);
    		unclaPart3 = new HashSet(unclassifiedAxioms);
    		unclaPart3.retainAll(module3);
    		
    		if(unclaPart1.isEmpty()||unclaPart2.isEmpty()) {
    			System.out.println("error");
    		}
    		if(!unclaPart3.isEmpty()) {
    			
    			//20231214 because using it will always cause memory problem so we leave that and solve it in the future
    			thirdModule = true;
        		
    			//if(unclaPart1.size()>unclaPart2.size()) {
    				//unclaPart2.addAll(unclaPart3);
    			//}
    			//else {
    				//unclaPart1.addAll(unclaPart3);
    			//}
    			
    		}
    	}*/
	
	}
}
