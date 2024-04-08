package owl.man.ac.uk.ReAD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import owl.man.ac.uk.ReADFull.ReADSetting;

/**
 * @author Haoruo Zhao
 * ***/

public class ReADRecursiveWithR extends RecursiveTask<Set<Reasoner>>{
	private Map<Set<OWLClass>, Set<OWLAxiom>> depen;
	private List<Set<OWLClass>> atomSigs;
	private int be;
	private int en;
	private int thre;
	//private Set<OWLClass> check;
	@Override
	protected Set<Reasoner> compute() {
		// TODO Auto-generated method stub
		Set<Reasoner> reasoners = new HashSet();
		if(en-be<thre){
			for(int i=be;i<en;i++){
				Set<OWLClass> cla = atomSigs.get(i);
				ReADSetting reAD = new ReADSetting(cla);
				
				Reasoner reasoner = new ModifiedHermiT(reAD, depen.get(cla)).getModifiedHermiT();
				
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
								
				reasoners.add(reasoner);
			}
			
		}
		else{
			int mid = (be + en) >>> 1;
			ReADRecursiveWithR left = new ReADRecursiveWithR(depen, atomSigs, be, mid, thre);
			ReADRecursiveWithR right = new ReADRecursiveWithR( depen, atomSigs, mid, en, thre);
			left.fork();
			right.fork();
			reasoners.addAll(left.join());
			reasoners.addAll(right.join());
		}
		return reasoners;
	}
	
	public ReADRecursiveWithR(Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen, List<Set<OWLClass>> sigs, 
			int begin, int end, int threshold){
		depen = bigDepen;
		atomSigs = sigs; 
		be = begin;
		en = end;
		thre = threshold;
		
	}
}
