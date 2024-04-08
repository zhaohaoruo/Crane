package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.more.lsignature.LogicFragment;
import org.semanticweb.more.lsignature.LsignatureManager;
import org.semanticweb.more.reasoner.Statistics;
import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReAD.ReADwithLSignature;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecompositionImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ReADCrane {

	private Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = new HashMap();
	private List<Set<OWLClass>> bigSigs = new ArrayList();
	private int semiThreshold;
	private Set<OWLClass> Sigclassified;
	private Set<OWLAxiom> setMiniusLModule;
	private int ontNumebr=0;
	private List<OWLAxiom> OntEL;
	private Set<OWLAxiom> TEL = new HashSet();
	public long allctTime =0;
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		
		
	}
	
	public ReADCrane(List<OWLAxiom> setMiniusLModule_list, Set<OWLAxiom> compModule, int threshold, 
			List<OWLAxiom> OntRAs, List<OWLAxiom> OntEL_Previous, Set<OWLClass> Sigclassified) 
					throws OWLOntologyCreationException, OWLOntologyStorageException, InterruptedException, ExecutionException{
		this.semiThreshold = threshold/10;
		this.Sigclassified = Sigclassified;
		this.OntEL = OntEL_Previous;
		setMiniusLModule = new HashSet(setMiniusLModule_list);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology dlOnt = manager.createOntology(compModule);
		int remainingAxiomNumber = OntRAs.size();
		int moduleSize = 0;
		
		SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(
				manager, dlOnt, ModuleType.BOT);	
		
		Set<OWLEntity> sig = new HashSet();
		Set<OWLAxiom> axiomInSig = new HashSet();
		for(int i =0;i<remainingAxiomNumber;i++){
			OWLAxiom axiom = OntRAs.get(i);
			if(setMiniusLModule.contains(axiom)){
				sig.addAll(axiom.getSignature());
				axiomInSig.add(axiom);
			}			
			
			if(axiomInSig.size()==threshold ){
				Set<OWLAxiom> module = botModExtractor.extract(sig);
				getModules(axiomInSig,module);
				moduleSize++;
				axiomInSig.clear();;
				sig.clear();
			}
			if(i==remainingAxiomNumber-1&&!axiomInSig.isEmpty()){
				Set<OWLAxiom> module = botModExtractor.extract(sig);
				getModules(axiomInSig,module);
				moduleSize++;
				axiomInSig.clear();;
				sig.clear();
			}
			
		}
		OntRAs = null;
		compModule = null;
		sig.clear();
		//System.out.println("remaining el axioms" +setMiniusLModule.size());
		for(OWLAxiom axiom:setMiniusLModule){			
			sig.addAll(axiom.getSignature());
		}
		Set<OWLAxiom> superModule = botModExtractor.extract(sig);
		//System.out.println("remaining module " +superModule.size());
		
		sig.clear();
		axiomInSig.clear();
		setMiniusLModule_list.retainAll(setMiniusLModule);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		SyntacticLocalityModuleExtractor extracotr = new SyntacticLocalityModuleExtractor(
				man, man.createOntology(superModule), ModuleType.BOT);	
		
		OntEL.retainAll(superModule);
		
		for(int i =0;i<setMiniusLModule_list.size();i++){
			OWLAxiom axiom = setMiniusLModule_list.get(i);
			if(setMiniusLModule.contains(axiom)){
				sig.addAll(axiom.getSignature());
				axiomInSig.add(axiom);
			}			
			
			if(axiomInSig.size()%threshold ==0){
				Set<OWLAxiom> module = extracotr.extract(sig);
				getModulesFromEL(axiomInSig,module);
				moduleSize++;
				axiomInSig.clear();;
				sig.clear();
			}
			if(i==remainingAxiomNumber-1&&!axiomInSig.isEmpty()){
				Set<OWLAxiom> module = extracotr.extract(sig);
				getModulesFromEL(axiomInSig,module);
				moduleSize++;
				axiomInSig.clear();;
				sig.clear();
			}
			
		}
		//System.out.println(moduleSize);
	}
	
	private void getModules(Set<OWLAxiom> axiomInSig,Set<OWLAxiom> module)
			throws OWLOntologyCreationException, OWLOntologyStorageException, InterruptedException, ExecutionException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Set<OWLClass> can = ToolPackage.getSig(module);
		can.removeAll(Sigclassified);
		if(!can.isEmpty()){
			List<OWLAxiom> allAdditionalAxiomInSig = new ArrayList(module);			
			allAdditionalAxiomInSig.retainAll(setMiniusLModule);
			
			if(allAdditionalAxiomInSig.size()>=semiThreshold){
				SyntacticLocalityModuleExtractor extracotr = new SyntacticLocalityModuleExtractor(
						manager, manager.createOntology(module), ModuleType.BOT);
				
				boolean stillSigIn = false;
				for(int i =0;i<semiThreshold;i++){
					Set<OWLEntity> sig = new HashSet();
					Set<OWLAxiom> additionalAxiomInSig = new HashSet();
					OWLAxiom axiom = allAdditionalAxiomInSig.get(i);
					if(setMiniusLModule.contains(axiom)){
						sig.addAll(axiom.getSignature());
						axiomInSig.add(axiom);
					}			
					
					if(axiomInSig.size()==semiThreshold ){
						Set<OWLAxiom> dependentModule = extracotr.extract(sig);
						if(dependentModule.size()!= module.size()){
							getModulesFromEL(axiomInSig, dependentModule);
						}
						else{
							stillSigIn = true;
						}
						axiomInSig.clear();;
						sig.clear();
						can.removeAll(Sigclassified);
					}
					if(i==semiThreshold-1&&!axiomInSig.isEmpty()){
						Set<OWLAxiom> dependentModule = extracotr.extract(sig);
						if(dependentModule.size()!= module.size()){
							getModulesFromEL(axiomInSig, dependentModule);
						}
						else{
							stillSigIn = true;
						}
						axiomInSig.clear();;
						sig.clear();
						can.removeAll(Sigclassified);
					}
					
				}
				if(stillSigIn&&!can.isEmpty()){
					addModuleAndClassified(can, module);
				}
				
			}
			else{
				addModuleAndClassified(can, module);				
			}
			
		}
		
		
	}
	
	private void getModulesFromEL(Set<OWLAxiom> axiomInSig,Set<OWLAxiom> module) 
			throws OWLOntologyCreationException, OWLOntologyStorageException, InterruptedException, ExecutionException{
		
		if(OntEL.containsAll(module)){
			//System.out.println(" in EL");
			TEL.addAll(module);
			Set<OWLClass> can = ToolPackage.getSig(module);
			setMiniusLModule.removeAll(module);
			Sigclassified.addAll(can);		
		}
		else{
			getModules(axiomInSig,module);
		}
		
	}
	
	private void addModuleAndClassified(Set<OWLClass> can, Set<OWLAxiom> module) 
			throws OWLOntologyCreationException, InterruptedException, ExecutionException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(module);   
		setMiniusLModule.removeAll(module);
		
		Sigclassified.addAll(can);
		bigDepen.put(can, module);
		bigSigs.add(can);
		
		if(bigSigs.size()>35){
			//System.out.println("begin classification");
			long currentCPUTime1 = System.currentTimeMillis();
			ReADRecursive reAD = new ReADRecursive(bigDepen, bigSigs, 0, bigDepen.size(), 7);		
	        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
	        forkJoinPool.submit(reAD);
	        int STNumber = reAD.get();
	        //System.out.println("We checked " +reAD.get() +" STs");
	        forkJoinPool.shutdown();
	        
	        long currentCPUTime3 = System.currentTimeMillis();
			long ctTime = currentCPUTime3 - currentCPUTime1;
			allctTime = allctTime+ctTime;
			//System.out.println(ctTime/1000 +"/s for classifying modules");
			bigDepen.clear();
			bigSigs.clear();
		}
		
	}
	
	public Map<Set<OWLClass>, Set<OWLAxiom>> getbigMaps(){
		return bigDepen;
	}
	
	public List<Set<OWLClass>> getBigAtomSigs(){
		return bigSigs;
	}
	
	public Set<OWLAxiom>  getTEL(){
		return TEL;
	}
}
