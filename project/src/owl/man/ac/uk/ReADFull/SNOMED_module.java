package owl.man.ac.uk.ReADFull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.opencsv.CSVWriter;

import owl.man.ac.uk.OntStorage.ADParsing;

public class SNOMED_module {
	private static String atomPath = "E:/SNOMED_CT/Storage/SnomedAtoms_1004";
	private static String depePath = "E:/SNOMED_CT/Storage";

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		// TODO Auto-generated method stub
		ADParsing s = new ADParsing(atomPath, depePath);
		Map<Set<OWLClass>, Set<OWLAxiom>> bigDepen = s.getbigMaps();

		
		File f = new File("E:/SNOMED_CT/modules.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(f));
		String[] head = {"elmoduleNumber", "remainingModuleNumber"};
		writer.writeNext(head);
		
		int moduleNum = bigDepen.keySet().size();
		
		String[] row = {String.valueOf(s.getELModules().size()), String.valueOf(moduleNum)};
		writer.writeNext(row);
		writer.close();
	}

}
