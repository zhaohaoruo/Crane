package owl.man.ac.uk.ReADFull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.hierarchy.DeterministicClassification.GraphNode;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Haoruo Zhao
 * ***/

public class ReADSetting {

	private Set<AtomicConcept> atomicC;
	private Set<AtomicConcept> checkedC;
	
	// how many concept name in clas already be checked for their candidate subsumers
	private int checkCanSNumber;
	
	private Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers = new HashMap();
	
	private int stNumber;
	
	public ReADSetting(Set<OWLClass> clas){
		atomicC = new HashSet();
		checkedC = new HashSet();
		checkCanSNumber = 0;
		stNumber = 0;
		for(OWLClass cla:clas){
			atomicC.add(AtomicConcept.create(cla.getIRI().toString()));
		}
	}
	
	public void addAllSubsumers(Map<AtomicConcept,GraphNode<AtomicConcept>> allSubs) {
		allSubsumers.putAll(allSubs);
	}
	
	public Map<AtomicConcept,GraphNode<AtomicConcept>> getAllSubsumers() {
		return  allSubsumers; 
	}
	
	public Set<AtomicConcept> getCanS(){
		return atomicC;
	}
	
	public void addCheckedCanS(){
		checkCanSNumber++;
	}
	
	public void addSTNumber(){
		stNumber++;
	}
	
	public int getCheckedNumber(){
		return checkCanSNumber;
	}
	
	public int getSTNumber(){
		return stNumber;
	}
}
