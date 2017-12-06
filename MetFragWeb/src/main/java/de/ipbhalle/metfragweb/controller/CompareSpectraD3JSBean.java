package de.ipbhalle.metfragweb.controller;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.primefaces.model.chart.LineChartModel;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

@ManagedBean
@SessionScoped
public class CompareSpectraD3JSBean {
	
	private String peakList1;
	private String peakList2;
	
	protected LineChartModel spectrumModel;
	
	/*
	 * bean initialisation
	 */
	public CompareSpectraD3JSBean() {
		System.out.println("CompareSpectraBean");
		this.init();
	}
	
	public void init() {
		this.generateFolders();
		this.peakList1 = "90.97445 681\n" +
				"106.94476 274\n" +
				"110.02750 110\n" +
				"115.98965 95\n";
		this.peakList2 = "115.98965 95\n" +
				"117.98540 384\n" +
				"124.93547 613\n" +
				"124.99015 146\n";
		try {
			System.out.println("write peaklists");
			String rootfolder = this.getRootSessionFolder();
		//	this.writeSpectrumToFile(this.peakList1, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist1.tsv");
		//	this.writeSpectrumToFile(this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist2.tsv");
			this.writeSpectraToFile(this.peakList1, this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public String getPathPeakLists() {
		return "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv";
	}
	
	public String getPathPeakList1() {
		return "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist1.tsv";
	}
	
	public String getPathPeakList2() {
		return "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist2.tsv";
	}
	
	protected void writeSpectrumToFile(String peaklist, String filename) throws IOException {
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
		String[] tmp = peaklist.split("\\n");
		bwriter.write("x\ty\tname\tcolor");
		bwriter.newLine();
		double maxint = 0.0;
		for(int i = 0; i < tmp.length; i++) {
			String[] peak = tmp[i].split("\\s+");
			double curint = Double.parseDouble(peak[1]);
			if(maxint < curint) maxint = curint;
		}
		for(int i = 0; i < tmp.length; i++) {
			String[] peak = tmp[i].split("\\s+");
			bwriter.write(peak[0] + "\t" + (MathTools.round(Double.parseDouble(peak[1]) / maxint)) + "\tp" + (i+1) + "\tblue");
			bwriter.newLine();
		}
		bwriter.close();
	}

	protected void writeSpectraToFile(String peaklist1, String peaklist2, String filename) throws IOException {
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
		String[] tmp1 = peaklist1.split("\\n");
		String[] tmp2 = peaklist2.split("\\n");
		bwriter.write("x\ty\tname\tcolor\ttype");
		bwriter.newLine();
		double maxint = 0.0;
		for(int i = 0; i < tmp1.length; i++) {
			String[] peak = tmp1[i].split("\\s+");
			double curint = Double.parseDouble(peak[1]);
			if(maxint < curint) maxint = curint;
		}
		for(int i = 0; i < tmp1.length; i++) {
			String[] peak = tmp1[i].split("\\s+");
			bwriter.write(peak[0] + "\t" + (MathTools.round(Double.parseDouble(peak[1]) / maxint)) + "\tp" + (i+1) + "\tblue\t1");
			bwriter.newLine();
		}
		maxint = 0.0;
		for(int i = 0; i < tmp2.length; i++) {
			String[] peak = tmp2[i].split("\\s+");
			double curint = Double.parseDouble(peak[1]);
			if(maxint < curint) maxint = curint;
		}
		for(int i = 0; i < tmp2.length; i++) {
			String[] peak = tmp2[i].split("\\s+");
			bwriter.write(peak[0] + "\t" + (MathTools.round(Double.parseDouble(peak[1]) / maxint)) + "\tp" + (i+1) + "\tgreen\t2");
			bwriter.newLine();
		}
		bwriter.close();
	}
	
	public void setPeakList1(String peakList1) {
		this.peakList1 = peakList1.replaceAll("^\\s+", "").replaceAll("\\n\\s+", "\n");
	}

	public void setPeakList2(String peakList2) {
		this.peakList2 = peakList2.replaceAll("^\\s+", "").replaceAll("\\n\\s+", "\n");
	}

	public String getPeakList1() {
		return peakList1;
	}

	public String getPeakList2() {
		return peakList2;
	}
	
	public void setSpectrumModel(LineChartModel spectrumModel) {
		this.spectrumModel = spectrumModel;
	}

	public DefaultPeakList generatePeakListObject(String peaklist) throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaklist);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 0.0);
		DefaultPeakList peaklistObject = new StringTandemMassPeakListReader(settings).read();
		return peaklistObject;
	}
	
	public void generateSpectrumModelViewListener(ActionEvent action) {
		System.out.println("generateSpectrumModelViewListener");
		try {
			System.out.println("write peaklists");
			String rootfolder = this.getRootSessionFolder();
		//	this.writeSpectrumToFile(this.peakList1, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist1.tsv");
		//	this.writeSpectrumToFile(this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist2.tsv");
			this.writeSpectraToFile(this.peakList1, this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LineChartModel getSpectrumModel() {
		return this.spectrumModel;
	}

	public boolean isSpectrumViewAvailable() {
		return this.spectrumModel != null;
	}

	public static void main(String[] args) {
		String peakList = "115.98965 95\n" +
				"117.98540 384\n" +
				"124.93547 613\n" +
				"124.99015 146\n";
		String[] tmp = peakList.split("\\n");
		System.out.println(tmp.length);
		for(int i = 0; i < tmp.length; i++) {
			System.out.println(tmp[i]);
		}
	}
	
	private void generateFolders() {
		String root = this.getRootSessionFolder();
		java.io.File rootFolder = new java.io.File(root);
		java.io.File[] files = rootFolder.listFiles();
		try {
			//first delete all files in the root folder
			if(files != null) {
				for(int i = 0; i < files.length; i++) {
					if(files[i].isDirectory()) FileUtils.deleteDirectory(files[i]);
					else files[i].delete();
				}
			}
			//generate new folder structure
			System.out.println("generate session folders");
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra").mkdirs();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("error generating session folders");
			return;
		}
	}

	public String getRootSessionURL() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String sessionId = session.getId();
		return "files" + Constants.OS_SPECIFIC_FILE_SEPARATOR + sessionId;
	}
	
	public String getRootSessionFolder() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String sessionId = session.getId();
		String rootPath = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
		return rootPath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "files" + Constants.OS_SPECIFIC_FILE_SEPARATOR + sessionId;
	}
}


