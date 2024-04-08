package owl.man.ac.uk.ReAD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import owl.man.ac.uk.ReADFull.ToolPackage;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ReADCraneParallelRecursive extends RecursiveTask<Integer>{

	private int be;
	private int en;
	private int thre;
	
	private List<OWLAxiom> all_setMiniusLModule;
	private int threshold; 
	private Set<OWLAxiom> compModule;
	private Set<OWLAxiom> OntRAs;
	private Set<OWLClass> Sigclassified;
	
	
	//private Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
	//private List<Set<OWLClass>> bigSigs = new ArrayList();	
	
	@Override
	protected Integer compute() {
		// TODO Auto-generated method stub
		int depenSize = 0;
		if(en-be<thre){
			Set<OWLAxiom> setMiniusLModule = new HashSet();
			for(int i=be;i<en;i++){
				setMiniusLModule.add(all_setMiniusLModule.get(i));
			}
	        Set<OWLAxiom> restCheckList = new HashSet(setMiniusLModule);
	        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    	SyntacticLocalityModuleExtractor extractor = null;
			try {
				extractor = new SyntacticLocalityModuleExtractor(manager, manager.createOntology(compModule), ModuleType.BOT);
			} catch (OWLOntologyCreationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for(OWLAxiom axiom: setMiniusLModule){
				
				if(restCheckList.isEmpty()){
	        		break;
	        	}
				if(restCheckList.contains(axiom)){
					Set<OWLAxiom> mOne = extractor.extract(axiom.getSignature());
					try {
						craneInteration(mOne,threshold, axiom, restCheckList,depenSize);
					} catch (OWLOntologyCreationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
	        	        	
	        }
			//System.out.println("here we have "+bigSigs.size() +" modules" );
		}
		else{
			int mid = (be + en) >>> 1;
			ReADCraneParallelRecursive left = 
					new ReADCraneParallelRecursive(all_setMiniusLModule, compModule, threshold, OntRAs, Sigclassified,be, mid, thre);
			ReADCraneParallelRecursive right = 
					new ReADCraneParallelRecursive(all_setMiniusLModule, compModule, threshold, OntRAs, Sigclassified, mid, en, thre);
			left.fork();
			right.fork();
			depenSize = left.join()+ right.join();
			

		}
		
		return depenSize;
	}
	
	void craneInteration(Set<OWLAxiom> module, int threshold, OWLAxiom alpha, Set<OWLAxiom> restCheckList,int depen) throws OWLOntologyCreationException{
		OntRAs.remove(alpha);
		restCheckList.remove(alpha);
		Set<OWLClass> sig = ToolPackage.getSig(module);
		
		SyntacticLocalityEvaluator sle = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
				
		if(module.size()<threshold){						
			sig.removeAll(Sigclassified);
			//bigSigs.add(sig);
			depen ++;
			
    		Sigclassified.addAll(ToolPackage.getSig(module));

			OntRAs.removeAll(module);
			restCheckList.removeAll(module);
		}
		else{
			Set<OWLAxiom> inSameAtom = new HashSet();
			Set<OWLAxiom> conjunction = new HashSet(module);
			conjunction.retainAll(OntRAs);
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			SyntacticLocalityModuleExtractor extractor = 
					new SyntacticLocalityModuleExtractor(manager, manager.createOntology(module), ModuleType.BOT);
	    	
			
			for(OWLAxiom blta: conjunction){
				if(restCheckList.isEmpty()){
	        		break;
	        	}
				if(restCheckList.contains(blta)&&sle.isLocal(blta, alpha.getSignature())){
					craneInteration(extractor.extract(blta.getSignature()), threshold, blta, restCheckList, depen);
				}
			}
			
			sig.removeAll(Sigclassified);
			//bigSigs.add(sig);
			depen ++;
			
			Sigclassified.addAll(ToolPackage.getSig(module));
			OntRAs.removeAll(module);
			restCheckList.removeAll(module);

		}
		
	}
	

	public ReADCraneParallelRecursive(List<OWLAxiom> setMiniusLModule, 
			Set<OWLAxiom> compModule, int threshold, Set<OWLAxiom> OntRAs, Set<OWLClass> Sigclassified,
			int begin, int end, int thresh){
		this.all_setMiniusLModule = setMiniusLModule;
		this.compModule = compModule;
		this.threshold = threshold;
		this.OntRAs = OntRAs;
		this.Sigclassified = Sigclassified;
		be = begin;
		en = end;
		thre = thresh;
	}
}
