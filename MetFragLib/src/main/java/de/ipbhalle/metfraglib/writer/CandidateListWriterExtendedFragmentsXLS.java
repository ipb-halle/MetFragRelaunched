package de.ipbhalle.metfraglib.writer;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterExtendedFragmentsXLS implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File xlsFile, IList list, Settings settings) throws Exception {
		CandidateList candidateList = null;
		int numberOfPeaksUsed = 0;
		if (list instanceof ScoredCandidateList
				|| list instanceof SortedScoredCandidateList) {
			candidateList = (ScoredCandidateList) list;
			numberOfPeaksUsed = ((ScoredCandidateList) list)
					.getNumberPeaksUsed();
		}
		if (list instanceof CandidateList) {
			candidateList = (CandidateList) list;
		}
		if (candidateList == null)
			return false;

		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			int countExplainedPeaks = 0;
			ICandidate scoredCandidate = candidateList.getElement(i);
			if (scoredCandidate.getMatchList() != null) {
				MatchList matchList = scoredCandidate.getMatchList();
				for (int l = 0; l < matchList.getNumberElements(); l++) {
					try {
						matchList.getElement(l).getMatchedPeak().getIntensity();
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					countExplainedPeaks++;
				}
			}
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";

			if (scoredCandidate.getMatchList() != null) {
				for (int ii = 0; ii < scoredCandidate.getMatchList()
						.getNumberElements(); ii++) {
					try {
						double intensity = scoredCandidate.getMatchList()
								.getElement(ii).getMatchedPeak().getIntensity();
						peaksExplained += scoredCandidate.getMatchList()
								.getElement(ii).getMatchedPeak().getMass()
								+ "_" + intensity + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					String formula = scoredCandidate.getMatchList()
							.getElement(ii)
							.getModifiedFormulaStringOfBestMatchedFragment();
					sumFormulasOfFragmentsExplainedPeaks += scoredCandidate
							.getMatchList().getElement(ii).getMatchedPeak()
							.getMass()
							+ ":" + formula + ";";

				}
				if (sumFormulasOfFragmentsExplainedPeaks.length() != 0)
					sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks
							.substring(0, sumFormulasOfFragmentsExplainedPeaks
									.length() - 1);
				if (peaksExplained.length() != 0)
					peaksExplained = peaksExplained.substring(0,
							peaksExplained.length() - 1);
				if (peaksExplained.length() == 0)
					peaksExplained = "NA";
				if (sumFormulasOfFragmentsExplainedPeaks.length() == 0)
					sumFormulasOfFragmentsExplainedPeaks = "NA";
				scoredCandidate.setProperty("ExplPeaks", peaksExplained);
				scoredCandidate.setProperty("FormulasOfExplPeaks",
						sumFormulasOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("NumberPeaksUsed",
						numberOfPeaksUsed);
				scoredCandidate.setProperty("NoExplPeaks", countExplainedPeaks);
			}
		}

		boolean withImages = true;
		boolean withFragments = true;

		xlsFile.createNewFile();
		WritableWorkbook workbook = Workbook.createWorkbook(xlsFile);
		WritableSheet sheet1 = workbook.createSheet("MetFrag result list", 0);

		WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
		WritableCellFormat arial10format = new WritableCellFormat(arial10font);
		arial10font.setBoldStyle(WritableFont.BOLD);

		int numberCells = 0;
		java.util.Map<String, Integer> labels = new java.util.HashMap<String, Integer>();
		int columnWidthAdd = withImages || withFragments ? 3 : 0;
		int rowHeightAdd = withImages || withFragments ? 9 : 1;
		List<RenderedImage> molImages = null;
		List<List<RenderedImage>> molFragmentImages = null;
		if (withImages) {

			molImages = convertMoleculesToImages(candidateList);
			for (int i = 0; i < molImages.size(); i++) {
				File imageFile = File.createTempFile("file" + i, ".png",
						new File(Constants.OS_TEMP_DIR));
				imageFile.deleteOnExit();
				if (ImageIO.write(molImages.get(i), "png", imageFile)) {
					WritableImage wi = new WritableImage(0,
							(i * rowHeightAdd) + 1, columnWidthAdd,
							rowHeightAdd, imageFile);
					sheet1.addImage(wi);
				}
			}
		}
		if (withFragments) {
			molFragmentImages = convertMoleculesAndFragmentToImages(candidateList);
			WritableSheet sheet2 = workbook.createSheet(
					"MetFrag fragment lists", 1);
			sheet2.addCell(new Label(0, 0, "Precursor", arial10format));
			sheet2.addCell(new Label(4, 0, "Identifier", arial10format));
			sheet2.addCell(new Label(5, 0, "Fragments", arial10format));

			for (int i = 0; i < molFragmentImages.size(); i++) {
				File imageFile = File.createTempFile("file" + i, ".png",
						new File(Constants.OS_TEMP_DIR));
				imageFile.deleteOnExit();
				if (ImageIO.write(molFragmentImages.get(i).get(0), "png",
						imageFile)) {
					WritableImage wi = new WritableImage(0,
							(i * rowHeightAdd) + 1, columnWidthAdd,
							rowHeightAdd, imageFile);
					sheet2.addImage(wi);
				}
				for (int k = molFragmentImages.get(i).size() - 1; k >= 1; k--) {
					File fragmentimageFile = File.createTempFile("file_" + i
							+ "_" + k, ".png", new File(Constants.OS_TEMP_DIR));
					fragmentimageFile.deleteOnExit();
					if (ImageIO.write(molFragmentImages.get(i).get(k), "png",
							fragmentimageFile)) {
						WritableImage wi = new WritableImage(
								((molFragmentImages.get(i).size() - k) * columnWidthAdd) + 2,
								(i * rowHeightAdd) + 1, columnWidthAdd,
								rowHeightAdd, fragmentimageFile);
						sheet2.addImage(wi);
					}
				}
				sheet2.addCell(new Label(4, (i * rowHeightAdd) + 1,
						candidateList.getElement(i).getIdentifier() + ""));
			}

		}

		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			java.util.Hashtable<String, Object> properties = candidateList
					.getElement(i).getProperties();
			Iterator<String> propNames = properties.keySet().iterator();

			while (propNames.hasNext()) {
				String propName = (String) propNames.next();
				if (!labels.containsKey(propName)) {
					labels.put(propName, new Integer(numberCells));
					sheet1.addCell(new Label(labels.get(propName)
							+ columnWidthAdd, 0, propName, arial10format));
					numberCells++;
				}
				sheet1.addCell(new Label(labels.get(propName) + columnWidthAdd,
						(i * rowHeightAdd) + 1, String.valueOf(checkEmptyProperty(properties
								.get(propName)))));
			}

		}

		workbook.write();
		workbook.close();
		return true;
	}

	private Object checkEmptyProperty(Object prop) {
		try {
			String value = (String)prop;
			if(value.trim().length() == 0) return "NA";
		}
		catch(Exception e) {
			return prop;
		}
		return prop;
	}
	
	/**
	 * 
	 * @param mols
	 * @return
	 * @throws Exception 
	 */
	private List<RenderedImage> convertMoleculesToImages(
			CandidateList candidateList) throws Exception {

		List<RenderedImage> molImages = new ArrayList<RenderedImage>();

		de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator imageGenerator = new de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator();
		imageGenerator.setImageHeight(200);
		imageGenerator.setImageWidth(200);
		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			RenderedImage renderedImage = null;
			renderedImage = imageGenerator.generateImage(candidateList
					.getElement(i));
			if (renderedImage == null) {
				molImages.add(new java.awt.image.BufferedImage(imageGenerator
						.getImageWidth(), imageGenerator.getImageHeight(),
						java.awt.image.BufferedImage.TYPE_INT_RGB));
			} else
				molImages.add(renderedImage);
		}
		return molImages;
	}

	/**
	 * 
	 * @param candidateList
	 * @return
	 */
	private List<List<RenderedImage>> convertMoleculesAndFragmentToImages(
			CandidateList candidateList) throws Exception {

		List<List<RenderedImage>> molImages = new ArrayList<List<RenderedImage>>();

		de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator imageGenerator = new de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator();

		imageGenerator.setImageHeight(200);
		imageGenerator.setImageWidth(200);

		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			List<RenderedImage> currentList = new ArrayList<RenderedImage>();
			RenderedImage renderedImage = imageGenerator
					.generateImage(candidateList.getElement(i));
			if (renderedImage != null) {
				currentList.add(renderedImage);
			} else
				currentList.add(new java.awt.image.BufferedImage(imageGenerator
						.getImageWidth(), imageGenerator.getImageHeight(),
						java.awt.image.BufferedImage.TYPE_INT_RGB));
			if (renderedImage != null) {
				MatchList matchList = candidateList.getElement(i)
						.getMatchList();
				for (int k = 0; k < matchList.getNumberElements(); k++) {
					currentList.add(imageGenerator.generateImage(matchList
							.getElement(k).getBestMatchedFragment()));
				}
			}
			molImages.add(currentList);
		}
		return molImages;
	}

	public void nullify() {

	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list);
	}

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + "_extendedFragments.xls"), list);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}


}
