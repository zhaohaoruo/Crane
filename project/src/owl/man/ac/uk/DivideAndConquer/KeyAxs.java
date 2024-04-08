package owl.man.ac.uk.DivideAndConquer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public class KeyAxs {

	private Map<OWLAxiom, Set<OWLAxiom>> keyAxiomWithGenuineModule;
	private Map<OWLAxiom, Set<OWLAxiom>> keyAxiomWithitAtom;
	private Set<OWLAxiom> keyAxs;
	
	public KeyAxs() {
		
		keyAxiomWithGenuineModule = new HashMap();
		keyAxiomWithitAtom = new HashMap();
		keyAxs = new HashSet();	
	}
	
	public void keyAxiomUpdate(OWLAxiom axiom, Set<OWLAxiom> module){
		if(module.contains(axiom)) {//if contains it means axiom is not a tautology axiom
			boolean isKey = true;
			for(OWLAxiom beta:keyAxs) {
				Set<OWLAxiom> betaModule = keyAxiomWithGenuineModule.get(beta);
				if(betaModule.containsAll(module)&&module.containsAll(betaModule)) {
					Set<OWLAxiom> betaAtom = keyAxiomWithitAtom.get(beta);
					betaAtom.add(axiom);
					keyAxiomWithitAtom.put(beta, betaAtom);
					isKey = false;
				}
			}
			if(isKey) {
				keyAxs.add(axiom);
				keyAxiomWithGenuineModule.put(axiom, module);
				Set<OWLAxiom> atom = new HashSet();
				atom.add(axiom);
				keyAxiomWithitAtom.put(axiom, atom);
			}
		}
	}
	
	public void combineKeyAxs(KeyAxs k) {
		Map<OWLAxiom, Set<OWLAxiom>> kAxiomWithGenuineModule = k.getKeyAxiomWithGenuineModule();
		for(OWLAxiom axiom:k.getKeyAxs()) {
			Set<OWLAxiom> alphaModule = kAxiomWithGenuineModule.get(axiom);
			boolean isAKey = true;
			for(OWLAxiom beta:keyAxs) {
				Set<OWLAxiom> betaModule = keyAxiomWithGenuineModule.get(beta);
				if(betaModule.containsAll(alphaModule)&&alphaModule.containsAll(betaModule)) {
					keyAxiomWithitAtom.get(beta).addAll(
							k.getKeyAxiomWithitAtom().get(axiom)
							);
					isAKey = false;
					
				}
			}
			if(isAKey) {
				keyAxs.add(axiom);
				keyAxiomWithGenuineModule.put(axiom, alphaModule);
				Set<OWLAxiom> atom = new HashSet();
				atom.add(axiom);
				keyAxiomWithitAtom.put(axiom, atom);
			}
		}
	}
	
	public Set<OWLAxiom> getKeyAxs(){
		return keyAxs;
	}
	
	public Map<OWLAxiom, Set<OWLAxiom>> getKeyAxiomWithGenuineModule(){
		return keyAxiomWithGenuineModule;
	}
	
	public Map<OWLAxiom, Set<OWLAxiom>> getKeyAxiomWithitAtom(){
		return keyAxiomWithitAtom;
	}
}
