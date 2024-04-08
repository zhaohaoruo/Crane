package owl.man.ac.uk.Verification;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.OntStorage.ADParsing;
import owl.man.ac.uk.ReADFull.ReADSetting;

/**
 * @author Haoruo Zhao
 * ***/

public class ReADCorrectness {
	
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		// TODO Auto-generated method stub
		ADParsing s = new ADParsing(atomPath, depePath);
		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		 
        OWLOntology elOnt = manager.createOntology(s.getELModules());
		
        OWLReasoner elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		//elkReasoner = new Reasoner(new Configuration(), elOnt, null);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
		Set<OWLAxiom> inferredAxioms = new HashSet();
		for(Set<OWLClass> cla:bigDepen.keySet()){
			ReADSetting reAD = new ReADSetting(cla);
			Set<OWLAxiom> axioms = bigDepen.get(cla);
			//System.out.println(axioms.size());
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();

			OWLOntology ontology = man.createOntology(axioms);
			OWLReasoner reasoner = new Reasoner(new Configuration(), ontology, reAD);			
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			inferredAxioms.addAll(getInferredAxioms(man, reasoner));	
		}		
		
		File inferred = new File("E:/SNOMED_CT/InferredResults/RemainingModulesInferred.owl");
		OWLOntology inferredOnt = manager.createOntology(inferredAxioms);
		manager.saveOntology(inferredOnt, IRI.create(inferred));
		
		inferredAxioms.addAll(getInferredAxioms(manager, elkReasoner));
		File allInferred = new File("E:/SNOMED_CT/InferredResults/ReADInferred.owl");
		OWLOntology allInferredOnt = manager.createOntology(inferredAxioms);
		manager.saveOntology(allInferredOnt, IRI.create(allInferred));
	}
	

	
	public static Set<OWLAxiom> getInferredAxioms(OWLOntologyManager manager, OWLReasoner reasoner) throws OWLOntologyCreationException{
		
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();
        gens.add(new InferredSubClassAxiomGenerator());
        gens.add(new InferredEquivalentClassAxiomGenerator());
        OWLOntology infOnt = manager.createOntology();
        // create the inferred ontology generator
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(manager, infOnt);
        
        Set<OWLAxiom> axioms = new HashSet();
        for(OWLAxiom axiom:infOnt.getAxioms()){
        	if(axiom instanceof OWLEquivalentClassesAxiom){
        		OWLEquivalentClassesAxiom ax = (OWLEquivalentClassesAxiom)axiom;
        		Iterator<OWLClassExpression> i= ax.getClassExpressions().iterator();
        		OWLClassExpression c = i.next();
        		OWLClassExpression d = i.next();
        		OWLDataFactory factory = manager.getOWLDataFactory();
        		axioms.add(factory.getOWLSubClassOfAxiom(c, d));
        		axioms.add(factory.getOWLSubClassOfAxiom(d, c));    		
        	}
        	else if(axiom instanceof OWLSubClassOfAxiom){
        		OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom)axiom;
    			if(!ax.getSuperClass().isOWLThing()&&!ax.getSubClass().isOWLNothing()){
    				axioms.add(axiom);
    			}
    	
        	}
        }
        
        return axioms;
	}
}
