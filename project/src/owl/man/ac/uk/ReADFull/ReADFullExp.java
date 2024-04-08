package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
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

import owl.man.ac.uk.OntStorage.ADParsing;

public class ReADFullExp {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		// TODO Auto-generated method stub
		ADParsing s = new ADParsing(atomPath, depePath);
		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long currentCPUTime1 = threadMXBean.getCurrentThreadCpuTime();
        
        OWLOntology elOnt = manager.createOntology(s.getELModules());
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		//elkReasoner = new Reasoner(new Configuration(), elOnt, null);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
		IdentityMultiMap<OWLClass, OWLClass> elkG = new IdentityMultiMap();
		for(OWLClass atomic:elOnt.getClassesInSignature(true)){
			Set<OWLClass> superClass = new HashSet();
			superClass.addAll(elkReasoner.getSuperClasses(atomic, false).getFlattened());	
			superClass.addAll(elkReasoner.getEquivalentClasses(atomic).getEntities());
			
			elkG.putAll(atomic, superClass);	
		}
		
		
		System.out.println("big modules number:"+bigDepen.keySet().size());
		for(Set<OWLClass> cla:bigDepen.keySet()){
			ReADSetting reAD = new ReADSetting(cla);
			Set<OWLAxiom> axioms = bigDepen.get(cla);
			//System.out.println(axioms.size());
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();

			OWLOntology ontology = man.createOntology(axioms);
			OWLReasoner reasoner = new Reasoner(new Configuration(), ontology, reAD);			
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			for(OWLClass atomic:cla){
				Set<OWLClass> superClass = new HashSet();
				superClass.addAll(reasoner.getSuperClasses(atomic, false).getFlattened());	
				superClass.addAll(reasoner.getEquivalentClasses(atomic).getEntities());
				elkG.putAll(atomic, superClass);
			}		
		}		
		int j = 0;
		for(OWLClass clas:elkG.keySet()){
			j = j +elkG.get(clas).size();
		}
		System.out.println("reAD SIZE:"+j);		
		long currentCPUTime3 = threadMXBean.getCurrentThreadCpuTime();
		long ctTime = currentCPUTime3 - currentCPUTime1;
        System.out.print(ctTime/1000000000 +"/s");
	}

}
