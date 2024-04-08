package owl.man.ac.uk.Analysis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class P90P95P99Experiment {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File ADResult = new File(args[1]);
		
		CSVReader reader = new CSVReader(new FileReader(ADResult));
		List<String[]> values = reader.readAll();
		List<String> botTime = new ArrayList();
		List<String> topTime = new ArrayList();
		List<String> starTime = new ArrayList();
		
		for(int i=1;i<values.size();i++){
			String[] value = values.get(i);
			botTime.add(value[1]);
			topTime.add(value[5]);
			starTime.add(value[9]);
			
		}
		
		botTime = botTime.stream().sorted((String a, String b)->Long.valueOf(a).compareTo(Long.valueOf(b))).collect(Collectors.toList());
		topTime = topTime.stream().sorted((String a, String b)->Long.valueOf(a).compareTo(Long.valueOf(b))).collect(Collectors.toList());
		starTime = starTime.stream().sorted((String a, String b)->Long.valueOf(a).compareTo(Long.valueOf(b))).collect(Collectors.toList());
		
		File resultFile = new File(args[2]);
		CSVWriter writer = new CSVWriter(new FileWriter(resultFile));
		
		String[] head = {"type", "Mean", "StdDev", "Median", "P90", "P95", "P99","Max"};
		writer.writeNext(head);
		int p90 = (int) (botTime.size()*0.9) - 1;
		int p95 = (int) (botTime.size()*0.95) - 1;
		int p99 = (int) (botTime.size()*0.99) - 1;
		writer.writeNext(new String[]{"botAD", 
				String.valueOf(getAve(botTime)),
				String.valueOf(getStdDev(botTime)),
				getMedian(botTime),
				String.valueOf(botTime.get(p90)),
				String.valueOf(botTime.get(p95)),
				String.valueOf(botTime.get(p99)),
				String.valueOf(botTime.get(botTime.size()-1))
				});
		
		writer.writeNext(new String[]{"topAD", 
				String.valueOf(getAve(topTime)),
				String.valueOf(getStdDev(topTime)),
				getMedian(topTime),
				String.valueOf(topTime.get(p90)),
				String.valueOf(topTime.get(p95)),
				String.valueOf(topTime.get(p99)),
				String.valueOf(topTime.get(topTime.size()-1))
				});
		
		writer.writeNext(new String[]{"starAD", 
				String.valueOf(getAve(starTime)),
				String.valueOf(getStdDev(starTime)),
				getMedian(starTime),
				String.valueOf(starTime.get(p90)),
				String.valueOf(starTime.get(p95)),
				String.valueOf(starTime.get(p99)),
				String.valueOf(starTime.get(starTime.size()-1))
				});
		
		
		
		writer.close();
	}
	
	public static long getAve(List<String> values){
		long count = 0;
		for(String value:values){
			count = count + Long.valueOf(value);
		}
		return count/values.size();
	}
	
	public static long getStdDev(List<String> values){
		double ave = (double)getAve(values);
		double fangcha =0;
		for(String value:values){
			fangcha = fangcha + (Double.valueOf(value)-ave)*(Double.valueOf(value)-ave); 
		}
		
		return (long)Math.sqrt(fangcha/values.size());
	}
	
	public static String getMedian(List<String> values){
		int size = values.size();
		String median = null;
		if(size%2>0){
			median = values.get(size/2);
		}
		else{
			long a1 = Long.valueOf(values.get(size/2));
			long a2 = Long.valueOf(values.get(size/2 -1 ));
			long a3 = (a1+a2)/ 2;
			median = String.valueOf(a3);
		}
		
		return median;
	} 
	
}
