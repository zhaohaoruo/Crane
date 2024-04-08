package owl.man.ac.uk.DivideAndConquer;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class AxiomsSig {
	static public Set<OWLEntity> getSigs(Set<OWLAxiom> t){
		Set<OWLEntity> sigs = new HashSet();
		for(OWLAxiom ax:t) {
			sigs.addAll(ax.getSignature());
		}
		return sigs;
	}
}
