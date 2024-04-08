package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Haoruo Zhao
 * ***/

public class AnatomyConverter {
	private Set<OWLAxiom> notModifiedAxioms;
	private Set<OWLAxiom> modifiedAxioms;
	
	private Set<OWLClass> modifiedStructureConcept;

	private int templateNumber;
	private OWLDataFactory df;
	
	private Map<String, String> structureCodeMap;
	private Map<String, String> entityCodeMap;
	private Map<String, String> structureEntityMap;
	
	private OWLObjectProperty templateProperty;
	private int modifiedAxiomN;
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException {
		// TODO Auto-generated method stub
		File f = new File("E:/SnomedCT_Extension/anatomy_20210128_TR.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology anatomy = manager.loadOntologyFromOntologyDocument(f);
		
		new AnatomyConverter(anatomy, manager.getOWLDataFactory());
	}

	
	public AnatomyConverter(OWLOntology ontology, OWLDataFactory datafactory) throws IOException{
		
		/************initialize ***************/
		Set<OWLAxiom> equivAxioms = new HashSet();
		df = datafactory;
		templateNumber = 0;
		
		notModifiedAxioms = new HashSet(ontology.getRBoxAxioms(true));
		modifiedAxioms = new HashSet();
		modifiedStructureConcept = new HashSet();
		
		AnatomyMapping map = new AnatomyMapping();
		System.out.println("finish loading SE maps");
		
		structureCodeMap = map.getStructureCodeMap();
		entityCodeMap = map.getEntityCodeMap();
		structureEntityMap = map.getStructureEntityMap();
		
		modifiedAxiomN = 0;
		
		for(OWLObjectProperty p:ontology.getObjectPropertiesInSignature(true)){
			//774081006|Proper part of (attribute)|
			if(p.getIRI().getFragment().equals("774081006")){
				templateProperty = p;
				break;
			}
		}
		
		/**************************/
				
		for(OWLAxiom axiom:ontology.getTBoxAxioms(true)){
			
			if(axiom instanceof OWLEquivalentClassesAxiom){
				equivAxioms.add(axiom);
			}
			else{
				notModifiedAxioms.add(axiom);
			}
		}
				
		System.out.println("equivAxioms number is"+equivAxioms.size());
		for(OWLAxiom axiom:equivAxioms){
			dealWithOWLEquivalentClassesAxiom(axiom);
		}
		System.out.println("suitable template number is"+templateNumber);
	}
	
	public Set<OWLAxiom> getNotModifiedAxioms(){
		return notModifiedAxioms;
	}
	
	public Set<OWLClass> getModifiedStructureConcepts(){
		return modifiedStructureConcept;
	}
	
	public Set<OWLAxiom> getModifiedAxioms(){
		return modifiedAxioms;
	}
	
	private void dealWithOWLEquivalentClassesAxiom(OWLAxiom axiom){
		
		OWLEquivalentClassesAxiom quivAxiom = (OWLEquivalentClassesAxiom)axiom;
		List<OWLClassExpression> expressions = quivAxiom.getClassExpressionsAsList();
		notModifiedAxioms.add(axiom);
		
		if(expressions.get(0) instanceof OWLClass){
			OWLClass structureConcept = (OWLClass)expressions.get(0);
			String structure = structureConcept.getIRI().getFragment();
			
			if(structureCodeMap.keySet().contains(structure)){
				OWLClassExpression entityComplexExpression = expressions.get(1);			
				if(entityComplexExpression instanceof OWLObjectIntersectionOf){
					boolean needModified; 
					needModified = dealWithOWLObjectIntersectionOf(entityComplexExpression, structureEntityMap.get(structure));
					if(needModified && modifiedAxiomN <1000 ){
						OWLClassExpression entityComplexExpressionRewrited = rewriteOWLObjectIntersectionOf(entityComplexExpression);
						modifiedAxioms.add(df.getOWLEquivalentClassesAxiom(structureConcept,entityComplexExpressionRewrited));
						notModifiedAxioms.remove(axiom);
						modifiedStructureConcept.add(structureConcept);
						modifiedAxiomN++;
					}
					
				}
			}
			
			
		}
		else if(expressions.get(1) instanceof OWLClass){
			OWLClass structureConcept = (OWLClass)expressions.get(1);
			String structure = structureConcept.getIRI().getFragment();
			
			if(structureCodeMap.keySet().contains(structure)){
				OWLClassExpression entityComplexExpression = expressions.get(0);			
				if(entityComplexExpression instanceof OWLObjectIntersectionOf){
					boolean needModified; 
					needModified = dealWithOWLObjectIntersectionOf(entityComplexExpression, structureEntityMap.get(structure));
					if(needModified && modifiedAxiomN <1000){
						OWLClassExpression entityComplexExpressionRewrited = rewriteOWLObjectIntersectionOf(entityComplexExpression);
						modifiedAxioms.add(df.getOWLEquivalentClassesAxiom(structureConcept,entityComplexExpressionRewrited));
						notModifiedAxioms.remove(axiom);
						modifiedStructureConcept.add(structureConcept);
						modifiedAxiomN++;
					}
					
				}
			}
			
		}
		
		
	}
	
	private boolean dealWithOWLObjectIntersectionOf(OWLClassExpression entityComplexExpression, String entityMapName){
		OWLObjectIntersectionOf ce = (OWLObjectIntersectionOf)entityComplexExpression;
    	Iterator<OWLClassExpression> it = ce.getOperands().iterator();

    	boolean isBodyStr = false;
    	boolean hasLaterality = false;
    	boolean hasEntityPart = false;
    	
    	while(it.hasNext()){
    		OWLClassExpression ex = it.next();
    		if(ex instanceof OWLClass){
    			OWLClass bodyStructureCla = (OWLClass)ex;
    			String bodyStructure = bodyStructureCla.getIRI().getFragment();
    			isBodyStr = bodyStructure.equals("123037004");
    		}
    		else if(ex instanceof OWLObjectSomeValuesFrom){
    			OWLObjectSomeValuesFrom someExpression = (OWLObjectSomeValuesFrom)ex;
    			OWLObjectProperty property = someExpression.getProperty().getNamedProperty();;
    			String proString = property.getIRI().getFragment();
    			
    			if(proString.equals("733928003")){
    				if(someExpression.getFiller() instanceof OWLClass){
    					hasEntityPart = ((OWLClass)someExpression.getFiller()).getIRI().getFragment().equals(entityMapName);
    				}
    				
    			}
    			else if(proString.equals("272741003")){  				
    				if(someExpression.getFiller() instanceof OWLClass){
    					hasLaterality = ((OWLClass)someExpression.getFiller()).getIRI().getFragment().equals("182353008");
    				}
    				
    			}
    		}
    	}
    	boolean needModified = isBodyStr&&hasLaterality&&hasEntityPart;
    	
    	if(needModified){
    		templateNumber++;
    	}
    	return needModified;
	}
	
	private OWLClassExpression rewriteOWLObjectIntersectionOf(OWLClassExpression entityComplexExpression){
		OWLObjectIntersectionOf ce = (OWLObjectIntersectionOf)entityComplexExpression;
    	Iterator<OWLClassExpression> it = ce.getOperands().iterator();
    	
    	Set<OWLClassExpression> expressions = new HashSet();
    	
    	
    	while(it.hasNext()){
    		OWLClassExpression ex = it.next();
    		if(ex instanceof OWLClass){
    			expressions.add(ex);
    		}
    		else if(ex instanceof OWLObjectSomeValuesFrom){
    			OWLObjectSomeValuesFrom someExpression = (OWLObjectSomeValuesFrom)ex;
    			OWLObjectProperty property = someExpression.getProperty().getNamedProperty();;
    			String proString = property.getIRI().getFragment();
    			
    			if(proString.equals("733928003")){
    				OWLClass entireC = (OWLClass)someExpression.getFiller();
    				OWLClassExpression rewritedSome = df.getOWLObjectSomeValuesFrom(templateProperty, entireC);
    				expressions.add(df.getOWLObjectUnionOf(rewritedSome, entireC));
    				
    			}
    			else if(proString.equals("272741003")){  				
    				expressions.add(ex);   				
    			}
    		}
    	}
    	
		return df.getOWLObjectIntersectionOf(expressions);
	}
}
