package de.ipbhalle.metfrag.phenomenal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.mztab.MzTabFileExportMethod;

public class WriteMetFragToMZtab {

	public static java.util.Hashtable<String, String> argsHash;

	public static void main(String[] args) {
		if(!getArgs(args)) {
			System.err.println("Error reading parameters.");
			System.exit(1);
		}
		
		String metfragFolder = argsHash.get("metfragFolder");
		int numberCandidates = Integer.parseInt(argsHash.get("numberCandidates"));
		String output = argsHash.get("output");
		
		// check metfrag result folder
		File resultFolder = new File(metfragFolder);
		if(!resultFolder.exists()) {
			System.err.println(resultFolder.getAbsolutePath() + " not found.");
			System.exit(2);
		}
		// get files to convert to mztab
		File[] files = resultFolder.listFiles();
		// mztab feature table
		FeatureTable candidateTable = MSDKObjectBuilder.getFeatureTable("candidateTable",
				DataPointStoreFactory.getMemoryDataStore());
		// define columns
		FeatureTableColumn<Integer> idColumn = MSDKObjectBuilder.getIdFeatureTableColumn();
		FeatureTableColumn<Double> mzColumn = MSDKObjectBuilder.getMzFeatureTableColumn();
		FeatureTableColumn<ChromatographyInfo> chromatographyInfoColumn = MSDKObjectBuilder.getChromatographyInfoFeatureTableColumn();
		FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn = MSDKObjectBuilder.getIonAnnotationFeatureTableColumn();
		FeatureTableColumn<Integer> chargeColumn = MSDKObjectBuilder.getChargeFeatureTableColumn();
		
		// add columns
		candidateTable.addColumn(idColumn);
		candidateTable.addColumn(mzColumn);
		candidateTable.addColumn(chromatographyInfoColumn);
		candidateTable.addColumn(ionAnnotationColumn);
		candidateTable.addColumn(chargeColumn);
		
		// current row number of feature table
		int rownumber = 1;
		for(int i = 0; i < files.length; i++) {
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, files[i].getAbsolutePath());
			
			IDatabase db = null;
			if(files[i].getName().endsWith("csv")) db = new LocalCSVDatabase(settings);
			else db = new LocalPSVDatabase(settings);
			
			Float rt = 0.0f;
			Double mz = 0.0;
			
			try {
				String[] tmp = files[i].getName().split("_");
				if(tmp.length == 1) throw new Exception();
				rt = Float.parseFloat(tmp[0]);
				mz = Double.parseDouble(tmp[1]);
			} catch(Exception e) {
				System.out.println(files[i].getName() + " has no rt and mz information. Check file name.");
			}
			
			ArrayList<String> identifiers = null;
			try {
				identifiers = db.getCandidateIdentifiers();
			} catch (MultipleHeadersFoundInInputDatabaseException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			CandidateList candidates = null;
			try {
				candidates = db.getCandidateByIdentifier(identifiers);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			int candidateIndex = 0;
			
			// get candidates and store them in the FeatureTable
			while(candidateIndex < numberCandidates && candidateIndex < candidates.getNumberElements()) {
				ICandidate candidate = candidates.getElement(candidateIndex);
				FeatureTableRow currentRow = MSDKObjectBuilder.getFeatureTableRow(candidateTable, rownumber);
				FeatureTableColumn<Object> column;
				// Add common data to columns
				// Common column: Id
				column = candidateTable.getColumn(ColumnName.ID, null);
				
				currentRow.setData(column, Integer.valueOf(rownumber));
				// Common column: m/z
				column = candidateTable.getColumn(ColumnName.MZ, null);
				currentRow.setData(column, mz);
				// Annotation
				column = candidateTable.getColumn(ColumnName.IONANNOTATION, null);
				List<IonAnnotation> ionAnnotations = new ArrayList<IonAnnotation>();
				IonAnnotation ionAnnotation = MSDKObjectBuilder.getIonAnnotation();
				ionAnnotation.setAnnotationId(candidate.getIdentifier());
				try {
					ionAnnotation.setChemicalStructure(candidate.getAtomContainer());
					ionAnnotation.setFormula(MolecularFormulaManipulator
							.getMolecularFormula(candidate.getAtomContainer()));
					ionAnnotation.setInchiKey(InChIGeneratorFactory.getInstance()
							.getInChIGenerator(candidate.getAtomContainer()).getInchiKey());
				} catch (Exception e) {
					candidateIndex++;
					continue;
				}
				ionAnnotation.setDescription(
						(String) candidate.getProperty(VariableNames.COMPOUND_NAME_NAME));
				ionAnnotations.add(ionAnnotation);
				//ionAnnotation.setExpectedMz(metfrag_settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
				currentRow.setData(column, ionAnnotations);
				
				// RT
				if (rt != null) {
					ChromatographyInfo cgInfo = MSDKObjectBuilder.getChromatographyInfo1D(SeparationType.LC, rt);
					FeatureTableColumn<ChromatographyInfo> rtcolumn = candidateTable.getColumn("Chromatography Info", null, ChromatographyInfo.class);
					currentRow.setData(rtcolumn, cgInfo);
				}
				
				// Add row to feature table
				candidateTable.addRow(currentRow);
				rownumber++;
				candidateIndex++;
			}
		}
		
		// write out mzTab file
		File outputFile = new File(output);
		MzTabFileExportMethod method = new MzTabFileExportMethod(candidateTable, outputFile, true);
		try {
			method.execute();
		} catch (MSDKException e) {
			e.printStackTrace();
			System.err.println("Could not write mzTab file.");
		}
	}
	

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("metfragFolder") && !tmp[0].equals("numberCandidates") && !tmp[0].equals("output")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("metfragFolder")) {
			System.err.println("no metfragFolder defined");
			return false;
		}

		if (!argsHash.containsKey("numberCandidates")) {
			System.err.println("no numberCandidates defined");
			return false;
		}

		if (!argsHash.containsKey("output")) {
			System.err.println("no output defined");
			return false;
		}
		
		return true;
	}

	
}
