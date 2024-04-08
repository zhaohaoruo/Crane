package owl.man.ac.uk.SnomedCT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReADFull.GraphADFull;

//for check the correctness of ReAD-Full versus HermiT
public class SNOMEDComparisonExp {

	private static String atomPath = null;
	private static String depePath = null;
	
	//this experiment we ensure the relation from ReAD-Full is the same class set as the ontology
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, 
													InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		
		File f = new File(args[0]);
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);		
		OWLReasoner reasoner = new Reasoner(new Configuration(),ontology,null);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		IdentityMultiMap<OWLClass, OWLClass> hermitG = new IdentityMultiMap();
		for(OWLClass atomic:ontology.getClassesInSignature(true)){
			Set<OWLClass> superClass = new HashSet();
			superClass.addAll(reasoner.getSuperClasses(atomic, false).getFlattened());	
			superClass.addAll(reasoner.getEquivalentClasses(atomic).getEntities());
			
			hermitG.putAll(atomic, superClass);	
		}
		int i = 0;
        Set<OWLClass> keys = hermitG.keySet();
        for(OWLClass clas:keys){
        	i = i+hermitG.get(clas).size();
        	
        }
        System.out.println("HermiT size:"+i);
		
		ADParsing s = new ADParsing(atomPath, depePath);
		//Map<Set<OWLClass>, Set<OWLAxiom>> smallDepen = s.getsmallMaps();
		IdentityMultiMap<OWLClass, OWLClass> reADG = new GraphADFull(s).getReADG();
		
		int j = 0;
		for(OWLClass clas:reADG.keySet()){
			j = j +reADG.get(clas).size();
		}
		System.out.println("reAD SIZE:"+j);
	}
}
