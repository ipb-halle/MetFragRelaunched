package de.ipbhalle.metfragweb.helper;

import java.io.File;

import org.primefaces.model.DefaultStreamedContent;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterSDF;
import de.ipbhalle.metfraglib.writer.CandidateListWriterXLS;
import de.ipbhalle.metfraglib.writer.CandidateWriterXLS;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.datatype.Molecule;
import de.ipbhalle.metfragweb.datatype.ScoreSummary;
import de.ipbhalle.metfragweb.datatype.UploadedSuspectListFile;

public class UserOutputDataHandler {

	protected BeanSettingsContainer beanSettingsContainer;
	protected FileStorer fileStorer;
	
	public UserOutputDataHandler(BeanSettingsContainer beanSettingsContainer) {
		this.beanSettingsContainer = beanSettingsContainer;
		this.fileStorer = new FileStorer();
	}

	/**
	 * retrieve the resource for one single candidate
	 * 
	 * @param metfragResult
	 * @return
	 * @throws Exception
	 */
	public org.primefaces.model.StreamedContent generatedCandidateDownloadFile(MetFragResult metfragResult, Settings settings) throws Exception {
		org.primefaces.model.StreamedContent resource = new DefaultStreamedContent(System.in, "application/vnd.ms-excel", "MetFragWeb_Candidate.xls" );
		if(metfragResult == null) {
			System.out.println("generatedCandidateDownloadFile null");
			return resource;
		}
		resource = new DefaultStreamedContent(System.in, "application/vnd.ms-excel", "MetFragWeb_Candidate_" + metfragResult.getIdentifier() + ".xls" );
		
		//only main molecule is written to the output
		Molecule root = metfragResult.getRoot();
		//generate candidate for the write and set all necessary properties
		ICandidate candidate = new PrecursorCandidate(root.getInChI(), root.getIdentifier());
		candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, String.valueOf(root.getMass()));
		candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, root.getFormula());
		candidate.setProperty(VariableNames.SMILES_NAME, root.getSMILES());
		candidate.setMatchList(root.getMatchList());
		if(root.getName().length() != 0) candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, root.getName());
		ScoreSummary[] scoreSummary = root.getScoreSummary();
		for(int i = 0; i < scoreSummary.length; i++) {
			candidate.setProperty(scoreSummary[i].getName(), String.valueOf(scoreSummary[i].getRawValue()));
		}
		
		CandidateWriterXLS writer = new CandidateWriterXLS();
		ScoredCandidateList scoredCandidateListSingle = new ScoredCandidateList();
		scoredCandidateListSingle.addElement(candidate);
		
		java.io.File folder = new java.io.File(this.beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "downloads");
		boolean folderExists = true;
		if (!folder.exists())
			folderExists = folder.mkdirs();
		if (!folderExists)
			throw new Exception();
		
		try {
			System.out.println("generating resource");
			if(writer.write(scoredCandidateListSingle, "MetFragWeb_Candidate_" + candidate.getIdentifier(), folder.getAbsolutePath(), settings)) {
				String filePath = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Candidate_" + candidate.getIdentifier() + ".xls";
				resource = new org.primefaces.model.DefaultStreamedContent(new java.io.FileInputStream(new java.io.File(filePath)), "application/vnd.ms-excel", "MetFragWeb_Candidate_" + candidate.getIdentifier() + ".xls");
			}	
			else return resource;
		} catch (Exception e) {
			e.printStackTrace();
			return resource;
		}
		System.out.println("resource generated");
		return resource;
	}
	
	/**
	 * 
	 * @param errorMessages
	 * @param pathToProperties
	 * @return
	 * @throws Exception
	 */
	public org.primefaces.model.StreamedContent getDownloadParameters(Messages errorMessages, String pathToProperties) throws Exception {
		org.primefaces.model.StreamedContent resource = new org.primefaces.model.DefaultStreamedContent(System.in, "application/zip", "MetFragWeb_Parameters.zip");
		//this.deactivateDownloadCandidatesButton();
		try {
			File storeFolder = this.fileStorer.prepareFolder(this.beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "downloads" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "parameters", errorMessages);
			if(storeFolder == null) return resource;

			// create settings object
			this.beanSettingsContainer.prepareSettingsObject();
			String databasename = (String)this.beanSettingsContainer.getMetFragSettings().get(VariableNames.METFRAG_DATABASE_TYPE_NAME);
			if(databasename.equals("LocalPubChem")) this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "PubChem");
			if(databasename.equals("LocalKegg")) this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "KEGG");
			if(databasename.equals("LocalExtendedPubChem")) this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "ExtendedPubChem");
			if(databasename.equals("MetChem")) {
				String library = (String)this.beanSettingsContainer.getMetFragSettings().get(VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME);
				if(library.equals("pubchem")) {
					this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "PubChem");
					this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME);
				}
				else if(library.equals("kegg")) {
					this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "KEGG");
					this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME);
				}
			} else {
				this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME);
			}
			this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.PEAK_LIST_STRING_NAME);
			this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.LOCAL_DATABASES_FOLDER_FOR_WEB);
			
			// setting missing values
			this.beanSettingsContainer.getMetFragSettings().set(VariableNames.SAMPLE_NAME, "MetFragWeb_Sample");
			this.beanSettingsContainer.getMetFragSettings().set(VariableNames.STORE_RESULTS_PATH_NAME, ".");
			this.beanSettingsContainer.getMetFragSettings().set(VariableNames.PEAK_LIST_PATH_NAME, "MetFragWeb_Peaklist.txt");
			
			if(this.beanSettingsContainer.getDatabase().equals("ChemSpider")) 
				this.beanSettingsContainer.getMetFragSettings().set(VariableNames.CHEMSPIDER_TOKEN_NAME, "");
			else 
				this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.CHEMSPIDER_TOKEN_NAME);
			/*
			if(this.beanSettingsContainer.getDatabase().equals("ChemSpiderRest")) 
				this.beanSettingsContainer.getMetFragSettings().set(VariableNames.CHEMSPIDER_REST_TOKEN_NAME, "");
			else 
				this.beanSettingsContainer.getMetFragSettings().remove(VariableNames.CHEMSPIDER_REST_TOKEN_NAME);
			*/
			// in case local database is defined, set the parameter to the
			// candidates
			if (this.beanSettingsContainer.isLocalDatabaseDefined()) {
				this.beanSettingsContainer.getMetFragSettings().set(VariableNames.LOCAL_DATABASE_PATH_NAME, new java.io.File(this.beanSettingsContainer.getCandidateFilePath()).getName());
			} else if(this.beanSettingsContainer.getMetFragSettings().containsKey(VariableNames.LOCAL_DATABASE_PATH_NAME) && this.beanSettingsContainer.getMetFragSettings().get(VariableNames.LOCAL_DATABASE_PATH_NAME) != null) {
				this.beanSettingsContainer.getMetFragSettings().set(VariableNames.LOCAL_DATABASE_PATH_NAME, new java.io.File((String)this.beanSettingsContainer.getMetFragSettings().get(VariableNames.LOCAL_DATABASE_PATH_NAME)).getName());
			}

			byte[] buffer = new byte[1024];
			System.out.println("generating zip archive");
			java.io.FileOutputStream fos = new java.io.FileOutputStream(storeFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Parameters.zip");
			java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
			// writing peaklist file
			java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Peaklist.txt");
			zos.putNextEntry(ze);
			java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(this.beanSettingsContainer.getPeakList().getBytes());
			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			if (this.beanSettingsContainer.isLocalDatabaseDefined()) {
				ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + new java.io.File(this.beanSettingsContainer.getCandidateFilePath()).getName());
				System.out.println("adding local database file to archive");
				zos.putNextEntry(ze);
				java.io.FileInputStream inFile = new java.io.FileInputStream(this.beanSettingsContainer.getCandidateFilePath());
				while ((len = inFile.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				inFile.close();

			}
			// write retentiontime file
			if (this.beanSettingsContainer.isScoreEnabled("retentionTimeTrainingFile")) {
				ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "retentiontimescore" + Constants.OS_SPECIFIC_FILE_SEPARATOR + new java.io.File(this.beanSettingsContainer.getRetentionTimeScoreTrainingFilePath()).getName());
				System.out.println("adding retention time file to archive");
				zos.putNextEntry(ze);
				java.io.FileInputStream inFile = new java.io.FileInputStream(this.beanSettingsContainer.getRetentionTimeScoreTrainingFilePath());
				while ((len = inFile.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				inFile.close();
				this.beanSettingsContainer.getMetFragSettings().set(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME, "retentiontimescore" + Constants.OS_SPECIFIC_FILE_SEPARATOR + this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName());
			}
			// write suspect lists for filter
			if (this.beanSettingsContainer.isFilterEnabled("suspectListsFilter")) {
				System.out.println("adding suspect filter files to archive");
				if (this.beanSettingsContainer.getSuspectListFilterFileContainer() != null) {
					String[] filenames = new String[this.beanSettingsContainer.getSuspectListFilterFileContainer().size()];
					for (int i = 0; i < this.beanSettingsContainer.getSuspectListFilterFileContainer().size(); i++) {
						UploadedSuspectListFile file = this.beanSettingsContainer.getSuspectListFilterFileContainer().get(i);
						ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsfilter" + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getName());
						zos.putNextEntry(ze);
						java.io.FileInputStream inFile = new java.io.FileInputStream(file.getAbsolutePath());
						while ((len = inFile.read(buffer)) > 0) {
							zos.write(buffer, 0, len);
						}
						inFile.close();
						filenames[i] = "suspectlistsfilter" + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getName();
					}
					this.beanSettingsContainer.getMetFragSettings().set(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME, filenames);
				}
			}
			// write suspect lists for score
			if (this.beanSettingsContainer.isScoreEnabled("suspectListsScore")) {
				System.out.println("adding suspect score files to archive");
				if (this.beanSettingsContainer.getSuspectListScoreFileContainer() != null) {
					String[] filenames = new String[this.beanSettingsContainer.getSuspectListScoreFileContainer().size()];
					for (int i = 0; i < this.beanSettingsContainer.getSuspectListScoreFileContainer().size(); i++) {
						UploadedSuspectListFile file = this.beanSettingsContainer.getSuspectListScoreFileContainer().get(i);
						ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsscore" + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getName());
						zos.putNextEntry(ze);
						java.io.FileInputStream inFile = new java.io.FileInputStream(file.getAbsolutePath());
						while ((len = inFile.read(buffer)) > 0) {
							zos.write(buffer, 0, len);
						}
						inFile.close();
						filenames[i] = "suspectlistsscore" + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getName();
					}
					this.beanSettingsContainer.getMetFragSettings().set(VariableNames.SCORE_SUSPECT_LISTS_NAME, filenames);
				}
			}
			//include readme
			if(pathToProperties != null) {
				java.io.File readMeFile = new java.io.File(pathToProperties);
				if(readMeFile.exists()) {
					ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "README.txt");
					zos.putNextEntry(ze);
					java.io.FileInputStream inFile = new java.io.FileInputStream(readMeFile.getAbsolutePath());
					while ((len = inFile.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
					inFile.close();
				}
			}
			
			// write cfg file
			this.beanSettingsContainer.getMetFragSettings().writeSettingsFile(storeFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Parameters.cfg");

			String file = storeFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Parameters.cfg";
			ze = new java.util.zip.ZipEntry("MetFragWeb_Parameters" + Constants.OS_SPECIFIC_FILE_SEPARATOR + new java.io.File(file).getName());
			System.out.println("adding cfg file to archive");
			zos.putNextEntry(ze);
			java.io.FileInputStream inFile = new java.io.FileInputStream(file);
			while ((len = inFile.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			inFile.close();
			zos.closeEntry();
			zos.close();
			resource = new org.primefaces.model.DefaultStreamedContent(new java.io.FileInputStream(new java.io.File(storeFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Parameters.zip")), "application/zip", "MetFragWeb_Parameters.zip");
		} catch (Exception e) {
			errorMessages.setMessage("buttonDownloadParametersDatabaseError", "Error when downloading parameters.");
			errorMessages.removeKey("buttonDownloadParametersFilterError");
			errorMessages.removeKey("buttonDownloadParametersScoreError");
			throw new Exception();
		}
		System.out.println("resource successfully generated");
		return resource;
	}
	
	/**
	 * 
	 * @param format
	 * @param errorMessages
	 * @return
	 */
	public org.primefaces.model.StreamedContent createCandidatesToDownload(String format, Messages errorMessages) {
		System.out.println("downloadCandidates " + format);
		IWriter candidateWriter = null;
		String filePath = "";
		org.primefaces.model.StreamedContent resource = new org.primefaces.model.DefaultStreamedContent(System.in, "text/csv", "MetFragWeb_Candidates." + format);
		
		try {
			java.io.File folder = new java.io.File(this.beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "downloads");
			System.out.println("creating folder " + folder.getAbsolutePath());
			boolean folderExists = true;
			if (!folder.exists())
				folderExists = folder.mkdirs();
			if (!folderExists)
				throw new Exception();
			String mimetype = "";
			if (format.equals("csv")) {
				candidateWriter = new CandidateListWriterCSV();
				mimetype = "text/csv";
			} else if (format.equals("xls")) {
				candidateWriter = new CandidateListWriterXLS();
				mimetype = "application/vnd.ms-excel";
			} else if (format.equals("sdf")) {
				candidateWriter = new CandidateListWriterSDF();
				mimetype = "chemical/x-mdl-sdfile";
			} else {
				System.out.println("Error: Unknown format " + format);
			}
			candidateWriter.write(this.beanSettingsContainer.getRetrievedCandidateList(), "MetFragWeb_Candidates", folder.getAbsolutePath());
			filePath = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Candidates." + format;
			resource = new org.primefaces.model.DefaultStreamedContent(new java.io.FileInputStream(new java.io.File(filePath)), mimetype, "MetFragWeb_Candidates." + format);
			errorMessages.removeKey("buttonDownloadCompoundsError");
		} catch (Exception e) {
			errorMessages.setMessage("buttonDownloadCompoundsError", "Error when downloading candidates.");
			return resource;
		}
		System.out.println("success " + resource.getContentType() + " " + resource.getName());
		return resource;
	}
	
	/**
	 * 
	 * @param format
	 * @param errorMessages
	 * @return
	 */
	public org.primefaces.model.StreamedContent createResultsToDownload(MetFragResultsContainer metfragResults, String format, Messages errorMessages) {
		System.out.println("downloadResults " + format);
		IWriter candidateWriter = null;
		String filePath = "";
		org.primefaces.model.StreamedContent resource = new org.primefaces.model.DefaultStreamedContent(System.in, "text/csv", "MetFragWeb_Candidates." + format);
		
		try {
			java.io.File folder = new java.io.File(this.beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "downloads");
			System.out.println("creating folder " + folder.getAbsolutePath());
			boolean folderExists = true;
			if (!folder.exists())
				folderExists = folder.mkdirs();
			if (!folderExists)
				throw new Exception();
			String mimetype = "";
			if (format.equals("csv")) {
				candidateWriter = new CandidateListWriterCSV();
				mimetype = "text/csv";
			} else if (format.equals("xls")) {
				candidateWriter = new CandidateListWriterXLS();
				mimetype = "application/vnd.ms-excel";
			} else if (format.equals("sdf")) {
				candidateWriter = new CandidateListWriterSDF();
				mimetype = "chemical/x-mdl-sdfile";
			} else {
				System.out.println("Error: Unknown format " + format);
			}
			
			candidateWriter.write(metfragResults.getScoredCandidateList(), "MetFragWeb_Results", folder.getAbsolutePath());
			filePath = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWeb_Results." + format;
			resource = new org.primefaces.model.DefaultStreamedContent(new java.io.FileInputStream(new java.io.File(filePath)), mimetype, "MetFragWeb_Candidates." + format);
			errorMessages.removeKey("buttonDownloadResultsError");
		} catch (Exception e) {
			e.printStackTrace();
			errorMessages.setMessage("buttonDownloadResultsError", "Error when downloading candidates.");
			return resource;
		}
		System.out.println("success " + resource.getContentType() + " " + resource.getName());
		return resource;
	}

}
