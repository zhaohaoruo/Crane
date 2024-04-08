package owl.man.ac.uk.ReAD;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import owl.man.ac.uk.ReADFull.ReADSetting;

public class ReADRecursiveInferredAxioms extends RecursiveTask< Set<OWLAxiom>>{

	private Map<Set<OWLClass>, Set<OWLAxiom>> depen;
	private List<Set<OWLClass>> atomSigs;
	private int be;
	private int en;
	private int thre;
	
	@Override
	protected Set<OWLAxiom> compute() {
		// TODO Auto-generated method stub
		Set<OWLAxiom> inferredAxioms = new HashSet();
		if(en-be<thre){
			for(int i=be;i<en;i++){
				Set<OWLClass> cla = atomSigs.get(i);
				ReADSetting reAD = new ReADSetting(cla);
				ModifiedHermiT modifiedHermiT = new ModifiedHermiT(reAD, depen.get(cla));
				inferredAxioms.addAll(modifiedHermiT.getInferredAxiomsFromClassification());
				
			}
			
		}
		else{
			int mid = (be + en) >>> 1;
			ReADRecursiveInferredAxioms left = new ReADRecursiveInferredAxioms(depen, atomSigs, be, mid, thre);
			ReADRecursiveInferredAxioms right = new ReADRecursiveInferredAxioms(depen, atomSigs, mid, en, thre);
			left.fork();
			right.fork();
			inferredAxioms.addAll(left.join());
			inferredAxioms.addAll(right.join());
			//System.out.println(num+" STs are checked");
		}
		return inferredAxioms;
	}
	
	public ReADRecursiveInferredAxioms(Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen, List<Set<OWLClass>> sigs, 
			int begin, int end, int threshold){
		depen = bigDepen;
		atomSigs = sigs; 
		be = begin;
		en = end;
		thre = threshold;
	}
	
}
