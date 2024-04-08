package owl.man.ac.uk.Crane;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Haoruo Zhao
 * ***/


public class AtomUnionWithModule {
	private Set<OWLAxiom> unionOfAtoms;
	private Set<OWLAxiom> module;
	
	public AtomUnionWithModule(Set<OWLAxiom> unionOfAtoms, Set<OWLAxiom> module){
		this.unionOfAtoms = unionOfAtoms;
		this.module = module;
	}
	
	public Set<OWLClass> getAllCanS(){
		Set<OWLClass> cla = new HashSet();
		for(OWLAxiom axiom:unionOfAtoms){
			cla.addAll(axiom.getClassesInSignature());
		}
		return cla;
	}
	
	public Set<OWLAxiom> getUnionOfAtoms(){
		return unionOfAtoms;
	}
	
	public Set<OWLAxiom> getModule(){
		return module;
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
		for(OWLAxiom axiom:this.unionOfAtoms){
			code = axiom.hashCode()*7 + code;
		}
        return code;
    }
}
