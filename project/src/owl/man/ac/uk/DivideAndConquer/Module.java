package owl.man.ac.uk.DivideAndConquer;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;


public class Module {

	private Set<OWLAxiom> axioms;
	
	public Module(Set<OWLAxiom> module) {
		this.axioms = module;
	}
	
	public Set<OWLAxiom> getAxioms(){
		return axioms;
	}
	
	public boolean containsModule(Module m) {
		if(this.getAxioms().containsAll(m.getAxioms())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o instanceof Module){
			return this.getAxioms().equals( ((Module)o).getAxioms());
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
