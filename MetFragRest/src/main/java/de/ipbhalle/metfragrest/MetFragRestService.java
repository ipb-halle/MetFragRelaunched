package de.ipbhalle.metfragrest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.ClassNames;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

@Service
public class MetFragRestService {

	@Async
	public static String startMetFrag(CombinedMetFragProcess mp, MetFragGlobalSettings settings, String resultFolderName) throws IOException {
		// retrieve candidates from database
		boolean candidatesRetrieved = false;
		try {
			candidatesRetrieved = mp.retrieveCompounds();
			mp.run();
			CandidateList scoredCandidateList = mp.getCandidateList();
			if(settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME) != null) {
				String[] candidateWriterNames = (String[])settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME);
				for(int k = 0; k < candidateWriterNames.length; k++) {
					IWriter candidateWriter = (IWriter) Class.forName(ClassNames.getClassNameOfCandidateListWriter(candidateWriterNames[k])).getConstructor().newInstance();
					try {
						Object ResultsFile = settings.get(VariableNames.STORE_RESULTS_FILE_NAME);
						if(ResultsFile != null) candidateWriter.writeFile(new File((String)ResultsFile), scoredCandidateList, settings); 
						else candidateWriter.write(scoredCandidateList, (String)settings.get(VariableNames.SAMPLE_NAME), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME), settings);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeStatus("ERROR", (String)settings.get(VariableNames.SAMPLE_NAME), resultFolderName);
			return "Error when fetching candidates.";
		}
		if (!candidatesRetrieved) {
			writeStatus("ERROR", (String)settings.get(VariableNames.SAMPLE_NAME), resultFolderName);
			return "Error when fetching candidates.";
		}
		else {
			writeStatus("SUCCESS", (String)settings.get(VariableNames.SAMPLE_NAME), resultFolderName);
			return (String) settings.get(VariableNames.SAMPLE_NAME);
		}
	}

    private static void writeStatus(String status, String processid, String resultFolderName) throws IOException {
    	File statusfile = new File(resultFolderName + Constants.OS_SPECIFIC_FILE_SEPARATOR + "status.txt");
    	if((!statusfile.exists() && new File(resultFolderName).canWrite()) || (statusfile.exists() && statusfile.canWrite())) {
    		BufferedWriter bwriter = new BufferedWriter(new FileWriter(statusfile));
    		bwriter.write(status);
    		bwriter.newLine();
    		bwriter.close();
    	}
    }
	
}
