package owl.man.ac.uk.ReAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owl.man.ac.uk.ReADFull.ReADSetting;
import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;

/**
 * @author Haoruo Zhao
 * ***/

public class ReADRecursive extends RecursiveTask<Integer>{

	private Map<Set<OWLClass>, Set<OWLAxiom>> depen;
	private List<Set<OWLClass>> atomSigs;
	private int be;
	private int en;
	private int thre;
	
	@Override
	protected Integer compute() {
		// TODO Auto-generated method stub
		int num = 0;
		if(en-be<thre){
			for(int i=be;i<en;i++){
				Set<OWLClass> cla = atomSigs.get(i);
				ReADSetting reAD = new ReADSetting(cla);
				
				OWLReasoner reasoner = new ModifiedHermiT(reAD, depen.get(cla)).getModifiedHermiT();			
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
				
				num = num+reAD.getSTNumber();
				reasoner = null;
			}
			
		}
		else{
			int mid = (be + en) >>> 1;
			ReADRecursive left = new ReADRecursive(depen, atomSigs, be, mid, thre);
			ReADRecursive right = new ReADRecursive(depen, atomSigs, mid, en, thre);
			left.fork();
			right.fork();
			num = left.join() + right.join();
			//System.out.println(num+" STs are checked");
		}
		return num;
	}
	
	public ReADRecursive(Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen, List<Set<OWLClass>> sigs, 
			int begin, int end, int threshold){
		depen = bigDepen;
		atomSigs = sigs; 
		be = begin;
		en = end;
		thre = threshold;
	}
	
}
