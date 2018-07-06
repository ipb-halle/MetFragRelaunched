package de.ipbhalle.metfragweb.controller;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Random;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.primefaces.model.chart.LineChartModel;

import de.ipbhalle.metfraglib.additionals.BondEnergies;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragmenterassignerscorer.AbstractFragmenterAssignerScorer;
import de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator;
import de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

@ManagedBean
@SessionScoped
public class CompareSpectraD3JSBean {

	private String peakList1;
	private String peakList2;

	private String smiles1 = "C(C(=O)O)OC1=NC(=C(C(=C1Cl)N)Cl)F";
	private String smiles2 = "";
	
	private String randomString1 = "";
	private String randomString2 = "";

	private String pathToPreImage1 = null;
	private String pathToPreImage2 = null;

	private String relativeMassDeviation = "5.0";
	private String absoluteMassDeviation = "0.001";
	private Random rand = new Random();

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
		this.randomString1 = this.getRandomString(6);
		this.randomString2 = this.getRandomString(6);
		this.peakList1 = "90.97445 681\n" + "106.94476 274\n" + "110.02750 110\n" + "115.98965 95\n";
		this.peakList2 = "115.98965 95\n" + "117.98540 384\n" + "124.93547 613\n" + "124.99015 146\n";
		try {
			System.out.println("write peaklists");
			String rootfolder = this.getRootSessionFolder();
			this.writeSpectraToFile(this.peakList1, this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR
					+ "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getRelativeMassDeviation() {
		return relativeMassDeviation;
	}

	public void setRelativeMassDeviation(String relativeMassDeviation) {
		this.relativeMassDeviation = relativeMassDeviation;
	}

	public String getAbsoluteMassDeviation() {
		return absoluteMassDeviation;
	}

	public void setAbsoluteMassDeviation(String absoluteMassDeviation) {
		this.absoluteMassDeviation = absoluteMassDeviation;
	}

	public String getPathPeakLists() {
		String url = this.getURL();
		System.out.println(url + "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR
				+ "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv");
		return url + "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra"
				+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv";
	}
	
	public String getPathToPreImage1() {
		return this.pathToPreImage1 + "?dummy=" + this.randomString1;
	}

	public void setPathToPreImage1(String pathToPreImage1) {
		this.pathToPreImage1 = pathToPreImage1;
	}

	public String getPathToPreImage2() {
		return pathToPreImage2 + "?dummy=" + this.randomString2;
	}

	public void setPathToPreImage2(String pathToPreImage2) {
		this.pathToPreImage2 = pathToPreImage2;
	}

	protected String getURL() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		// String url = req.getRequestURL().toString();
		String url = req.getRequestURI();
		String[] tmp = url.split("/");
		url = "";
		if (tmp.length > 0)
			url = tmp[0];
		for (int i = 1; i < tmp.length - 1; i++)
			url += "/" + tmp[i];
		return url;
	}

	public String getPathPeakList1() {
		return "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra"
				+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist1.tsv";
	}

	public String getPathPeakList2() {
		return "/" + this.getRootSessionURL() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra"
				+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklist2.tsv";
	}

	protected String generateImageOfMolecule(String smiles, String subpath) throws IOException {
		IAtomContainer m = null;
		try {
			m = MoleculeFunctions.getAtomContainerFromSMILES(smiles);
			MoleculeFunctions.prepareAtomContainer(m, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StandardSingleStructureImageGenerator s = new StandardSingleStructureImageGenerator();
		s.setImageHeight(300);
		s.setImageWidth(300);
		s.setStrokeRation(2.2);
		RenderedImage img = s.generateImage(m, "1");
		String filepath = this.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra"
			+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "images" + Constants.OS_SPECIFIC_FILE_SEPARATOR
			+ "fragments" + Constants.OS_SPECIFIC_FILE_SEPARATOR + subpath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "precursor.png";
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File(filepath));
		String sessionId = this.getSessionId();
		//return "https://i.forbesimg.com/media/lists/teams/carolina-panthers_416x416.jpg";
		return this.getURL() + "/files/" + sessionId + "/comparespectra/images/fragments/" + subpath + "/precursor.png";
	}

	protected java.util.Vector<PeakFragmentImage> generateFragmentsForPeakList(String peaklist, String smiles,
			String subpath, String randomString) throws Exception {
		String inchi = MoleculeFunctions.getInChIFromSmiles(smiles);
		ICandidate precursorCandidate = new TopDownPrecursorCandidate(inchi, "1");
		precursorCandidate.initialisePrecursorCandidate();
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, inchi.split("/")[1]);
		precursorCandidate.setProperty(VariableNames.SMILES_NAME, smiles);
		precursorCandidate.initialisePrecursorCandidate();
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		double monoisotopicmass = precursorCandidate.getMolecularFormula().getMonoisotopicMass();
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, monoisotopicmass);
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaklist);
		settings.set(VariableNames.PEAK_LIST_NAME, new FilteredStringTandemMassPeakListReader(settings).read());
		precursorCandidate.setUseSmiles(true);
		settings.set(VariableNames.CANDIDATE_NAME, precursorCandidate);
		settings.set(VariableNames.BOND_ENERGY_OBJECT_NAME, new BondEnergies());
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, Double.parseDouble(this.relativeMassDeviation));
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, Double.parseDouble(this.absoluteMassDeviation));
		AbstractFragmenterAssignerScorer fas = (AbstractFragmenterAssignerScorer) Class
				.forName((String) settings.get(VariableNames.METFRAG_ASSIGNER_SCORER_NAME))
				.getConstructor(Settings.class, ICandidate.class).newInstance(settings, precursorCandidate);
		fas.setCandidate(precursorCandidate);
		fas.initialise();
		fas.calculate();
		fas.assignInterimScoresResults();
		ICandidate scoredCandidate = fas.getCandidates()[0];
		// generate fragments
		java.io.File imageFolderFragments = new java.io.File(
				this.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra"
						+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "images" + Constants.OS_SPECIFIC_FILE_SEPARATOR
						+ "fragments" + Constants.OS_SPECIFIC_FILE_SEPARATOR + subpath);
		if (imageFolderFragments.exists())
			FileUtils.deleteDirectory(imageFolderFragments);
		imageFolderFragments.mkdirs();
		String sessionId = this.getSessionId();
		java.util.Vector<PeakFragmentImage> imagePathVector = new java.util.Vector<PeakFragmentImage>();
		for (int i = 0; i < scoredCandidate.getMatchList().getNumberElements(); i++) {
			HighlightSubStructureImageGenerator imageGenerator = new HighlightSubStructureImageGenerator();
			int size = 300;
			if (monoisotopicmass > 500)
				size = 400;
			if (monoisotopicmass > 700)
				size = 500;
			imageGenerator.setImageHeight(size);
			imageGenerator.setImageWidth(size);
			RenderedImage image;
			java.io.File imageFile = new java.io.File(imageFolderFragments.getAbsolutePath()
					+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "fragment_" + i + ".png");
			try {
				image = imageGenerator.generateImage(scoredCandidate.getPrecursorMolecule(),
						scoredCandidate.getMatchList().getElement(i).getBestMatchedFragment());
				ImageIO.write(image, "png", imageFile);
			} catch (Exception e) {
				System.err.println("error generating fragment image");
			}

			imagePathVector
					.add(new PeakFragmentImage(scoredCandidate.getMatchList().getElement(i).getMatchedPeak().getMass(),
							this.getURL() + "/files/" + sessionId + "/comparespectra/images/fragments/" + subpath
									+ "/fragment_" + i + ".png?dummy=" + randomString,
							MathTools.round(((FragmentMassToPeakMatch) scoredCandidate.getMatchList().getElement(i))
									.getBestMatchFragmentMass())));
		}
		return imagePathVector;
	}

	protected void writeSpectraToFile(String peaklist1, String peaklist2, String filename) throws IOException {
		java.util.Vector<PeakFragmentImage> imagePathVector1 = null;
		java.util.Vector<PeakFragmentImage> imagePathVector2 = null;
		try {
			if (this.smiles1 != null && this.smiles1.length() != 0) {
				System.out.println("generating images peaklist 1 -> " + this.smiles1);
				imagePathVector1 = this.generateFragmentsForPeakList(peaklist1, this.smiles1, "peaklist1", this.randomString1);
				this.pathToPreImage1 = this.generateImageOfMolecule(this.smiles1, "peaklist1");
			} else this.pathToPreImage1 = "";
			if (this.smiles2 != null && this.smiles2.length() != 0) {
				System.out.println("generating images peaklist 2 -> " + this.smiles2);
				imagePathVector2 = this.generateFragmentsForPeakList(peaklist2, this.smiles2, "peaklist2", this.randomString2);
				this.pathToPreImage2 = this.generateImageOfMolecule(this.smiles2, "peaklist2");
			} else this.pathToPreImage2 = "";
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AtomTypeNotKnownFromInputListException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(filename);
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
		String[] tmp1 = peaklist1.split("\\n");
		String[] tmp2 = peaklist2.split("\\n");
		bwriter.write("x\ty\tname\tcolor\ttype\timagepath\tfragmass");
		bwriter.newLine();
		double maxint = 0.0;
		for (int i = 0; i < tmp1.length; i++) {
			String[] peak = tmp1[i].split("\\s+");
			double curint = Double.parseDouble(peak[1]);
			if (maxint < curint)
				maxint = curint;
		}
		for (int i = 0; i < tmp1.length; i++) {
			String[] peak = tmp1[i].split("\\s+");
			Double peakMass = Double.parseDouble(peak[0]);
			String url = "null";
			String fragmentMass = "null";
			int idx = this.contains(peakMass, imagePathVector1);
			if (idx != -1) {
				url = imagePathVector1.get(idx).getUrl();
				fragmentMass = String.valueOf(imagePathVector1.get(idx).getFragmentMass());
			}
			String output = peak[0] + "\t" + (MathTools.round(Double.parseDouble(peak[1]) / maxint)) + "\tp" + (i + 1);
			if (imagePathVector1 == null || !url.equals("null"))
				output += "\tblue\t1\t" + url + "\t" + fragmentMass;
			else
				output += "\tgray\t1\t" + url + "\t" + fragmentMass;
			bwriter.write(output);
			bwriter.newLine();
		}
		maxint = 0.0;
		for (int i = 0; i < tmp2.length; i++) {
			String[] peak = tmp2[i].split("\\s+");
			double curint = Double.parseDouble(peak[1]);
			if (maxint < curint)
				maxint = curint;
		}
		for (int i = 0; i < tmp2.length; i++) {
			String[] peak = tmp2[i].split("\\s+");
			Double peakMass = Double.parseDouble(peak[0]);
			String url = "null";
			String fragmentMass = "null";
			int idx = this.contains(peakMass, imagePathVector2);
			if (idx != -1) {
				url = imagePathVector2.get(idx).getUrl();
				fragmentMass = String.valueOf(imagePathVector2.get(idx).getFragmentMass());
			}
			String output = peak[0] + "\t" + (MathTools.round(Double.parseDouble(peak[1]) / maxint)) + "\tp" + (i + 1);
			if (imagePathVector2 == null || !url.equals("null"))
				output += "\tgreen\t2\t" + url + "\t" + fragmentMass;
			else
				output += "\tgray\t2\t" + url + "\t" + fragmentMass;
			bwriter.write(output);
			bwriter.newLine();
		}
		bwriter.close();
	}

	protected int contains(Double mass, java.util.Vector<PeakFragmentImage> imagePathVector) {
		if (imagePathVector == null)
			return -1;
		for (int i = 0; i < imagePathVector.size(); i++) {
			if (imagePathVector.get(i).getPeakMass().equals(mass))
				return i;
		}
		return -1;
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

	public String getSmiles1() {
		return smiles1;
	}

	public void setSmiles1(String smiles1) {
		if(!smiles1.equals(this.smiles1)) this.randomString1 = this.getRandomString(6);
		this.smiles1 = smiles1;
	}

	public String getSmiles2() {
		return smiles2;
	}

	public void setSmiles2(String smiles2) {
		if(!smiles2.equals(this.smiles2)) this.randomString2 = this.getRandomString(6);
		this.smiles2 = smiles2;
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
			this.writeSpectraToFile(this.peakList1, this.peakList2, rootfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR
					+ "comparespectra" + Constants.OS_SPECIFIC_FILE_SEPARATOR + "peaklists.tsv");
			// RequestContext.getCurrentInstance().update("spectrumViewForm");
			// RequestContext.getCurrentInstance().update("spectrumView");
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
		String peakList = "115.98965 95\n" + "117.98540 384\n" + "124.93547 613\n" + "124.99015 146\n";
		String[] tmp = peakList.split("\\n");
		System.out.println(tmp.length);
		for (int i = 0; i < tmp.length; i++) {
			System.out.println(tmp[i]);
		}
	}

	private void generateFolders() {
		String root = this.getRootSessionFolder();
		java.io.File rootFolder = new java.io.File(root);
		java.io.File[] files = rootFolder.listFiles();
		try {
			// first delete all files in the root folder
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory())
						FileUtils.deleteDirectory(files[i]);
					else
						files[i].delete();
				}
			}
			// generate new folder structure
			System.out.println("generate session folders");
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "comparespectra").mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("error generating session folders");
			return;
		}
	}

	public String getSessionId() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		return session.getId();
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
		String rootPath = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
				.getRealPath("/");
		return rootPath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "files" + Constants.OS_SPECIFIC_FILE_SEPARATOR
				+ sessionId;
	}

	public String getRandomString(int len) {
		String nums = "0123456789";
		String string = "";
		for(int i = 0; i < len; i++) {
			string += nums.charAt(this.rand.nextInt(nums.length()));
		}
		return string;
	}
	
	public class PeakFragmentImage {
		private String url;
		private Double peakMass;
		private Double fragmentMass;

		public PeakFragmentImage(Double peakMass, String url, Double fragmentMass) {
			this.url = url;
			this.peakMass = peakMass;
			this.fragmentMass = fragmentMass;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Double getPeakMass() {
			return peakMass;
		}

		public void setPeakMass(Double peakMass) {
			this.peakMass = peakMass;
		}

		public Double getFragmentMass() {
			return fragmentMass;
		}

		public void setFragmentMass(Double fragmentMass) {
			this.fragmentMass = fragmentMass;
		}

	}

}
