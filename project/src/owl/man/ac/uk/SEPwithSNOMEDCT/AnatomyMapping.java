package owl.man.ac.uk.SEPwithSNOMEDCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Haoruo Zhao
 * ***/

public class AnatomyMapping {

	private Map<String, String> structureCodeMap = new HashMap();
	private Map<String, String> entityCodeMap = new HashMap();
	private Map<String, String> structureEntityMap = new HashMap();
	
	public AnatomyMapping() throws IOException{
		FileInputStream fin = new FileInputStream("E:/SnomedCT_Extension/Uli group/Anatomy_SE_maps_20210128.txt");
		InputStreamReader reader = new InputStreamReader(fin);
		BufferedReader buffReader = new BufferedReader(reader);
	        
		String line = buffReader.readLine();
	        
	        
		while(line!=null){
			String[] lines = line.split("\\t");      	
			structureCodeMap.put(lines[0], lines[1]);
			entityCodeMap.put(lines[2], lines[3]);
			structureEntityMap.put(lines[0], lines[2]);
			line = buffReader.readLine();
		}
		buffReader.close();
	}
	
	public Map<String, String> getStructureCodeMap(){
		return structureCodeMap;
	}
	
	public Map<String, String> getEntityCodeMap(){
		return entityCodeMap;
	}
	
	public Map<String, String> getStructureEntityMap(){
		return structureEntityMap;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new AnatomyMapping();
	}

}
