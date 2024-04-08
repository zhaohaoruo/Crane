package owl.man.ac.uk.CraneDevelopeExp;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Haoruo Zhao
 * ***/

public class HalfSigSelector {

	private Set<OWLEntity> seedSigs = new HashSet();
	private Set<OWLAxiom> selectedAxioms = new HashSet();
	public HalfSigSelector(Set<OWLAxiom> t2) {
		
		int i=0;
		for(OWLAxiom axiom:t2) {
			if(i%2==0) {
				seedSigs.addAll(axiom.getSignature());
				selectedAxioms.add(axiom);
			}
			i++;			
		}
	}
	
	public Set<OWLEntity> getSeedSigs(){
		return seedSigs;
	}
	
	public Set<OWLAxiom> getSelectedAxioms(){
		return selectedAxioms;
	}
	
}
