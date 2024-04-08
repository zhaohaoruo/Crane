package owl.man.ac.uk.ReAD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import owl.man.ac.uk.ReADFull.ReADSetting;


/**
 * @author Haoruo Zhao
 * ***/

public class ModifiedHermiT {

	private Reasoner reasoner;
	
	public ModifiedHermiT(ReADSetting reAD, Set<OWLAxiom> module){
		//System.out.println(axioms.size());
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = null;
		try {
			ontology = man.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reasoner = new Reasoner(new Configuration(), ontology, reAD);			
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	}
	
	public Reasoner getModifiedHermiT() {
		return reasoner;
	}
	
	public Set<OWLAxiom> getInferredAxiomsFromClassification(){
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		
        OWLOntology infOnt = null;
		try {
			infOnt = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, getGenType());
        iog.fillOntology(manager, infOnt);
		return new HashSet(infOnt.getAxioms());
	}
	
	public static List<InferredAxiomGenerator<? extends OWLAxiom>> getGenType(){
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();
        gens.add(new InferredSubClassAxiomGenerator());
        gens.add(new InferredEquivalentClassAxiomGenerator());
        
        return gens;
	}
}
