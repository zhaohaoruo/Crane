package owl.man.ac.uk.CraneDevelopeExp;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

public class RemainingT2Manager {

	private Set<OWLAxiom> remainingT2Axioms;
	private Set<OWLEntity> remainingModuleSigs = new HashSet();
	private Set<OWLClass> remainingModuleCla = new HashSet();
	
	public RemainingT2Manager(Set<OWLAxiom> T2) {
		remainingT2Axioms = new HashSet(T2);
		for(OWLAxiom remainingT2Axiom:remainingT2Axioms){
			remainingModuleSigs.addAll(remainingT2Axiom.getSignature());
			remainingModuleCla.addAll(remainingT2Axiom.getClassesInSignature());
		}
	}
	
	public Set<OWLAxiom> getRemainingT2Axioms(){
		return remainingT2Axioms; 
	}
	
	public Set<OWLEntity> getRemainingT2Sigs(){
		return remainingModuleSigs;
	}
	
	public Set<OWLClass> getRemainingT2Cla(){
		return remainingModuleCla;
	}
	
	public void updateViaModule(Set<OWLAxiom> module) {
		remainingT2Axioms.removeAll(module);
		for(OWLAxiom axiom:module) {
			remainingModuleSigs.removeAll(axiom.getSignature());
			remainingModuleCla.removeAll(axiom.getClassesInSignature());
		}
	}
}
