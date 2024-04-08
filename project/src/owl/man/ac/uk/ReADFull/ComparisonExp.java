package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

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
import owl.man.ac.uk.ReAD.ReADRecursive;
import owl.man.ac.uk.ReAD.ReADRecursiveGraph;

public class ComparisonExp {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		ADParsing s = new ADParsing(atomPath, depePath);
		//Map<Set<OWLClass>, Set<OWLAxiom>> smallDepen = s.getsmallMaps();
		IdentityMultiMap<OWLClass, OWLClass> reADG = new GraphADFull(s).getReADG();
		
		
		File f = new File("E:/SNOMED_CT/ELOnt/SNOMED_20200731_TR.owl");
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(ontology);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		IdentityMultiMap<OWLClass, OWLClass> elkG = new IdentityMultiMap();
		for(OWLClass atomic:ontology.getClassesInSignature(true)){
			Set<OWLClass> superClass = new HashSet();
			superClass.addAll(elkReasoner.getSuperClasses(atomic, false).getFlattened());	
			superClass.addAll(elkReasoner.getEquivalentClasses(atomic).getEntities());
			
			elkG.putAll(atomic, superClass);	
		}
		int i = 0;
        Set<OWLClass> keys = elkG.keySet();
        for(OWLClass clas:keys){
        	i = i+elkG.get(clas).size();
        	
        }
        System.out.println("ELK size:"+i);
        
        int j = 0;
        for(OWLClass clas:reADG.keySet()){
        	j = j +reADG.get(clas).size();
        }
        System.out.println("reAD SIZE:"+j);
	}

}
