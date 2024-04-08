package owl.man.ac.uk.OntStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVReader;

public class OneComplexModuleFromAD {

	private Set<OWLAxiom> module;
	private Set<OWLClass> canS;
	
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		// TODO Auto-generated method stub
		//args[0] for atom path, args[1] for depePath and args[2] for the module path
		
		OneComplexModuleFromAD module = new OneComplexModuleFromAD(args[0], args[1]);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		File f = new File(args[2]);
		OWLOntology m = manager.createOntology(module.getModule());
		manager.saveOntology(m, IRI.create(f));
		
	}
	
	public OneComplexModuleFromAD(String atomPath, String depePath) throws OWLOntologyCreationException, IOException{
		File atomFiles = new File(atomPath);
		File depeFile = new File(depePath);
		CSVReader reader = new CSVReader(new FileReader(depeFile));
		List<String[]> rows = reader.readAll();
		
		module = new HashSet();
		for(File f:atomFiles.listFiles()){
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
			
			Set<OWLClass> sig = new HashSet();

			Set<OWLAxiom> axioms = new HashSet();
	        axioms.addAll(ontology.getTBoxAxioms(true));
	    	axioms.addAll(ontology.getRBoxAxioms(true));    	
	    	sig.addAll(ontology.getClassesInSignature(true));
			boolean isELK = true;
	    	for(OWLAxiom axiom:axioms){
	    		ELKAxiomVisitor vistor = new ELKAxiomVisitor();
				axiom.accept(vistor);
				isELK = vistor.isInFragment();
				if(!isELK){
					break;
				}
	    	}
	    	
			int index = Integer.valueOf(f.getName().substring(0, f.getName().indexOf(".")));
			System.out.println("ontology:"+index);
			
			if(!isELK){
				String[] row = rows.get(index);
				
				boolean moduleIsEL = true;
				for(String s:row){
					File atomFile = new File(atomPath+"/"+s+".owl");
					int atomIndex = Integer.valueOf(atomFile.getName().substring(0, atomFile.getName().indexOf(".")));
					if(atomIndex!=index){
						buildModule(axioms, atomFile, sig);
					}
					else{
						System.out.println("remove one");
					}
				}
				canS = new HashSet(sig);
				module.addAll(axioms);
				break;
			}
		}
	}
	
	public Set<OWLAxiom> getModule(){
		return module;
	}
	
	public Set<OWLClass> getCanS(){
		return canS;
	}
	
	private void buildModule(Set<OWLAxiom> axioms, File atomFile, Set<OWLClass> sig) throws OWLOntologyCreationException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(atomFile);
        axioms.addAll(ontology.getTBoxAxioms(true));
    	axioms.addAll(ontology.getRBoxAxioms(true));
    	sig.removeAll(ontology.getClassesInSignature(true));
	}

}
