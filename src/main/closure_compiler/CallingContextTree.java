package main.closure_compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class CallingContextTree {
	int spaces = 0;
	HashMap<Integer, String> map = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException {
		CallingContextTree cct = new CallingContextTree();
		ArrayList<String> list = cct.createCCT();
		ArrayList<String> nlist = cct.createTreeWithAnnotation(list);
		cct.generateKLengthSequences(nlist);
	}

	public ArrayList<String> createCCT() throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(
				"ENTER FILENAME HERE"));
		Stack<String> stack = new Stack<String>();
		ArrayList<String> list = new ArrayList<>();
		String padding = "";

		while (input.ready()) {
			String line = input.readLine();
			if (stack.isEmpty() && line.contains("CALL")) {
				stack.push("");
				padding = padString();
				list.add("");
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
					padding = padString();
					list.add(padding + line);
				}
			}
		}
		input.close();
		return list;
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

	public ArrayList<String> createTreeWithAnnotation(ArrayList<String> list) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(
				"ENTER FILENAME HERE"));
		int count = 1;
		ArrayList<String> nlist = new ArrayList<>();
		for (int i = 0; i < list.size() - 2; i++) {
			if (list.get(i).equals("")) {
				output.newLine();
				continue;
			}
			if (list.get(i).contains("CALL")) {
				if (list.get(i).equals(list.get(i + 2))) {
					count++;
					i++;
				} else {
					output.write(list.get(i) + " <COUNT>" + count);
					output.newLine();
					nlist.add(list.get(i) + " <COUNT>" + count);
					count = 1;
				}
			} else if (list.get(i).contains("RETURN")) {
				output.write(list.get(i));
				output.newLine();
			}
		}
		System.out.println("CCT generated");
		output.close();
		return nlist;
	}

	public void generateKLengthSequences(ArrayList<String> list) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(
				"ENTER FILENAME HERE"));
		Scanner sc = new Scanner(System.in);
		int seqlen = 1;
		System.out.println("Enter the threshold length k");
		int klength = sc.nextInt();
		while (++seqlen <= klength) {
			HashMap<String, Pattern> pattern = new HashMap<>();
			for (int i = 0; i < list.size() - seqlen; i++) {
				String str = list.get(i);
				String patarr[] = str.trim().split(" ");
				String pat = patarr[1];
				Pattern p = pattern.get(pat);
				if (p == null) {
					p = new Pattern();
					p.setNxtline(new ArrayList<String>());
					int k = 1;
					while (k < seqlen) {
						String cmparr[] = list.get(i + k).trim().split(" ");
						String strnxt = cmparr[1];
						p.getNxtline().add(strnxt); // add
						k++;
					}
					p.count = p.count + 1;
				} else {
					int k = 1;
					while (k < seqlen) {
						String cmparr[] = list.get(i + k).trim().split(" ");
						String strnxt = cmparr[1];
						if (!p.getNxtline().get(k - 1).equals(strnxt))
							break;
						k++;
					}
					if (k == seqlen)
						p.count = p.count + 1;
				}
				pattern.put(pat, p);
			}
			for (Map.Entry<String, Pattern> entry : pattern.entrySet()) {
				output.write("" + seqlen);
				output.write(",");
				String arr[] = entry.getKey().split("/");
				output.write(arr[arr.length - 1] + " ");
				for (String value : entry.getValue().nxtline) {
					String arrv[] = value.split("/");
					output.write(arrv[arrv.length - 1] + " ");
				}
				output.write(",");
				output.write("" + entry.getValue().getCount());
				output.newLine();
			}
		}
		output.close();
		System.out.println("All sequence till length " + klength + " are generated");
	}
}