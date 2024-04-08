package owl.man.ac.uk.ReAD;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecompositionImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ReADwithLSignature {
	private Map<Set<OWLClass>, Set<OWLAxiom>> depen = new HashMap();
	private List<Set<OWLClass>> atomSigs = new ArrayList();
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		System.out.println("begin the experiment ");

        File f = new File(args[0]);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		new ReADwithLSignature(ontology);
	}
	
	public ReADwithLSignature(OWLOntology ontology) throws OWLOntologyCreationException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLAxiom> axioms = new HashSet();
        axioms.addAll(ontology.getTBoxAxioms(true));
    	axioms.addAll(ontology.getRBoxAxioms(true));
    	long currentCPUTime = System.currentTimeMillis();

    	OWLOntology dlOnt = manager.createOntology(axioms);
    	
    	
    	LsignatureManager lSignatureManager = new LsignatureManager(false, false);
    	lSignatureManager.findLsignature(dlOnt, LogicFragment.ELK, 
    				new Statistics(dlOnt, false, false));
    	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
		Set<OWLAxiom> lModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getLsignatureClasses()));
        System.out.println("we have a EL++ module with "+lModule.size() +" axioms");

		Set<OWLAxiom> compModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getCompSignatureClasses()));
    	List<OWLAxiom> remainingAxioms = new ArrayList(compModule);
        System.out.println("we have a remaining module with "+remainingAxioms.size() +" axioms");

    	AtomicDecomposition ad = new AtomicDecompositionImpl(remainingAxioms, ModuleType.BOT);
    	initialRemainingModules(ad);
    	
        System.out.println("AD has "+ad.getAtoms().size() +" atoms");

        System.out.println("finsih preparation ");

		long currentCPUTime1 = System.currentTimeMillis();

        OWLOntology elOnt = manager.createOntology(lModule);
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);


		ReADRecursive reAD = new ReADRecursive(depen, atomSigs, 0, depen.size(), 7);		
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(reAD);
        //System.out.println("We checked " +reAD.get() +" STs");
        forkJoinPool.shutdown();
        
    	long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		long allTime = currentCPUTime3 - currentCPUTime;
		System.out.println(ctTime +"ms");
        System.out.println(ctTime/1000 +"/s");	
        System.out.println(allTime +"ms with parsing, ad computation and classification");	
	}
	
	private void initialRemainingModules(AtomicDecomposition ad){
		for(Atom atom:ad.getAtoms()){
    		Set<OWLClass> canS = new HashSet();
			for(OWLAxiom axiom:atom.getAxioms()){
				canS.addAll(axiom.getClassesInSignature());
			}
			Set<OWLAxiom> axioms = new HashSet();
			for(Atom depen:ad.getDependencies(atom)){
				axioms.addAll(depen.getAxioms());
			}
			
			depen.put(canS, axioms);
			atomSigs.add(canS);
    	}
	}
}
