package de.ipbhalle.metfrag.phenomenal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * rename param names in phenomenal galaxy xml files
 * 
 * @author cruttkie
 *
 */
public class RenameParamNames {

	public static String fileContent = "";
	public static String toolName = "";
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		
		Vector<String> variableNames = new Vector<String>();
		Vector<String> variableNamesTypeDataInput = new Vector<String>();
		Vector<String> variableNamesTypeDataOutput = new Vector<String>();
		
		String line = "";
		while((line = breader.readLine()) != null) {
			fileContent += line + "\n";
			line = line.trim().replaceAll("^\\s+", "");
			line = line.trim().replaceAll("^\\t+", "");

			if(line.startsWith("<tool")) {
				toolName = line.split("id=\"")[1].split("\"")[0];
			}
			if(line.startsWith("<command>")) {
				readVariableNamesFromCommand(variableNames, breader);
			}
			if(line.startsWith("<inputs>")) {
				readVariableNamesFromInput(variableNamesTypeDataInput, breader);
			}
			if(line.startsWith("<outputs>")) {
				readVariableNamesFromOutput(variableNamesTypeDataOutput, breader);
			}
		}

		breader.close();
		System.out.println("found " + variableNames.size() + " variables in command section");
		
		int inputIndex = 1;
		for(String name : variableNamesTypeDataInput) {
			System.out.println("input " + name);
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+\n", "\\$" + toolName + "-input-" + inputIndex + "\n");
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+", "\\$" + toolName + "-input-" + inputIndex + " ");
			fileContent = fileContent.replaceAll("\\$" + name + ";", "\\$" + toolName + "-input-" + inputIndex + ";");
			fileContent = fileContent.replaceAll("\\$" + name + "\n", "\\$" + toolName + "-input-" + inputIndex + "\n");
			fileContent = fileContent.replaceAll("\"" + name + "\"", "\"" + toolName + "-input-" + inputIndex + "\"");
			inputIndex++;
		}

		int outputIndex = 1;
		for(String name : variableNamesTypeDataOutput) {
			System.out.println("output " + name);
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+\n", "\\$" + toolName + "-output-" + outputIndex + "\n");
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+", "\\$" + toolName + "-output-" + outputIndex + " ");
			fileContent = fileContent.replaceAll("\\$" + name + ";", "\\$" + toolName + "-output-" + outputIndex + ";");
			fileContent = fileContent.replaceAll("\\$" + name + "\n","\\$" +  toolName + "-output-" + outputIndex + "\n");
			fileContent = fileContent.replaceAll("\"" + name + "\"", "\"" + toolName + "-output-" + outputIndex + "\"");
			outputIndex++;
		}
		
		System.out.println(fileContent);
		
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(filename)));
		bwriter.write(fileContent);
		bwriter.close();
		
	}

	/**
	 * 
	 * @param variableNames
	 * @throws IOException 
	 */
	public static void readVariableNamesFromInput(Vector<String> variableNamesTypeData, BufferedReader breader) throws IOException {
		String line = "";
		while((line = breader.readLine()) != null) {
			fileContent += line + "\n";
			line = line.trim().replaceAll("^\\s+", "");
			if(line.endsWith("</inputs>")) return;
			if(line.startsWith("<param")) {
				String name = line.split("name=\"")[1].split("\"")[0];
				String type = line.split("type=\"")[1].split("\"")[0];
				if(type.equals("data") && !variableNamesTypeData.contains(name)) variableNamesTypeData.addElement(name);
			}
		}
	}

	/**
	 * 
	 * @param variableNames
	 * @throws IOException 
	 */
	public static void readVariableNamesFromOutput(Vector<String> variableNamesTypeData, BufferedReader breader) throws IOException {
		String line = "";
		while((line = breader.readLine()) != null) {
			fileContent += line + "\n";
			line = line.trim().replaceAll("^\\s+", "");
			if(line.endsWith("</outputs>")) return;
			if(line.startsWith("<data")) {
				String name = line.split("name=\"")[1].split("\"")[0];
			//	String type = line.split("type=\"")[1].split("\"")[0];
				if(!variableNamesTypeData.contains(name)) variableNamesTypeData.addElement(name);
			}
		}
	}
	
	/**
	 * 
	 * @param variableNames
	 * @throws IOException 
	 */
	public static void readVariableNamesFromCommand(Vector<String> variableNames, BufferedReader breader) throws IOException {
		String line = "";
		while((line = breader.readLine()) != null) {
			fileContent += line + "\n";
			line = line.trim().replaceAll("^\\s+", "");
			if(line.endsWith("</command>")) return;
			int stringindex = 0;
			while(stringindex < line.length()) {
				if(line.charAt(stringindex) == '$' && (stringindex != 0 || line.charAt(stringindex - 1) != '\\')) {
					//read variable name
					String paramName = "";
					stringindex++;
					while(stringindex < line.length() && (
							line.charAt(stringindex) != ';' && 
							line.charAt(stringindex) != ' ' && 
							line.charAt(stringindex) != ':' &&
							line.charAt(stringindex) != '=' )) {
						paramName += line.charAt(stringindex);
						stringindex++;
					}
					if(!variableNames.contains(paramName)) variableNames.addElement(paramName);
				}
				stringindex++;
			}
		}
	}
	
}
