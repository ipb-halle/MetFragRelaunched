package de.ipbhalle.metfragweb.helper;

import java.io.File;

import org.primefaces.model.UploadedFile;

import de.ipbhalle.metfraglib.exceptions.RetentionTimeNotFoundException;
import de.ipbhalle.metfraglib.exceptions.TooFewCandidatesException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.datatype.UploadedSuspectListFile;

public class UserInputDataHandler {

	protected FileStorer fileStorer;
	
	public UserInputDataHandler(BeanSettingsContainer beanSettingsContainer) {
		this.fileStorer = new FileStorer();
	}
	
	/**
	 * 
	 * @param candidateFile
	 * @param infoMessages
	 * @param errorMessages
	 * @return
	 */
	public void handleLocalCandidateFile(UploadedFile candidateFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		//check user input
		if(candidateFile == null) {
			errorMessages.setMessage("candidateFileError", "Error: Candidate file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "candidatefiles", errorMessages);
		if(uploadFolder == null) return;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(candidateFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("candidateFileError", "Error: Candidate file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
		if(storedFile == null) return;
		//process the file
		beanSettingsContainer.setCandidateFilePath(storedFile.getAbsolutePath());
		try {
			beanSettingsContainer.preprocessLocalDatabaseCandidates();
		} catch (Exception e) {
			System.err.println("Candidate file was not processed correctly. Check file.");
			errorMessages.setMessage("candidateFileError", "Candidate file was not processed correctly. Check file.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
	}
	
	public void handleLocalCandidateFile(java.io.File suspectListScoreFile, 
			Messages infoMessages, Messages errorMessages, String fileName, BeanSettingsContainer beanSettingsContainer) {
		//check user input
		if(suspectListScoreFile == null) {
			errorMessages.setMessage("candidateFileError", "Error: Candidate file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "candidatefiles", errorMessages);
		if(uploadFolder == null) return;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListScoreFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("candidateFileError", "Error: Candidate file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
		if(storedFile == null) return;
		//process the file
		beanSettingsContainer.setCandidateFilePath(storedFile.getAbsolutePath());
		try {
			beanSettingsContainer.preprocessLocalDatabaseCandidates();
		} catch (Exception e) {
			System.err.println("Candidate file was not processed correctly. Check file.");
			errorMessages.setMessage("candidateFileError", "Candidate file was not processed correctly. Check file.");
			beanSettingsContainer.setCandidateFilePath("");
			return;
		}
	}
	
	/**
	 * 
	 * @param parameterZipFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param availableParameters
	 */
	public void handleParametersZipFile(UploadedFile parameterZipFile, Messages infoMessages, 
			Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		//check user input
		if(parameterZipFile == null) {
			errorMessages.setMessage("uploadParametersError", "Error: Uploading ZIP file failed");
			System.err.println("Error: Uploading file failed (event is null)");
			return;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "uploads", errorMessages);
		if(uploadFolder == null) return;
		//store the file
		java.util.zip.ZipFile zipFile = null;
		java.io.File[] contentFiles = null; 
		try {
			zipFile = new java.util.zip.ZipFile(this.fileStorer.saveUploadedFile(parameterZipFile, uploadFolder));
			contentFiles = this.fileStorer.decompressZipFile(zipFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("uploadParametersError", "Error: ZIP file could not be saved");
			System.out.println("Error: ZIP file could not be saved");
			return;
		}
		//write parameter data to the settings object of the backing bean
		beanSettingsContainer = new BeanSettingsContainer(beanSettingsContainer.getRootSessionFolder());
		//check and init all settings
		beanSettingsContainer.readUploadedSettings(contentFiles, infoMessages, errorMessages, beanSettingsContainer.getAvailableParameters(), beanSettingsContainer.getRootSessionFolder());
	}
	
	/**
	 * 
	 * @param retentionTimeTrainingFile
	 * @param infoMessages
	 * @param errorMessages
	 * @return
	 */
	public void handleRetentionTimeTrainingFile(UploadedFile retentionTimeTrainingFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer)  
	{
		if(retentionTimeTrainingFile == null) {
			errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Retention time training file was not saved.");
			return;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "retentiontimescore", errorMessages);
		if(uploadFolder == null) return;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(retentionTimeTrainingFile, uploadFolder);
			beanSettingsContainer.setRetentionTimeScoreTrainingFilePath(storedFile.getAbsolutePath());
		} catch(Exception e) {
			errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Retention time training file was not saved.");
			beanSettingsContainer.setRetentionTimeScoreTrainingFilePath("");
			return;
		}
		//preprocess
		try {
			beanSettingsContainer.preprocessRetentionTimeTrainingFile();
			if (beanSettingsContainer.getAvailablePartitioningCoefficients() == null || beanSettingsContainer.getAvailablePartitioningCoefficients().size() == 0) 
			{
				errorMessages.setMessage("retentionTimeTrainingFileError", "No proper value for a partitioning coefficient found. Check candidate and retention time file.");
				beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
				beanSettingsContainer.setRetentionTimeScoreTrainingFilePath("");
				return;
			}
		} catch (TooFewCandidatesException e1) {
			beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
			beanSettingsContainer.setRetentionTimeScoreTrainingFilePath("");
			beanSettingsContainer.setAvailablePartitioningCoefficients(null);
			errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Uploaded file contains too few values.");
		} catch (RetentionTimeNotFoundException e2) {
			beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
			beanSettingsContainer.setRetentionTimeScoreTrainingFilePath("");
			beanSettingsContainer.setAvailablePartitioningCoefficients(null);
			errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Uploaded file does not contain a RetentionTime field.");
		} catch (Exception e3) {
			e3.printStackTrace();
			beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
			beanSettingsContainer.setRetentionTimeScoreTrainingFilePath("");
			beanSettingsContainer.setAvailablePartitioningCoefficients(null);
			errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Could not read file. Missing/Wrong header?");
		}
	}
	
	/**
	 * 
	 * @param suspectListScoreFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param fileSize
	 * @param contentType
	 * @param identifier
	 * @return
	 */
	public boolean handleSuspectListFilterFile(UploadedFile suspectListFilterFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		if(suspectListFilterFile == null) {
			errorMessages.setMessage("suspectListsFilterError", "Error: Suspect list file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return false;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsfilter", errorMessages);
		if(uploadFolder == null) return false;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListFilterFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("suspectListsFilterError", "Error: Suspect list file was not saved.");
			return false;
		}
		if(storedFile == null) return false;
		try {
			String message = beanSettingsContainer.addUploadedSuspectListFilterFile(
				new UploadedSuspectListFile(storedFile.getName(), suspectListFilterFile.getSize() + " bytes", 
						suspectListFilterFile.getContentType(), "", storedFile.getAbsolutePath(), beanSettingsContainer.getSuspectListFilterFileIdentifier()));
			if(message.length() != 0) 	
				errorMessages.setMessage("suspectListsFilterError", message);
		} catch (Exception e) {
			errorMessages.setMessage("suspectListsFilterError", "Error reading file.");
			return false;
		}
		if (beanSettingsContainer.getSuspectListFilterFileContainer() == null || beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0) {
			beanSettingsContainer.setFilterValid(false, "suspectListsFilter");
			return false;
		}
		else {
			beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
		}
		beanSettingsContainer.incrementSuspectListFilterFileIdentifier();
		return true;
	}
	
	/**
	 * 
	 * @param suspectListFilterFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param identifier
	 * @return
	 */
	public boolean handleSuspectListFilterFile(java.io.File suspectListFilterFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		if(suspectListFilterFile == null) {
			errorMessages.setMessage("suspectListsFilterError", "Error: Suspect list file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return false;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsfilter", errorMessages);
		if(uploadFolder == null) return false;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListFilterFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("suspectListsFilterError", "Error: Suspect list file was not saved.");
			return false;
		}
		if(storedFile == null) return false;
		try {
			//get file properties
			java.net.URLConnection conn = new java.net.URL(uploadFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + suspectListFilterFile.getName()).openConnection();
			String contentType = conn.getContentType();
			java.io.FileInputStream fis = new java.io.FileInputStream(storedFile);
			long size = fis.getChannel().size();
			fis.close();
			//save file as UploadedSuspectListFile
			String message = beanSettingsContainer.addUploadedSuspectListFilterFile(
				new UploadedSuspectListFile(storedFile.getName(), size + " bytes", 
						contentType, "", storedFile.getAbsolutePath(), beanSettingsContainer.getSuspectListFilterFileIdentifier()));
			if(message.length() != 0) 	
				errorMessages.setMessage("suspectListsFilterError", message);
		} catch (Exception e) {
			errorMessages.setMessage("suspectListsFilterError", "Error reading file.");
			return false;
		}
		if (beanSettingsContainer.getSuspectListFilterFileContainer() == null || beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0) {
			beanSettingsContainer.setFilterValid(false, "suspectListsFilter");
			return false;
		}
		else {
			beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
		}
		beanSettingsContainer.incrementSuspectListFilterFileIdentifier();
		return true;
	}
	
	/**
	 * 
	 * @param suspectListScoreFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param fileSize
	 * @param contentType
	 * @param identifier
	 * @return
	 */
	public boolean handleSuspectListScoreFile(UploadedFile suspectListScoreFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		if(suspectListScoreFile == null) {
			errorMessages.setMessage("suspectListsScoreError", "Error: Suspect list file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return false;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsscore", errorMessages);
		if(uploadFolder == null) return false;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListScoreFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("candidateFileError", "Error: Suspect list file was not saved.");
			return false;
		}
		if(storedFile == null) return false;
		try {
			String message = beanSettingsContainer.addUploadedSuspectListScoreFile(
				new UploadedSuspectListFile(storedFile.getName(), suspectListScoreFile.getSize() + " bytes", 
						suspectListScoreFile.getContentType(), "", storedFile.getAbsolutePath(), beanSettingsContainer.getSuspectListScoreFileIdentifier()));
			if(message.length() != 0) 	
				errorMessages.setMessage("suspectListsScoreError", message);
		} catch (Exception e) {
			errorMessages.setMessage("suspectListsScoreError", "Error reading file.");
			return false;
		}
		if (beanSettingsContainer.getSuspectListScoreFileContainer() == null || beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) {
			beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			return false;
		}
		else {
			beanSettingsContainer.setScoreValid(true, "suspectListsScore");
		}
		beanSettingsContainer.incrementSuspectListScoreFileIdentifier();
		return true;
	}
	
	/**
	 * 
	 * @param suspectListScoreFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param identifier
	 * @return
	 */
	public boolean handleSuspectListScoreFile(File suspectListScoreFile, 
			Messages infoMessages, Messages errorMessages, BeanSettingsContainer beanSettingsContainer) {
		if(suspectListScoreFile == null) {
			errorMessages.setMessage("suspectListsScoreError", "Error: Suspect list file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return false;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsscore", errorMessages);
		if(uploadFolder == null) return false;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListScoreFile, uploadFolder);
		} catch(Exception e) {
			errorMessages.setMessage("candidateFileError", "Error: Suspect list file was not saved.");
			return false;
		}
		if(storedFile == null) return false;
		try {
			//get file properties
			java.net.URLConnection conn = new java.net.URL(suspectListScoreFile.getAbsolutePath()).openConnection();
			String contentType = conn.getContentType();
			java.io.FileInputStream fis = new java.io.FileInputStream(storedFile);
			long size = fis.getChannel().size();
			fis.close();
			//save file as UploadedSuspectListFile
			String message = beanSettingsContainer.addUploadedSuspectListScoreFile(
				new UploadedSuspectListFile(storedFile.getName(), size + " bytes", 
						contentType, "", storedFile.getAbsolutePath(), beanSettingsContainer.getSuspectListScoreFileIdentifier()));
			if(message.length() != 0) 	
				errorMessages.setMessage("suspectListsScoreError", message);
		} catch (Exception e) {
			errorMessages.setMessage("suspectListsScoreError", "Error reading file.");
			return false;
		}
		if (beanSettingsContainer.getSuspectListScoreFileContainer() == null || beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) {
			beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			return false;
		}
		else {
			beanSettingsContainer.setScoreValid(true, "suspectListsScore");
		}
		beanSettingsContainer.incrementSuspectListScoreFileIdentifier();
		return true;
	}
	
	/**
	 * 
	 * @param suspectListScoreFile
	 * @param infoMessages
	 * @param errorMessages
	 * @param fileName
	 * @return
	 */
	public boolean handleSuspectListScoreFile(java.io.InputStream suspectListScoreFile, 
			Messages infoMessages, Messages errorMessages, String fileName, BeanSettingsContainer beanSettingsContainer) {
		if(suspectListScoreFile == null) {
			errorMessages.setMessage("suspectListsScoreError", "Error: Suspect list file was not saved.");
			beanSettingsContainer.setCandidateFilePath("");
			return false;
		}
		//create upload folder
		File uploadFolder = this.fileStorer.prepareFolder(beanSettingsContainer.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsscore", errorMessages);
		if(uploadFolder == null) return false;
		//store the file
		File storedFile = null;
		try {
			storedFile = this.fileStorer.saveUploadedFile(suspectListScoreFile, uploadFolder, fileName);
		} catch(Exception e) {
			errorMessages.setMessage("candidateFileError", "Error: Suspect list file was not saved.");
			return false;
		}
		if(storedFile == null) return false;
		try {
			//get file properties
			java.net.URLConnection conn = new java.net.URL(uploadFolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileName).openConnection();
			String contentType = conn.getContentType();
			java.io.FileInputStream fis = new java.io.FileInputStream(storedFile);
			long size = fis.getChannel().size();
			fis.close();
			//save file as UploadedSuspectListFile
			String message = beanSettingsContainer.addUploadedSuspectListScoreFile(
				new UploadedSuspectListFile(storedFile.getName(), size + " bytes", 
						contentType, "", storedFile.getAbsolutePath(), beanSettingsContainer.getSuspectListScoreFileIdentifier()));
			if(message.length() != 0) 	
				errorMessages.setMessage("suspectListsScoreError", message);
		} catch (Exception e) {
			errorMessages.setMessage("suspectListsScoreError", "Error reading file.");
			return false;
		}
		if (beanSettingsContainer.getSuspectListScoreFileContainer() == null || beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) {
			beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			return false;
		}
		else {
			beanSettingsContainer.setScoreValid(true, "suspectListsScore");
		}
		beanSettingsContainer.incrementSuspectListScoreFileIdentifier();
		return true;
	}
}
