package de.ipbhalle.metfraglib.converter;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class PubChemXLStoCSV {

	public static String[] neededFields = { "PUBCHEM_COMPOUND_CID",
			"PUBCHEM_IUPAC_INCHI", "PUBCHEM_MONOISOTOPIC_WEIGHT",
			"PUBCHEM_MOLECULAR_FORMULA", "PUBCHEM_IUPAC_INCHIKEY",
			"PUBCHEM_OPENEYE_ISO_SMILES", "PUBCHEM_IUPAC_OPENEYE_NAME",
			"PUBCHEM_XLOGP3", "PUBCHEM_XLOGP3_AA" };

	public static void main(String[] args) {
		File inputWorkbook = new File(args[0]);
		Workbook w;
		Hashtable<String, Integer> colNameToColNum = new Hashtable<String, Integer>();

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			Sheet sheet = w.getSheet(0);

			int numColumns = sheet.getColumns();
			int numRows = sheet.getRows();

			for (int i = 0; i < numColumns; i++) {
				Cell cell = sheet.getCell(i, 0);
				colNameToColNum.put(cell.getContents().trim(), i);
			}

			for (int j = 1; j < numRows; j++) {
				Cell cell = sheet.getCell(colNameToColNum.get(neededFields[0]), j);
				System.out.print(cell.getContents().trim());
				for (int i = 1; i < neededFields.length; i++) {
					try {
						cell = sheet.getCell(colNameToColNum.get(neededFields[i]), j);
					} catch(NullPointerException e) {
						if(i == 4) System.out.print("||");
						else System.out.print("|");
						continue;
					}
					if(i == 4) {
						String inchikey = cell.getContents().trim();
						String[] tmp = inchikey.split("-");
						try {
							System.out.print("|" + tmp[0] + "|" + tmp[1]);
						} catch(Exception e) {
							System.out.print("||");
						}
					}
					else System.out.print("|" + cell.getContents().trim());
				}
				System.out.println();
			}
			w.close();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
