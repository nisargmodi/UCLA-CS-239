package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class CreateTree {
	int spaces = 0;
	HashMap<Integer, String> map = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException {
		new CreateTree().createTree();
	}

	public void createTree() throws IOException {
		BufferedReader input = new BufferedReader(
				new FileReader("ENTER FILENAME HERE"));
		BufferedWriter output = new BufferedWriter(
				new FileWriter("ENTER FILENAME HERE"));
		Stack<String> stack = new Stack<String>();
		String padding = "";

		while (input.ready()) {
			String line = input.readLine();
			if (stack.isEmpty() && line.contains("CALL")) {
				stack.push("");
				padding = padString();
				output.write(padding + line);
				output.newLine();
				spaces++;
			} else {
				if (line.contains("CALL")) {
					stack.push("");
					padding = padString();
					output.write(padding + line);
					output.newLine();
					spaces++;
				}
				if (line.contains("RETURN")) {
					stack.pop();
					spaces--;
					padding = padString();
					output.write(padding + line);
					output.newLine();
				}
			}
		}
		System.out.println("Done");
		input.close();
		output.close();
	}

	public String padString() {
		String padding = map.get(spaces);
		if (padding == null) {
			int i = 0;
			StringBuffer pad = new StringBuffer("");
			while (i < spaces) {
				pad.append("	");
				i++;
			}
			String ins = pad.toString() + "<" + i + ">";
			map.put(spaces, ins);
			return ins;
		} else
			return padding;
	}

}