package owl.man.ac.uk.ReADFull;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public class ToolPackage {
	public static boolean isInFragment(OWLAxiom axiom){
		ELKAxiomVisitor vistor = new ELKAxiomVisitor();
		axiom.accept(vistor);
		return vistor.isInFragment();
	}
	
	public static Set<OWLClass> getSig(Set<OWLAxiom> axioms){
		Set<OWLClass> sigs = new HashSet();
		for(OWLAxiom axiom:axioms){
			sigs.addAll(axiom.getClassesInSignature());
    	}
		return sigs;
	}
}
