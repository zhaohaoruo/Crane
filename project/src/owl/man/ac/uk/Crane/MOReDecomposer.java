package owl.man.ac.uk.Crane;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.more.lsignature.LogicFragment;
import org.semanticweb.more.lsignature.LsignatureManager;
import org.semanticweb.more.reasoner.Statistics;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owl.man.ac.uk.ReADFull.ToolPackage;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**********we use this one to decompose the ontology Ont into two modules using MORe approach:
 * 1)a relatively big EL module M_1 lModule
 * 2)a complex module M_2 compModule out of EL
 * M_1 UNION M_2 == Ont
 * 
 * 
 * @author Haoruo Zhao
 *	
 *	Please Note the codes from row 46 to row 55 are from the code from MORe project https://github.com/anaphylactic/MORe
 */


public class MOReDecomposer {
	private Set<OWLAxiom> lModule = new HashSet();
	private Set<OWLAxiom> compModule = new HashSet();
	private int exampleModuleSize;
	private Set<OWLAxiom> OntEL;
	public MOReDecomposer(OWLOntology ontology) throws OWLOntologyCreationException{
		
		Set<OWLAxiom> axioms = new HashSet();
        axioms.addAll(ontology.getTBoxAxioms(true));
    	axioms.addAll(ontology.getRBoxAxioms(true));
    	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	OWLOntology dlOnt = manager.createOntology(axioms);
    	
    	LsignatureManager lSignatureManager = new LsignatureManager(false, false);
    	lSignatureManager.findLsignature(dlOnt, LogicFragment.ELK, 
    				new Statistics(dlOnt, false, false));
    	   	
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);
		lModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getLsignatureClasses()));
		
		compModule = botModExtractor.extract(
				new HashSet<OWLEntity>(lSignatureManager.getCompSignatureClasses()));
		
		OntEL = getELOntologyPart(compModule);
	}
	
	private Set<OWLAxiom> getELOntologyPart(Set<OWLAxiom> module) {
		Set<OWLAxiom> OntEL = new HashSet();
		for(OWLAxiom axiom:module) {
			if(ToolPackage.isInFragment(axiom)){
				OntEL.add(axiom);
			}
		}
		return OntEL;
	}
	
	public int getExampleModuleSize(int seedSize) throws OWLOntologyCreationException {
		Set<OWLEntity> exampleSeeds = new HashSet();		
		int i=0;
		for(OWLAxiom ax:compModule) {
			if(i<seedSize) {
				exampleSeeds.addAll(ax.getSignature());
				i++;
			}			
		}
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	OWLOntology dlOnt = manager.createOntology(compModule);
    	SyntacticLocalityModuleExtractor botModExtractor = new SyntacticLocalityModuleExtractor(manager, dlOnt, ModuleType.BOT);

		Set<OWLAxiom> examplemodule = botModExtractor.extract(exampleSeeds);
		exampleModuleSize = examplemodule.size();
		
		return exampleModuleSize;
	}
	public Set<OWLAxiom> getMOReELModule(){
		return lModule;
	}
	
	public Set<OWLAxiom> getT2(){
		Set<OWLAxiom> t2 = new HashSet(compModule);
		t2.removeAll(lModule);
		return t2;
	}
	
	public Set<OWLAxiom> getMOReRemainingModule(){
		return compModule;
	}
	
	public Set<OWLAxiom> getELPartInRemainingModule(){
		return OntEL;
	}
	
}
