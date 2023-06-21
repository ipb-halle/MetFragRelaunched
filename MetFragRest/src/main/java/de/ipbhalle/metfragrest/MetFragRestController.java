package de.ipbhalle.metfragrest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.hateoas.Resource;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import de.ipbhalle.exception.CouldNotCreateProcessException;
import de.ipbhalle.exception.CouldNotFetchResultsException;
import de.ipbhalle.exception.CouldNotReadHostException;
import de.ipbhalle.exception.CouldNotReadStatusException;
import de.ipbhalle.exception.CouldNotRemoveProcessException;
import de.ipbhalle.exception.CouldNotWriteStatusException;
import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.model.HostAssembler;
import de.ipbhalle.model.ProcessArguments;
import de.ipbhalle.model.ProcessAssembler;
import de.ipbhalle.model.StatusAssembler;

@RestController
@EnableAutoConfiguration
/**@RequestMapping("/metfrag/api/v1")**/
/**@RequestMapping("/MetFrag-deNBI/api/v1")**/
@RequestMapping("/${metfragrest-controller.path}")

public class MetFragRestController {

	private Logger logger = Logger.getLogger(MetFragRestController.class);
//	https://github.com/ipb-halle/MetFragRelaunched/issues/115
//	private static final String RESULTS_FOLDER = System.getProperty("java_io_tmpdir");
	private static final String RESULTS_FOLDER = "/tmp";
	private static final String STATUS_FILE_NAME = "status.txt";
	private static final String HOST_FILE_NAME = "host.txt";

	/**
	 * runs a metfrag query
	 *
	 * @param args
	 * @return
	 * @throws CouldNotWriteStatusException
	 * @throws CouldNotCreateProcessException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<Resource<ProcessAssembler>> process(@RequestBody ProcessArguments args) throws CouldNotWriteStatusException, CouldNotCreateProcessException {
		File resFolder;
		String processid;
		try {
			resFolder = Files.createTempDirectory("java_io_tmpdir").toFile();
			processid = resFolder.getName();
			try {
				MetFragGlobalSettings settings = args.getSettingsObject(resFolder);
				// check settings
				SettingsChecker settingsChecker = new SettingsChecker();
				if (!settingsChecker.check(settings)) throw new CouldNotCreateProcessException( "Error: Corrupt parameters" );
				logger.info("Storing in " + settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
				CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
				this.writeStatus("RUNNING", processid);
				this.writeHost(processid);
				new Thread(() -> {
					System.out.println("staring run");
					try {
						MetFragRestService.startMetFrag(mp, settings, resFolder.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}, "MyThread-" + processid).start();
			} catch (ParameterNotKnownException e) {
				e.printStackTrace();
				this.writeStatus("ERROR", processid);
				throw new CouldNotCreateProcessException( "Error: Parameter not known" );
			} catch (IOException e) {
				e.printStackTrace();
				throw new CouldNotCreateProcessException( "Error: Could not write status" );
			} catch (Exception e) {
				e.printStackTrace();
				this.writeStatus("ERROR", processid);
				throw new CouldNotCreateProcessException( "Error: Unknown error" );
			}
		} catch(IOException e) {
			throw new CouldNotWriteStatusException( e.getMessage() );
		}
		Resource<ProcessAssembler> resource = new ProcessAssembler("process", processid).toResource();
		resource.add(linkTo(MetFragRestController.class).slash("process").withSelfRel());
		resource.add(linkTo(MetFragRestController.class).slash("status").slash(processid).withRel("status"));
		resource.add(linkTo(MetFragRestController.class).slash("host").slash(processid).withRel("host"));
		resource.add(linkTo(MetFragRestController.class).slash("result").slash(processid).withRel("result"));
		resource.add(linkTo(MetFragRestController.class).slash("resultzip").slash(processid).withRel("resultzip"));
		return new ResponseEntity<Resource<ProcessAssembler>>(resource, HttpStatus.CREATED);
	}

	/**
	 * get the status of a running/finished query
	 *
	 * @param processid
	 * @return
	 * @throws CouldNotReadStatusException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/status/{processid}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Resource<StatusAssembler>> status(@PathVariable String processid) throws CouldNotReadStatusException {
		Resource<StatusAssembler> resource = this.readStatus(processid).toResource();
		resource.add(linkTo(MetFragRestController.class).slash("status").slash(processid).withSelfRel());
		return new ResponseEntity<Resource<StatusAssembler>>(resource, HttpStatus.OK);
	}

	/**
	 * get host on which the query was run
	 *
	 * @param processid
	 * @return
	 * @throws CouldNotReadHostException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/host/{processid}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Resource<HostAssembler>> host(@PathVariable String processid) throws CouldNotReadHostException {
		Resource<HostAssembler> resource = this.readHost(processid).toResource();
		resource.add(linkTo(MetFragRestController.class).slash("host").slash(processid).withSelfRel());
		return new ResponseEntity<Resource<HostAssembler>>(resource, HttpStatus.OK);
	}

	/**
	 * get the result back (as CSV)
	 *
	 * @param processid
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/result/{processid}", produces = { MediaType.TEXT_PLAIN_VALUE })
	public String fetch(@PathVariable String processid) throws InterruptedException, ExecutionException, IOException {
		File resultFile = new File(this.getResultFileName(processid));
		if (!resultFile.exists() || !resultFile.isFile() || !resultFile.canRead())
			new CouldNotFetchResultsException(processid);
		StringBuilder builder = new StringBuilder();
		BufferedReader breader = new BufferedReader(new FileReader(resultFile));
		String line = "";
		while ((line = breader.readLine()) != null) {
			builder.append(line + "\r\n");
		}
		breader.close();
		return builder.toString();
	}

	/**
	 * get the result back (as ZIP)
	 *
	 * @param processId
	 * @param response
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/resultzip/{processid}", produces = { MediaType.TEXT_PLAIN_VALUE })
	public void fetchzip(@PathVariable String processId, HttpServletResponse response) throws InterruptedException, ExecutionException, IOException {
		File resultFile = new File(this.getResultFileName(processId));
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

	/**
	 * delete the query after it was finished
	 *
	 * @param processid
	 * @return
	 * @throws CouldNotRemoveProcessException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/{processid}")
	ResponseEntity<Void> delete(@PathVariable String processid) throws CouldNotRemoveProcessException {
		try {
			if(this.readStatus(processid).getStatus().equals("RUNNING")) throw new CouldNotRemoveProcessException("Process with id " + processid + " is still running");
		} catch (CouldNotReadStatusException e) {
			throw new CouldNotRemoveProcessException("Process with id " + processid + " may not exist");
		}
		this.removeProcess(processid);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	/*
	 * some helper methods
	 */

	/**
	 *
	 * @param processid
	 * @throws CouldNotRemoveProcessException
	 */
	private void removeProcess(String processid) throws CouldNotRemoveProcessException {
		File file = new File(this.getResultFolderName(processid));
		if(!file.exists()) throw new CouldNotRemoveProcessException("Process with id " + processid + " not found");
		if(!file.canWrite()) throw new CouldNotRemoveProcessException("Process with id " + processid + " cannot be deleted");
		try {
			FileUtils.deleteDirectory(file);
		} catch (Exception e) {
			throw new CouldNotRemoveProcessException(e.getMessage());
		}
		System.out.println("Deleted " + file.getAbsolutePath());
	}

	/**
	 *
	 * @return
	 */
	public String getServerName() {
        String hostname = "";
        try {
        	hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        	e.printStackTrace();
        	return "";
        }
        return hostname;
	}

	/**
	 *
	 * @param status
	 * @param processid
	 * @throws IOException
	 */
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

	/**
	 *
	 * @param processid
	 * @throws IOException
	 */
	private void writeHost(String processid) throws IOException {
		File hostfile = new File(
				this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + HOST_FILE_NAME);
		if ((!hostfile.exists() && new File(this.getResultFolderName(processid)).canWrite())
				|| (hostfile.exists() && hostfile.canWrite())) {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(hostfile));
			bwriter.write(this.getServerName());
			bwriter.newLine();
			bwriter.close();
		}
	}

	/**
	 *
	 * @param processid
	 * @return
	 * @throws CouldNotReadStatusException
	 */
	private StatusAssembler readStatus(String processid) throws CouldNotReadStatusException {
    	File statusfile = new File(this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + STATUS_FILE_NAME);
    	if(statusfile.canRead()) {
    		String status = "";
    		try {
				BufferedReader breader = new BufferedReader(new FileReader(statusfile));
				status = breader.readLine();
				breader.close();
	    		return new StatusAssembler("status", status);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CouldNotReadStatusException(processid);
			}
    	}
    	throw new CouldNotReadStatusException(processid);
    }

	/**
	 *
	 * @param processid
	 * @return
	 * @throws CouldNotReadHostException
	 */
	protected HostAssembler readHost(String processid) throws CouldNotReadHostException {
    	File hostfile = new File(this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + HOST_FILE_NAME);
    	if(hostfile.canRead()) {
    		String host = "";
    		try {
				BufferedReader breader = new BufferedReader(new FileReader(hostfile));
				host = breader.readLine();
				breader.close();
	    		return new HostAssembler("host", host);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CouldNotReadHostException(processid);
			}
    	}
		throw new CouldNotReadHostException(processid);
    }

	/**
	 *
	 * @param processid
	 * @return
	 */
	private String getResultFileName(String processid) {
		return this.getResultFolderName(processid) + Constants.OS_SPECIFIC_FILE_SEPARATOR + processid + ".csv";
	}

	/**
	 *
	 * @param processid
	 * @return
	 */
	private String getResultFolderName(String processid) {
		return RESULTS_FOLDER + Constants.OS_SPECIFIC_FILE_SEPARATOR + processid;
	}

	/**
	 * start controller
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(MetFragRestController.class, args);
	}
}
