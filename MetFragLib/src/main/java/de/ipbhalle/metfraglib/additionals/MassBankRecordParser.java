/*
*
* Copyright (C) 2009-2010 IPB Halle, Franziska Taruttis
*
* Contact: ftarutti@ipb-halle.de
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package de.ipbhalle.metfraglib.additionals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * The Class NewMassbankParser.
 */
public class MassBankRecordParser {

	//Compound information

	/** The Constant CH$. */
	private final static String CH$ = "CH$";

	//Sample information

	/** The Constant SP$. */
	private final static String SP$ = "SP$";

	//Analytical information

	/** The Constant AC$. */
	private final static String AC$ = "AC$";

	// Spectral information

	/** The Constant MS$. */
	private final static String MS$ = "MS$";

	// Peak information

	/** The Constant PK$. */
	private final static String PK$ = "PK$";

	/**
	 * Read the spectra.
	 *
	 * @param filename the filename
	 * @return the Spectrum
	 */
	public static double[][] Read(String filename)  {

		boolean readPeaks = false;

		java.util.Vector<String> peakLines = new java.util.Vector<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();

			while(line != null)
			{

				//read Record specific information				
				while(!line.contains("$") && line.contains(":"))
				{
					line = in.readLine();
				}

				//Compound information
				while(line.contains(CH$))
				{
					line = in.readLine();
				}
				//Sample information
				while(line.contains(SP$))
				{
					line = in.readLine();
				}
				//Analytical information
				while(line.contains(AC$))
				{
					line = in.readLine();
				}
				//Spectral information
				while(line.contains(MS$))
				{
					line = in.readLine();
				}
				//Peak information
				while(line.contains(PK$))
				{
					if(line.contains("PK$PEAK:"))
					{
						readPeaks=true;
						line = in.readLine();
					}
					else
					{
						line=in.readLine();
					}
				}
				//read Peaks
				if(line.equals("")) break;
				while(line != null && !line.contains("//") && readPeaks)/* && line!=null*/    
				{		
					peakLines.add(line);
					line = in.readLine();
				}
				line = in.readLine();
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double[][] spectrum = new double[peakLines.size()][2]; 
		for(int i = 0; i < peakLines.size(); i++) {
			String[] tmp = peakLines.get(i).trim().split("\\s+");
			/*
			 * assign mz value
			 */
			spectrum[i][0] = Double.valueOf(tmp[0]);
			/*
			 * assign intensity value
			 */
			spectrum[i][1] = Double.valueOf(tmp[1]);
		}
		return spectrum;
	}


	/**
	 * Adds the elements to map.
	 *
	 * @param line the line of massbank file
	 * @param map the map
	 * @return the map
	 */
	public static Map<String,ArrayList<String>> addElementsToMap(String line, Map<String,ArrayList<String>> map)
	{

		line = cleanLineFromHTML(line);
		String[] splitString = new String[line.split(":").length];
		splitString = line.split(":");

		if(splitString.length>2)
		{
			for (int i = 2; i < splitString.length; i++) {
				splitString[1] += ":"+splitString[i]; 
			}
		}

		if(map.containsKey(splitString[0]))
		{

			ArrayList<String> elements = map.get(splitString[0]);
			elements.add(splitString[1]);
			map.put(splitString[0], elements );	
		}
		else{

			ArrayList<String> elements = new ArrayList<String>(); 
			elements.add(splitString[1]);
			map.put(splitString[0], elements );	
		}

		return map;
	}

	public static boolean isValidRecord(String filename) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		boolean isPeaklist = false;
		while((line = breader.readLine()) != null) {
			if(line.contains("PK$NUM_PEAK") && line.contains("N/A")) {
				breader.close();
				return false;
			}
			if(line.contains("PK$PEAK")) {
				isPeaklist = true;
				continue;
			}
			if(isPeaklist && line.contains("N/A")) {
				breader.close();
				return false;
			}
		}
		breader.close();
		return true;
	}
	
	public static String cleanLineFromHTML(String line) {
		return line.replaceAll("<.*?>", "");
	}
	
	public static void main(String[] args) {
		double[][] values = MassBankRecordParser.Read("/home/cruttkie/svn/record/Eawag/EA020705.txt");
		for(int i = 0; i < values.length; i++) {
			System.out.println(values[i][0] + " " + values[i][1]);
		}
	}
}