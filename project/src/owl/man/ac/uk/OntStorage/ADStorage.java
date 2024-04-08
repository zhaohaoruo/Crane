package owl.man.ac.uk.OntStorage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.opencsv.CSVWriter;

import uk.ac.manchester.cs.atomicdecomposition.Atom;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecomposition;
import uk.ac.manchester.cs.atomicdecomposition.AtomicDecompositionImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class ADStorage {
	
	//private static String atomPath = "E:/SNOMED_CT/Storage/Atom";
	//private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms";
	//private static String depePath = "E:/SNOMED_CT/Storage";
	
	private static String atomPath = "E:/FMA/adJournalVersion/FMAAtoms";
	private static String depePath = "E:/FMA/adJournalVersion";

	
	public static void main(String[] args) 
			throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		//File f = new File("E:/SNOMED_CT/DLOnt/SNOMED_20200731_DLtest_TR.owl");
		
		File f = new File(args[0]);
		//File f = new File("example/ccont.cell-culture-ontology.3.owl.xml");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		ADStorage s = new ADStorage(ontology, args[1], args[2]);
		
	}
	
	public ADStorage(OWLOntology ontology, String atomPath, String dependencyPath) 
			throws OWLOntologyCreationException, OWLOntologyStorageException, IOException{
		
		List<OWLAxiom> axioms = new ArrayList();
        axioms.addAll(ontology.getTBoxAxioms(true));
    	axioms.addAll(ontology.getRBoxAxioms(true));
    	
    	AtomicDecomposition ad = new AtomicDecompositionImpl(axioms, ModuleType.BOT);
    	
    	File atomsFile = new File(dependencyPath+"/SnomedAtomsNum.csv");
    	CSVWriter atomWriter = new CSVWriter(new FileWriter(atomsFile));
    	
    	atomWriter.writeNext(new String[]{"Atom number", String.valueOf(ad.getAtoms().size())});
    	atomWriter.close();
    	
    	Map<Atom, Integer> maps = new HashMap();
    	int i = 0;
    	for(Atom atom:ad.getAtoms()){
    		maps.put(atom, i);
    		i++;
    	}
    	
    	File f = new File(dependencyPath+"/SnomedDepen.csv");
    	CSVWriter writer = new CSVWriter(new FileWriter(f));
    	
    	for(Atom atom:maps.keySet()){
    		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    		OWLOntology ont = manager.createOntology(new HashSet(atom.getAxioms()));
    		File atomFile = new File(atomPath+"/"+ maps.get(atom)+".owl");
    		manager.saveOntology(ont, IRI.create(atomFile));
    		Set<Atom> depen = new HashSet(ad.getDependencies(atom));
    		List<String> rowL = new ArrayList();
    		for(Atom d:depen){
    			rowL.add(String.valueOf(maps.get(d)));
    			
    		}
    		writer.writeNext(rowL.toArray(new String[rowL.size()]));
    		
    	}
    	writer.close();
	}
}
