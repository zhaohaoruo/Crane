package owl.man.ac.uk.ReADFull;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.DeterministicClassification.GraphNode;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owl.man.ac.uk.ReAD.ModifiedHermiT;


public class ReADRecursiveHermiTHierarchy extends RecursiveTask< Map<AtomicConcept,GraphNode<AtomicConcept>> >  {
	private Map<Set<OWLClass>, Set<OWLAxiom>> depen;
	private List<Set<OWLClass>> atomSigs;
	private int be;
	private int en;
	private int thre;
	//private Set<OWLClass> check;
	@Override
	protected Map<AtomicConcept,GraphNode<AtomicConcept>> compute() {
		// TODO Auto-generated method stub
		Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers = new HashMap();
		if(en-be<thre){
			for(int i=be;i<en;i++){
				Set<OWLClass> cla = atomSigs.get(i);
				ReADSetting reAD = new ReADSetting(cla);
				
				OWLReasoner reasoner = new ModifiedHermiT(reAD, depen.get(cla)).getModifiedHermiT();
				
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
								
				allSubsumers.putAll(reAD.getAllSubsumers());

			}			
		}
		else{
			int mid = (be + en) >>> 1;
			ReADRecursiveHermiTHierarchy left = new ReADRecursiveHermiTHierarchy(depen, atomSigs, be, mid, thre);
			ReADRecursiveHermiTHierarchy right = new ReADRecursiveHermiTHierarchy( depen, atomSigs, mid, en, thre);
			left.fork();
			right.fork();
			allSubsumers.putAll(left.join());
			allSubsumers.putAll(right.join());
		}
		return allSubsumers;
	}
	
	public ReADRecursiveHermiTHierarchy(Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen, List<Set<OWLClass>> sigs, 
			int begin, int end, int threshold){
		depen = bigDepen;
		atomSigs = sigs; 
		be = begin;
		en = end;
		thre = threshold;
		
	}

}
