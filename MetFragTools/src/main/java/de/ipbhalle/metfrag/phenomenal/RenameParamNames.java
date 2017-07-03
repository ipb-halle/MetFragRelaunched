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
	public static boolean renameVariables = false;
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		if(args.length > 1) {
			if(args[1].toLowerCase().equals("true")) renameVariables = true;
		}
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		
		Vector<String> variableNames = new Vector<String>();
		Vector<String> variableNamesTypeDataInput = new Vector<String>();
		Vector<String> variableNamesTypeDataOutput = new Vector<String>();
		
		String line = "";
		while((line = breader.readLine()) != null) {
			String line_tmp = line.trim().replaceAll("^\\s+", "");
			line_tmp = line_tmp.trim().replaceAll("^\\t+", "");
			if(line_tmp.startsWith("<requirements>")) {
				readAndAdaptRequirements(breader, line);
			} else {
				fileContent += line + "\n";
				line = line.trim().replaceAll("^\\s+", "");
				line = line.trim().replaceAll("^\\t+", "");
	
				if(line.startsWith("<tool")) {
					toolName = line.split("id=\"")[1].split("\"")[0];
				}
				if(renameVariables && line.startsWith("<command>")) {
					readVariableNamesFromCommand(variableNames, breader);
				}
				if(renameVariables && line.startsWith("<inputs>")) {
					readVariableNamesFromInput(variableNamesTypeDataInput, breader);
				}
				if(renameVariables && line.startsWith("<outputs>")) {
					readVariableNamesFromOutput(variableNamesTypeDataOutput, breader);
				}
			}
		}

		breader.close();
		System.out.println("found " + variableNames.size() + " variables in command section");
		
		int inputIndex = 1;
		for(String name : variableNamesTypeDataInput) {
			String file_toolName = toolName.replaceAll("-", "_");
			System.out.println("input " + name);
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+\n", "\\$" + file_toolName + "_input_" + inputIndex + "\n");
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+", "\\$" + file_toolName + "_input_" + inputIndex + " ");
			fileContent = fileContent.replaceAll("\\$" + name + ";", "\\$" + file_toolName + "_input_" + inputIndex + ";");
			fileContent = fileContent.replaceAll("\\$" + name + "\n", "\\$" + file_toolName + "_input_" + inputIndex + "\n");
			fileContent = fileContent.replaceAll("\"" + name + "\"", "\"" + file_toolName + "_input_" + inputIndex + "\"");
			inputIndex++;
		}

		int outputIndex = 1;
		for(String name : variableNamesTypeDataOutput) {
			String file_toolName = toolName.replaceAll("-", "_");
			System.out.println("output " + name);
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+\n", "\\$" + file_toolName + "_output_" + outputIndex + "\n");
			fileContent = fileContent.replaceAll("\\$" + name + "\\s+", "\\$" + file_toolName + "_output_" + outputIndex + " ");
			fileContent = fileContent.replaceAll("\\$" + name + ";", "\\$" + file_toolName + "_output_" + outputIndex + ";");
			fileContent = fileContent.replaceAll("\\$" + name + "\n","\\$" +  file_toolName + "_output_" + outputIndex + "\n");
			fileContent = fileContent.replaceAll("\"" + name + "\"", "\"" + file_toolName + "_output_" + outputIndex + "\"");
			outputIndex++;
		}
		
	//	System.out.println(fileContent);
		
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
	public static void readAndAdaptRequirements(BufferedReader breader, String lastLine) throws IOException {
		Vector<String> toAdd = new Vector<String>();
		toAdd.add(lastLine);
		String line = "";
		int containerLine = -1;
		int index = 1;
		while((line = breader.readLine()) != null) {
			String line_tmp = line.trim().replaceAll("^\\s+", "");
			if(line_tmp.length() == 0) continue;
			if(line_tmp.startsWith("<container type=\"docker\">")) {
				line = line.replaceAll(">phnmnl", ">container-registry.phenomenal-h2020.eu/phnmnl");
				line = line.replaceAll(">docker-registry", ">container-registry");
				toAdd.add(line);
				containerLine = index;
			} else {
				toAdd.add(line);
				if(line_tmp.endsWith("</requirements>")) {
					break;
				}
			}
			index++;
		}
		if(toAdd.size() == 3) {
			toAdd.setElementAt("<!-- " + toAdd.get(0), 0);
			toAdd.setElementAt(toAdd.get(2) + "-->", 2);
		} else {
			toAdd.setElementAt("<!-- " + toAdd.get(containerLine) + "-->", containerLine);
		}
		for(int i = 0; i < toAdd.size(); i++) {
			fileContent += toAdd.get(i) + "\n";
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
				if(line.charAt(stringindex) == '$' && (stringindex == 0 || line.charAt(stringindex - 1) != '\\')) {
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
