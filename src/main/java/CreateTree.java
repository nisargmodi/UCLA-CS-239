package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class CreateTree {
	int spaces = 0;
	HashMap<Integer, String> map = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException {
		new CreateTree().createTree();
	}

	public void createTree() throws IOException {
		BufferedReader input = new BufferedReader(new FileReader("ENTER FILENAME HERE"));
		Stack<String> stack = new Stack<String>();
		ArrayList<String> list = new ArrayList<>();
		String padding = "";

		while (input.ready()) {
			String line = input.readLine();
			if (stack.isEmpty() && line.contains("CALL")) {
				stack.push("");
				padding = padString();
				list.add(padding + line);
				spaces++;
			} else {
				if (line.contains("CALL")) {
					stack.push("");
					padding = padString();
					list.add(padding + line);
					spaces++;
				}
				if (line.contains("RETURN")) {
					stack.pop();
					spaces--;
					list.add(padding + line);
				}
			}
		}
		input.close();
		this.createTreewithAnno(list);
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

	public void createTreewithAnno(ArrayList<String> list) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter("ENTER FILENAME HERE"));

		int count = 1;
		for (int i = 2; i < list.size(); i++) {
			if (list.get(i).contains("CALL") && list.get(i - 2).contains("CALL")) {
				if (!list.get(i).equals(list.get(i - 2))) {
					output.write(list.get(i) + "<COUNT>" + count);
					output.newLine();
					output.write(list.get(i));
					output.newLine();
					count = 1;
				} else
					count++;
			}
		}
		System.out.println("CCT generated");
		output.close();
	}

}