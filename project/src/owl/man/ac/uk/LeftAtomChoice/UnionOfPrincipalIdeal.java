package owl.man.ac.uk.LeftAtomChoice;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;

public class UnionOfPrincipalIdeal {
	private Set<OWLAxiom> unionPI;
	private Set<OWLClass> unionPICon;
	public UnionOfPrincipalIdeal(Set<Atom> leftAtoms, AtomicDecomposition ad){
		//Set<Atom> topAtomsInLeftAtoms = new HashSet(ad.getTopAtoms());
		//topAtomsInLeftAtoms.retainAll(leftAtoms);
		unionPI = new HashSet();
		unionPICon = new HashSet();
		for(Atom atom:leftAtoms){
			for(Atom dep : ad.getDependencies(atom)){
				unionPI.addAll(dep.getAxioms());
				for(OWLAxiom axiom:dep.getAxioms()){
					unionPICon.addAll(axiom.getClassesInSignature());
			
				}
			}
			
		}
	}
	
	public Set<OWLAxiom> getUnionOfPI(){
		return unionPI;
	}
	
	public Set<OWLClass> getUnionOfPICon(){
		return unionPICon;
	}
}
