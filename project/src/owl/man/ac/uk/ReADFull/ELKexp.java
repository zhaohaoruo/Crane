package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

public class ELKexp {

	public static void main(String[] args) throws OWLOntologyCreationException {
		// TODO Auto-generated method stub
		File f = new File("E:/SNOMED_CT/ELOnt/SNOMED_20200731_TR.owl");
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		Set<OWLAxiom> axioms = new HashSet();
        axioms.addAll(ontology.getTBoxAxioms(true));
    	axioms.addAll(ontology.getRBoxAxioms(true));
    	
    	OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		//ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        //long currentCPUTime1 = threadMXBean.getCurrentThreadCpuTime();
		long currentCPUTime1 = System.currentTimeMillis();
        OWLOntology elOnt = man.createOntology(axioms);
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		//elkReasoner = new Reasoner(new Configuration(), elOnt, null);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		long currentCPUTime3 = System.currentTimeMillis();
		long ctTime = currentCPUTime3 - currentCPUTime1;
		
		
		System.out.println(ctTime +"ms");
        System.out.println(ctTime/1000 +"/s");
        IdentityMultiMap<OWLClass, OWLClass> elkG = new IdentityMultiMap();
		for(OWLClass atomic:elOnt.getClassesInSignature(true)){
			elkG.putAll(atomic, elkReasoner.getSuperClasses(atomic, false).getFlattened());	
			elkG.putAll(atomic, elkReasoner.getEquivalentClasses(atomic).getEntities());
		}
		
		Set<OWLClass> clas = new Ont("E:/SNOMED_CT/DLOnt/SNOMED_20200731_DLtest_TR.owl").getOnt().getClassesInSignature(true);
		
		for(OWLClass cla:elkG.keySet()){
			if(!clas.contains(cla)){
				System.out.println("don't conatain");
			}
			System.out.println(elkG.get(cla).size());
		}
	}

}
