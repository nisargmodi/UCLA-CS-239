
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class MethodCount {
	int spaces = 0;
	HashMap<Integer, String> map = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException {
		getMethodCount();
	}

	public static void getMethodCount() throws IOException {
		BufferedReader input = new BufferedReader(new FileReader("/home/ashish/Eclipseworkspace/UCLA-CS-239/src/convert.txt"));
		FileWriter fw = new FileWriter("/home/ashish/Eclipseworkspace/UCLA-CS-239/src/methodcount.csv");
		BufferedWriter output = new BufferedWriter(fw);
		
		Stack<String> stack = new Stack<String>();
		ArrayList<String> list = new ArrayList<>();
		String padding = "";
		HashMap<String,Integer> hmap = new HashMap<String,Integer>(); 
		
		while (input.ready()) {
			
			String line = input.readLine();
	
			int index = line.indexOf("CALL");
			
			if (index != -1) {
					String method = line.substring(index + 5);
					
					if(hmap.containsKey(method)) {
						int count = hmap.get(method);
						count++;
						hmap.put(method, count);						
					}
					else{
						hmap.put(method, 1);
					}
						
				}				
			
		}
		
		// Generate CSV file of method counts
		for (Map.Entry<String, Integer> entry : hmap.entrySet()) {
		    
			//System.out.println( entry.getKey() + "," + entry.getValue());
			output.write(entry.getKey()+","+entry.getValue()+"\n");
		}
		output.close();
		
	}

}