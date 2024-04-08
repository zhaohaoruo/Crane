package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author Haoruo Zhao
 * ***/

public class SNOMEDdeleteStrucureConcept {
	private Set<OWLAxiom> remainingSCTAxioms;
	private int modifiedSCTAxiomN;
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		// TODO Auto-generated method stub
		File f = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/anatomy_20210128_TR.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology anatomy = manager.loadOntologyFromOntologyDocument(f);
		Set<OWLAxiom> anatomyTR = new HashSet(anatomy.getTBoxAxioms(true));
		anatomyTR.addAll(anatomy.getRBoxAxioms(true));
		System.out.println(anatomyTR.size());
		
		AnatomyConverter converter = new AnatomyConverter(anatomy, manager.getOWLDataFactory());
		manager = null;
		
		OWLOntologyManager man1 = OWLManager.createOWLOntologyManager();
		File snomedFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/SNOMED_2021Jan_TR.owl");
		OWLOntology snomedOnt = man1.loadOntologyFromOntologyDocument(snomedFile);
		
		SNOMEDdeleteStrucureConcept sno = new SNOMEDdeleteStrucureConcept(snomedOnt, converter.getModifiedStructureConcepts());
		
		Set<OWLAxiom> axioms = new HashSet(sno.getRemainingSCTAxioms());
		axioms.addAll(converter.getModifiedAxioms());
		System.out.println("ModifiedAxioms:"+converter.getModifiedAxioms().size());
		axioms.addAll(converter.getNotModifiedAxioms());
		System.out.println("NotModifiedAxioms:"+converter.getNotModifiedAxioms().size());


		System.out.println("new ontology size:"+axioms.size());
		
		/*File ontFile = new File("E:/SnomedCT_Extension/ExperimentOnt_TR/SnomedWithModifiedAnatomy7905_20210719.owl");
		
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();

		OWLOntology modifiedAnatomy = manager1.createOntology(axioms);
		manager1.saveOntology(modifiedAnatomy, IRI.create(ontFile));*/
	}

	public SNOMEDdeleteStrucureConcept(OWLOntology snomedOnt, Set<OWLClass> structureConcepts){
		modifiedSCTAxiomN = 0;
		remainingSCTAxioms = new HashSet(snomedOnt.getRBoxAxioms(true));
		remainingSCTAxioms.addAll(snomedOnt.getTBoxAxioms(true));
		for(OWLAxiom axiom:snomedOnt.getTBoxAxioms(true)){
			if(axiom instanceof OWLSubClassOfAxiom){
				OWLSubClassOfAxiom subAxiom = (OWLSubClassOfAxiom)axiom;
				
				OWLClassExpression subEx = subAxiom.getSubClass();
				OWLClassExpression supEx = subAxiom.getSuperClass();

				if(subEx instanceof OWLClass && supEx instanceof OWLClass){
					OWLClass sub = (OWLClass)subEx;
					OWLClass sup = (OWLClass)supEx;
					if(structureConcepts.contains(sub)&&structureConcepts.contains(sup)){
						modifiedSCTAxiomN++;
						remainingSCTAxioms.remove(axiom);
					}
				
				}
			}
			else if(axiom instanceof OWLEquivalentClassesAxiom){
				OWLEquivalentClassesAxiom equAxiom = (OWLEquivalentClassesAxiom) axiom;
				List<OWLClassExpression> expressions = equAxiom.getClassExpressionsAsList();
				
				if(expressions.get(0) instanceof OWLClass && expressions.get(1) instanceof OWLClass){
					OWLClass sub = (OWLClass)expressions.get(0);
					OWLClass sup = (OWLClass)expressions.get(1);
					if(structureConcepts.contains(sub)&&structureConcepts.contains(sup)){
						modifiedSCTAxiomN++;
						remainingSCTAxioms.remove(axiom);
					}
				}
			}
			
		}
		System.out.println("we remove "+modifiedSCTAxiomN+" axioms from SCT");
	}
	
	public Set<OWLAxiom> getRemainingSCTAxioms(){
		return remainingSCTAxioms;
	}
}
