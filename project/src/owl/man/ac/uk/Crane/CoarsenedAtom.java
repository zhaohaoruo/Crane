package owl.man.ac.uk.Crane;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Haoruo Zhao
 * ***/

public class CoarsenedAtom {

	private Set<OWLAxiom> axioms;
	
	public CoarsenedAtom(Set<OWLAxiom> axioms){
		this.axioms = axioms;
	}
	
	public Set<OWLClass> getAllCanS(){
		Set<OWLClass> cla = new HashSet();
		for(OWLAxiom axiom:axioms){
			cla.addAll(axiom.getClassesInSignature());
		}
		return cla;
	}
	
	public Set<OWLAxiom> getAxioms(){
		return axioms;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o instanceof CoarsenedAtom){
			return this.hashCode() == ((CoarsenedAtom)o).hashCode();
		}
		return false;
		
	}
	
	@Override
    public int hashCode() {
		int code = 0;
		for(OWLAxiom axiom:this.axioms){
			code = axiom.hashCode()*7 + code;
		}
        return code;
    }
}
