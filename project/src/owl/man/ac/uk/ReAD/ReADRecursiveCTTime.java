package owl.man.ac.uk.ReAD;

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
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.ReADFull.ReADSetting;

public class ReADRecursiveCTTime extends RecursiveTask<IdentityMultiMap<OWLClass, OWLClass>>{
	private Map<Set<OWLClass>, Set<OWLAxiom>> depen;
	private List<Set<OWLClass>> atomSigs;
	private int be;
	private int en;
	private int thre;
	//private Set<OWLClass> check;
	@Override
	protected IdentityMultiMap<OWLClass, OWLClass> compute() {
		// TODO Auto-generated method stub
		IdentityMultiMap<OWLClass, OWLClass> maps = new IdentityMultiMap<OWLClass, OWLClass>();
		if(en-be<thre){
			for(int i=be;i<en;i++){
				Set<OWLClass> cla = atomSigs.get(i);
				ReADSetting reAD = new ReADSetting(cla);
				
				OWLReasoner reasoner = new ModifiedHermiT(reAD, depen.get(cla)).getModifiedHermiT();
				
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
								
				for(OWLClass atomic:cla){
					Set<OWLClass> superClass = new HashSet();
					superClass.addAll(reasoner.getSuperClasses(atomic, false).getFlattened());	
					superClass.addAll(reasoner.getEquivalentClasses(atomic).getEntities());
					maps.putAll(atomic, superClass);
				}
			}
			return maps;
		}
		else{
			int mid = (be + en) >>> 1;
			ReADRecursiveGraph left = new ReADRecursiveGraph(depen, atomSigs, be, mid, thre);
			ReADRecursiveGraph right = new ReADRecursiveGraph( depen, atomSigs, mid, en, thre);
			left.fork();
			right.fork();
			maps.putAll(left.join());
			maps.putAll(right.join());
		}
		return maps;
	}
	
	public ReADRecursiveCTTime(Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen, List<Set<OWLClass>> sigs, 
			int begin, int end, int threshold){
		depen = bigDepen;
		atomSigs = sigs; 
		be = begin;
		en = end;
		thre = threshold;
		
	}
}
