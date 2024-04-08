package owl.man.ac.uk.OntStorage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class DependenciesCheckExp {
	private static String depePath = "E:/SNOMED_CT/Storage";


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File depeFile = new File(depePath+"/SnomedDepen1004.csv");
		CSVReader reader = new CSVReader(new FileReader(depeFile));
		List<String[]> rows = reader.readAll();
		
		System.out.println(rows.size());
	}

}
