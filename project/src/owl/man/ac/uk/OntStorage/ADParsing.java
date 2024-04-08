package owl.man.ac.uk.OntStorage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.more.visitors.ELKAxiomVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import com.opencsv.CSVReader;

import uk.ac.manchester.cs.atomicdecomposition.Atom;

public class ADParsing {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";
	
	//private Map<Set<OWLClass>, Set<OWLAxiom>> smallDepen;
	private Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen;

	private List<Set<OWLClass>> bigSigs;
	//private List<Set<OWLClass>> smallSigs;

	
	private Set<OWLAxiom> elAxioms;
	
	public static void main(String[] args) 
			throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		System.out.println("begin");
		ADParsing s = new ADParsing(atomPath, depePath);
		
	}
	
	public ADParsing(String atomPath, String depePath) throws OWLOntologyCreationException, IOException{
		File atomFiles = new File(atomPath);
		
		Map<Integer, Set<OWLAxiom>> maps = new HashMap();
		Map<Integer, Boolean> isEL = new HashMap();
		
		for(File f:atomFiles.listFiles()){
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
			Set<OWLAxiom> axioms = new HashSet();
	        axioms.addAll(ontology.getTBoxAxioms(true));
	    	axioms.addAll(ontology.getRBoxAxioms(true));
	    	
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
			maps.put(index, axioms);
			isEL.put(index, isELK);
		}
		System.out.println("finish parsing the atoms");

		File depeFile = new File(depePath);
		CSVReader reader = new CSVReader(new FileReader(depeFile));
		List<String[]> rows = reader.readAll();
		
		//smallDepen = new HashMap();
		bigDepen = new HashMap();
		//smallSigs = new ArrayList();
		bigSigs = new ArrayList();
		elAxioms = new HashSet();
		
		Map<Integer, Set<OWLClass>> CanSsets = new HashMap();
		for(int atomInd = 0;atomInd<rows.size();atomInd++){
			//the number of the row is also the number of index of the atom
									
			String[] row = rows.get(atomInd);
			Set<OWLAxiom> axioms = new HashSet();
			boolean moduleIsEL = true;
			for(String s:row){
				int depeIndex = Integer.valueOf(s);
				if(!isEL.get(depeIndex)){
					moduleIsEL = false;
				}
				axioms.addAll(maps.get(depeIndex));
			}
			
			if(!moduleIsEL){
				
				Set<OWLClass> canS = new HashSet();
				for(OWLAxiom axiom:maps.get(atomInd)){
					canS.addAll(axiom.getClassesInSignature());
				}
				
				//if(axioms.size()>100){
					bigDepen.put(canS, axioms);
					bigSigs.add(canS);
					//CanSsets.put(atomInd, canS);
				/*}
				else{
					smallDepen.put(canS, axioms);
					smallSigs.add(canS);
				}*/
			}
			else{
				elAxioms.addAll(axioms);
			}
		}
					
		maps = null;
		System.out.println("pre finished");
	}
	
	/*public Map<Set<OWLClass>, Set<OWLAxiom>> getsmallMaps(){
		return smallDepen;
	}*/
	
	public Map<Set<OWLClass>, Set<OWLAxiom>> getbigMaps(){
		return bigDepen;
	}
	
	public List<Set<OWLClass>> getBigAtomSigs(){
		return bigSigs;
	}

	/*public List<Set<OWLClass>> getSmallAtomSigs(){
		return smallSigs;
	}*/
	
	public Set<OWLAxiom> getELModules(){
		return elAxioms;
	}
}
