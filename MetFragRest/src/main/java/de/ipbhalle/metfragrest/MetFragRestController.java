package de.ipbhalle.metfragrest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import com.google.common.io.Files;

import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

@RestController
@EnableAutoConfiguration
public class MetFragRestController {

	private Logger logger = Logger.getLogger(MetFragRestController.class);
	private static final String RESULTS_FOLDER = System.getProperty("java.io.tmpdir");
	private static final String STATUS_FILE_NAME = "status.txt";

	@RequestMapping("/process")
	@ResponseBody
	public String process(@RequestBody ProcessArguments args)
			throws InterruptedException, ExecutionException, IOException {
		File resFolder = Files.createTempDir();
		String returnMsg = "";
		try {
			MetFragGlobalSettings settings = args.getSettingsObject(resFolder);
			// check settings
			SettingsChecker settingsChecker = new SettingsChecker();
			if (!settingsChecker.check(settings)) {
				return "Error: Check given parameters";
			}
			logger.info("Storing in " + settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
			CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
			this.writeStatus("RUNNING", resFolder.getName());
			new Thread(() -> {
				System.out.println("staring run");
				try {
					MetFragRestService.startMetFrag(mp, settings, resFolder.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}, "MyThread-" + (String) settings.get(VariableNames.SAMPLE_NAME)).start();
			returnMsg = (String) settings.get(VariableNames.SAMPLE_NAME);
			// return (String)settings.get(VariableNames.SAMPLE_NAME);
		} catch (ParameterNotKnownException e) {
			e.printStackTrace();
			this.writeStatus("ERROR", resFolder.getName());
			return "Error: Parameter not known";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error: Could not write status";
		} catch (Exception e) {
			e.printStackTrace();
			this.writeStatus("ERROR", resFolder.getName());
			return "Error: Unknown error";
		}
		return returnMsg;
	}

	private void writeStatus(String status, String processid) throws IOException {
		File statusfile = new File(
				this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + STATUS_FILE_NAME);
		if ((!statusfile.exists() && new File(this.getResultFolderName(processid)).canWrite())
				|| (statusfile.exists() && statusfile.canWrite())) {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(statusfile));
			bwriter.write(status);
			bwriter.newLine();
			bwriter.close();
		}
	}

	private String readStatus(String processid) {
    	File statusfile = new File(this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + STATUS_FILE_NAME);
    	if(statusfile.canRead()) {
    		String status = "";
    		try {
				BufferedReader breader = new BufferedReader(new FileReader(statusfile));
				status = breader.readLine();
				breader.close();
	    		return status;
			} catch (IOException e) {
				e.printStackTrace();
				return "ERROR - could not read status";
			}
    	}
    	return "ERROR - could not read status";
    }

	private String getResultFileName(String processid) {
		return this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + processid + ".csv";
	}

	private String getResultFolderName(String processid) {
		return RESULTS_FOLDER + Constants.OS_SPECIFIC_FILE_SEPARATOR + processid;
	}

	@RequestMapping("/status")
	@ResponseBody
	public String status(@RequestBody StatusArguments args)
			throws InterruptedException, ExecutionException {
		if (args == null || args.getProcessid() == null)
			return "Error: No processid given.";
		return this.readStatus(args.getProcessid());
	}

	@RequestMapping("/fetch")
	@ResponseBody
	public String fetch(@RequestBody FetchArguments args) throws InterruptedException, ExecutionException, IOException {
		if (args == null || args.getProcessid() == null)
			return "Error: No processid given.";
		File resultFile = new File(this.getResultFileName(args.getProcessid()));
		if (!resultFile.exists() || !resultFile.isFile() || !resultFile.canRead())
			return "Error: No result found with id " + args.getProcessid();
		StringBuilder builder = new StringBuilder();
		BufferedReader breader = new BufferedReader(new FileReader(resultFile));
		String line = "";
		while ((line = breader.readLine()) != null) {
			builder.append(line + "\r\n");
		}
		breader.close();
		return builder.toString();
	}

	@RequestMapping(value = "/fetchzip", method = RequestMethod.GET)
	@ResponseBody
	public void fetchzip(@RequestBody FetchArguments args, HttpServletResponse response) throws InterruptedException, ExecutionException, IOException {
		if (args == null || args.getProcessid() == null)
			return;;
		File resultFile = new File(this.getResultFileName(args.getProcessid()));
		if (!resultFile.exists() || !resultFile.isFile() || !resultFile.canRead())
			return;
		FileSystemResource resource = new FileSystemResource(resultFile.getAbsolutePath()); 
	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=file.zip");

	    try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
	        ZipEntry e = new ZipEntry(resource.getFilename());
	        // Configure the zip entry, the properties of the file
	        e.setSize(resource.contentLength());
	        e.setTime(System.currentTimeMillis());
	        // etc.
	        zippedOut.putNextEntry(e);
	        // And the content of the resource:
	        StreamUtils.copy(resource.getInputStream(), zippedOut);
	        zippedOut.closeEntry();
	        zippedOut.finish();
	    } catch (Exception e) {
	        // Do something with Exception
	    }        
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MetFragRestController.class, args);
	}
}