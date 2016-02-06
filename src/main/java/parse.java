package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class parse {
	static int spaces = 0;
	static HashMap<Integer,String> map = new HashMap<Integer,String>();
	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(
				new FileReader("E:\\Punit\\D\\UCLA\\Winter15\\CS239\\Project\\PartA\\Code\\test\\src\\test\\small.txt"));
		BufferedWriter output = new BufferedWriter(new FileWriter(
				"E:\\Punit\\D\\UCLA\\Winter15\\CS239\\Project\\PartA\\Code\\test\\src\\test\\qwerty.txt"));
		Stack<String> stack = new Stack<String>();
		String padding = "";
		String paddedString[] = { "", "	", "		", "			", "				","					","      "};
		String countStack[] = {"<0>","<1>","<2>","<3>","<4>","<5>","<6>"};
		
		
		while (input.ready()) {
			System.out.println("check");
			String line = input.readLine();
			// output.write(line);
			if (stack.isEmpty()) {
				stack.push("");
				padding = padString();
				//output.write(paddedString[spaces]+countStack[spaces]+ line);
				output.write(padding+line);
				output.newLine();
				spaces++;
			} else {
				if (line.contains("CALL")) {
					stack.push("");
					padding = padString();
					//output.write(paddedString[spaces]+countStack[spaces]+ line);
					output.write(padding+line);
					output.newLine();
					spaces++;
				}
				if (line.contains("RETURN")) { //add elseif end if there are extra statements
					stack.pop();
					spaces--;
					padding = padString();
					//output.write(paddedString[spaces]+countStack[spaces]+ line);
					output.write(padding+line);
					output.newLine();
					// add if another begin
				}
			}
		}
		System.out.println("done");
		input.close();
		output.close();
	}
	
	public static String padString()
	{
		String padding = map.get(spaces);
		if(padding==null)
		{
			int i=0;
			StringBuffer pad= new StringBuffer("");
			while(i<spaces)
			{
				pad.append("	");
			}
			String ins = pad.toString()+"<"+i+">";
			map.put(spaces,ins);
			return ins ;
		}
		else
			return padding;
		
	}

}