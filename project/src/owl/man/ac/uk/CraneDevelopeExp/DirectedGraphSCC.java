package owl.man.ac.uk.CraneDevelopeExp;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import owl.man.ac.uk.Crane.MOReDecomposer;

public class DirectedGraphSCC {

	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("D:/SnomedCT_Extension/ExperimentOnt_TR/snomed/SnomedWithModifiedAnatomy7905_20210719.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
    	
    	MOReDecomposer moreDecomposer = new MOReDecomposer(ontology);
    	
    	Set<OWLAxiom> t2 = new HashSet(moreDecomposer.getT2());
    	System.out.println(t2.size());
    	
    	
    	Set<OWLEntity> t2Signature = new HashSet();
    	for(OWLAxiom axiom:t2){
    		t2Signature.addAll(axiom.getSignature());
    	}
    	ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long currentCPUTime1 = threadMXBean.getCurrentThreadCpuTime();
        
        SyntacticLocalityEvaluator sle = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);
        int i =0;
    	for(OWLAxiom axiom:t2){
    		Set<OWLAxiom> t2Remain = new HashSet(t2);
    		Set<OWLEntity> remainingSig = new HashSet(t2Signature);

    		t2Remain.remove(axiom);
    		remainingSig.removeAll(axiom.getSignature());
    		
    		for(OWLAxiom axiomB:t2Remain){
    			Set<OWLEntity> checkedSig = new HashSet(axiomB.getSignature());
    			
    			if(!sle.isLocal(axiom, checkedSig)){
    				i++;
    			}
    			checkedSig.removeAll(remainingSig);
    			
    			remainingSig.removeAll(checkedSig);
    			
    			Set<OWLEntity> checkedSig2 = new HashSet(axiom.getSignature());
    			checkedSig2.removeAll(remainingSig);
    			if(checkedSig2.isEmpty()){
    				break;
    			}
    			
    		}
    	}
    	
    	 long currentCPUTime2 = threadMXBean.getCurrentThreadCpuTime();
         long reADTime = currentCPUTime2 - currentCPUTime1;
     	System.out.println(reADTime);
     	System.out.println(i);

	}

}
