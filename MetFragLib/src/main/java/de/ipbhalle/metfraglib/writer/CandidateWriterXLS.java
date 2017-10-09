package de.ipbhalle.metfraglib.writer;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

/**
 * writes single information from a single candidate into a XLS file 
 * 
 * @author cruttkie
 *
 */
public class CandidateWriterXLS implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	@Override
	public boolean writeFile(File xlsFile, IList list, Settings settings) throws Exception {

		CandidateList candidateList = null;
		boolean isScoredCandidate = false;
		if (list instanceof ScoredCandidateList
				|| list instanceof SortedScoredCandidateList) {
			candidateList = (ScoredCandidateList) list;
			isScoredCandidate = true;
		}
		if (list instanceof CandidateList) {
			candidateList = (CandidateList) list;
		}
		if (candidateList == null || candidateList.getNumberElements() == 0)
			return false;
		
		ICandidate candidate = candidateList.getElement(0);
		if(settings != null) candidate.setUseSmiles((Boolean)settings.get(VariableNames.USE_SMILES_NAME));
		candidate.initialisePrecursorCandidate();
		xlsFile.createNewFile();
		WritableWorkbook workbook = Workbook.createWorkbook(xlsFile);
		WritableSheet sheet1 = workbook.createSheet("MetFrag Candidate Result", 0);

		WritableFont arial10fontBold = new WritableFont(WritableFont.ARIAL, 10);
		arial10fontBold.setBoldStyle(WritableFont.BOLD);
		WritableCellFormat arial10formatBold = new WritableCellFormat(arial10fontBold);

		WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
		arial10font.setBoldStyle(WritableFont.NO_BOLD);
		WritableCellFormat arial10format = new WritableCellFormat(arial10font);

		sheet1.addCell(new Label(0, 0, VariableNames.IDENTIFIER_NAME, arial10formatBold));
		sheet1.addCell(new Label(1, 0, candidate.getIdentifier(), arial10format));
		
		java.util.Hashtable<String, Object> properties = candidate.getProperties();
		
		java.util.Enumeration<?> keys = properties.keys();
		
		int propertyRow = 1;
		while(keys.hasMoreElements()) {
			String currentKey = (String)keys.nextElement();
			sheet1.addCell(new Label(0, propertyRow, currentKey, arial10formatBold));
			String value = "";
			Object obj = properties.get(currentKey);
			if(obj instanceof java.lang.Double) value = String.valueOf((Double)obj);
			else if(obj instanceof java.lang.Integer) value = String.valueOf((Integer)obj);
			else if(obj instanceof java.lang.String) value = (String)obj;
			sheet1.addCell(new Label(1, propertyRow, value, arial10format));
			propertyRow++;
		}
		
		java.util.List<RenderedImage> molFragmentImages = null;
		RenderedImage molImage = this.convertMoleculeToImages(candidate);
		File imageFile = File.createTempFile("file", ".png", new File(Constants.OS_TEMP_DIR));
		imageFile.deleteOnExit();
		
		int rowHeightImage = 10;
		int colWidthImage = 3;
		int fragmentColumns = 1;
		sheet1.addCell(new Label(0, propertyRow + 1, "Precursor", arial10formatBold));
		
		if (ImageIO.write(molImage, "png", imageFile)) {
			WritableImage wi = new WritableImage(0, propertyRow + 2, colWidthImage, rowHeightImage, imageFile);
			sheet1.addImage(wi);
		}
		if (isScoredCandidate) {
			molFragmentImages = this.convertMoleculeFragmentsToImages(candidate);

			if(molFragmentImages.size() > 0) {
				sheet1.addCell(new Label(0, (propertyRow + 4) + rowHeightImage, "Fragments", arial10formatBold));
			}
			int imagesWritten = 1;
			int fragmentRow = 0;
			for (int i = 0; i < molFragmentImages.size(); i++) {
				File imageFileFragment = File.createTempFile("file" + i, ".png", new File(Constants.OS_TEMP_DIR));
				imageFileFragment.deleteOnExit();
				if (ImageIO.write(molFragmentImages.get(i), "png", imageFileFragment)) {
					int column = (((imagesWritten - 1) % fragmentColumns)) * (colWidthImage + 3);
					if((imagesWritten - 1) % fragmentColumns == 0) fragmentRow++;
					
					WritableImage wi = new WritableImage(column, (propertyRow + 5) + (rowHeightImage * fragmentRow), colWidthImage, rowHeightImage, imageFileFragment);
					sheet1.addImage(wi);
					
					FragmentMassToPeakMatch match = (FragmentMassToPeakMatch)candidate.getMatchList().getElement(imagesWritten - 1);
					sheet1.addCell(new Label(column + colWidthImage, (propertyRow + 5) + (rowHeightImage * fragmentRow), "Fragment " + imagesWritten, arial10formatBold));
					
					sheet1.addCell(new Label(column + colWidthImage, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 2, "Formula", arial10formatBold));
					sheet1.addCell(new Label(column + colWidthImage + 1, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 2, match.getModifiedFormulaStringOfBestMatchedFragment(candidate.getPrecursorMolecule()), arial10format));
					
					sheet1.addCell(new Label(column + colWidthImage, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 3, "Mass", arial10formatBold));
					sheet1.addCell(new Label(column + colWidthImage + 1, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 3, String.valueOf(MathTools.round(match.getBestMatchFragmentMass(), 5)), arial10format));
					
					sheet1.addCell(new Label(column + colWidthImage, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 4, "Peak m/z", arial10formatBold));
					sheet1.addCell(new Label(column + colWidthImage + 1, (propertyRow + 5) + (rowHeightImage * fragmentRow) + 4, String.valueOf(match.getMatchedPeak().getMass()), arial10format));
					
					imagesWritten++;
				}
			}
			
		}
		workbook.write();
		workbook.close();
		return true;
	}

	/**
	 * 
	 * @param candidate
	 * @return
	 * @throws Exception
	 */
	private RenderedImage convertMoleculeToImages(ICandidate candidate) throws Exception {

		de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator imageGenerator = new de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator();
		imageGenerator.setBackgroundColor(new Color(1f, 1f, 1f, 0f));
		imageGenerator.setImageHeight(200);
		imageGenerator.setImageWidth(200);
		

		RenderedImage renderedImage = imageGenerator.generateImage(candidate);

		return renderedImage;
	}
	
	/**
	 * 
	 * @param candidateList
	 * @return
	 * @throws Exception
	 */
	private java.util.List<RenderedImage> convertMoleculeFragmentsToImages(
			ICandidate candidate) throws Exception {

		java.util.List<RenderedImage> molImages = new java.util.ArrayList<RenderedImage>();

		de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator imageGenerator = new de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator();
		imageGenerator.setBackgroundColor(new Color(1f, 1f, 1f, 1f));
		imageGenerator.setImageHeight(200);
		imageGenerator.setImageWidth(200);
		
		MatchList matchList = candidate.getMatchList();
		
		for (int i = 0; i < matchList.getNumberElements(); i++) {

			RenderedImage renderedImage = imageGenerator.generateImage(candidate.getPrecursorMolecule(), matchList.getElement(i).getBestMatchedFragment());

			molImages.add(renderedImage);

		}
		return molImages;
	}
	
	@Override
	public void nullify() {
		
	}
	
	public static void main(String[] args) {
		String peakListFilePath = "/tmp/peaklist_file_example_1.txt";
		String candidateListFilePath = "/tmp/candidate_file_example_1.txt";
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		//set peaklist path and candidate list path
		settings.set(VariableNames.PEAK_LIST_PATH_NAME, peakListFilePath);
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, candidateListFilePath);
		//set needed parameters
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.001);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 253.966126);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalCSV");
		
		CombinedMetFragProcess metfragProcess = new CombinedMetFragProcess(settings);
		
		try {
			metfragProcess.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		metfragProcess.run();
		
		ScoredCandidateList scoredCandidateList = (ScoredCandidateList)metfragProcess.getCandidateList();
		
		CandidateWriterXLS writer = new CandidateWriterXLS();
		ScoredCandidateList scoredCandidateListSingle = new ScoredCandidateList();
		
		scoredCandidateListSingle.addElement(scoredCandidateList.getElement(0));
		
		try {
			writer.write(scoredCandidateListSingle, "test_candidate", "/tmp");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".xls"), 
				list, null);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), 
				list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
