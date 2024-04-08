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

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.more.lsignature.LogicFragment;
import org.semanticweb.more.lsignature.LsignatureManager;
import org.semanticweb.more.reasoner.Statistics;
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

import owl.man.ac.uk.ReAD.ReADCraneParallelRecursive;
import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReAD.ReADwithLSignature;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/*****
 * The version we have for chapter future work Crane algorithm.
 * We use it to get the data for tables in chapter 9.
 * ********/

public class ReADCraneParallel {
	private List<OWLAxiom> OntEL = new ArrayList();
	private List<OWLAxiom> OntRAs = new ArrayList();
	
	private Set<OWLAxiom> TEL = new HashSet();
	private Set<OWLClass> Sigclassified = new HashSet();
	private Set<OWLAxiom> rbox = new HashSet();
	private long craneParallelallCTTime = 0;
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException, OWLOntologyStorageException {
		System.out.println("begin the experiment ");

        File f = new File(args[0]);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		int i = Integer.parseInt(args[1]);

		long currentCPUTime1 = System.currentTimeMillis();
		ReADCraneParallel crane = new ReADCraneParallel(ontology, i);
		long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		System.out.println(ctTime/1000 +"/s for all");	
	}
	
	
	public ReADCraneParallel(OWLOntology ontology, int threshold) throws OWLOntologyCreationException,
																InterruptedException, ExecutionException, OWLOntologyStorageException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLAxiom> axioms = new HashSet();
        axioms.addAll(ontology.getTBoxAxioms(true));
        rbox = new HashSet(ontology.getRBoxAxioms(true));
    	axioms.addAll(rbox);
    	
    	initializeELOnt(axioms);
    	
    	long currentCPUTime = System.currentTimeMillis();
    	ontology =null;
    	OWLOntology dlOnt = manager.createOntology(axioms);
    	
    	
    	LsignatureManager lSignatureManager = new LsignatureManager(false, false);
    	lSignatureManager.findLsignature(dlOnt, LogicFragment.ELK, 
    				new Statistics(dlOnt, false, false));
    	
    	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
		Set<OWLAxiom> lModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getLsignatureClasses()));
        System.out.println("we have a EL++ module with "+lModule.size() +" axioms");

        TEL.addAll(lModule);
        Sigclassified.addAll(ToolPackage.getSig(lModule));
        
		Set<OWLAxiom> compModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getCompSignatureClasses()));
		
		//here we don't need RBox for getting remaining modules
        List<OWLAxiom> setMiniusLModule_list = new ArrayList(dlOnt.getTBoxAxioms(true));
        setMiniusLModule_list.removeAll(lModule);
        System.out.println("we have remaining axioms "+setMiniusLModule_list.size());
        dlOnt =null;
        manager = null;
        System.gc();
        ReADCrane crane = new ReADCrane(setMiniusLModule_list, compModule, threshold, OntRAs, OntEL, Sigclassified);		
        System.out.println("finish approximated AD");
        TEL.addAll(crane.getTEL());
       
        Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = crane.getbigMaps();
        System.out.println("we have "+bigDepen.size() +" complex modules");
        int remainingModuleNumber = bigDepen.size();
        
        long currentCPUTime1 = System.currentTimeMillis();
		ReADRecursive reAD = new ReADRecursive(bigDepen, crane.getBigAtomSigs(), 0, bigDepen.size(), 7);		
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(reAD);
        int STNumber = reAD.get();
        //System.out.println("We checked " +reAD.get() +" STs");
        forkJoinPool.shutdown();
        
        long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		//System.out.println(ctTime/1000 +"/s for classification");	
		
		craneParallelallCTTime = crane.allctTime + ctTime;
		System.out.println(craneParallelallCTTime/1000 +"/s for all classification");
	}
	
	
	
	private void initializeELOnt(Set<OWLAxiom> axioms){
		
		for(OWLAxiom axiom:axioms){
    		
			if(ToolPackage.isInFragment(axiom)){
				OntEL.add(axiom);
			}
			else{
				OntRAs.add(axiom);
			}
    	}
		//System.out.println("we have complex axioms "+OntRAs.size());
		OntRAs.removeAll(rbox);
	}
	
	private boolean isInFragment(Set<OWLAxiom> axioms){		
    	return OntEL.containsAll(axioms);
	}
	
	public long getAllCTTimeInMilliSeconds(){
		return craneParallelallCTTime;
	}
}
