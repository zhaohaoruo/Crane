package owl.man.ac.uk.Verification;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
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
import owl.man.ac.uk.ReADFull.ReADSetting;

/**
 * @author Haoruo Zhao
 * ***/

public class EmbeddingReasonerToReAD {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		// TODO Auto-generated method stub
		ADParsing s = new ADParsing(atomPath, depePath);
		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        
        OWLOntology elOnt = manager.createOntology(s.getELModules());
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		//elkReasoner = new Reasoner(new Configuration(), elOnt, null);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
       		
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
			}
		}		
		
	}
}
