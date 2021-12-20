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
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterXLS implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File xlsFile, IList list, Settings settings)
			throws Exception {
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
		
		java.util.ArrayList<Integer> correctIndeces = new java.util.ArrayList<Integer>();
		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			int countExplainedPeaks = 0;
			ICandidate scoredCandidate = candidateList.getElement(i);
			if(settings != null) scoredCandidate.setUseSmiles((Boolean)settings.get(VariableNames.USE_SMILES_NAME));
			try {
				scoredCandidate.initialisePrecursorCandidate();
			} catch(Exception e) {
				continue;
			}
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
							.getModifiedFormulaStringOfBestMatchedFragment(scoredCandidate.getPrecursorMolecule());
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
			scoredCandidate.resetPrecursorMolecule();
			correctIndeces.add(i);
		}

		boolean withImages = false;

		xlsFile.createNewFile();
		WritableWorkbook workbook = Workbook.createWorkbook(xlsFile);
		WritableSheet sheet = workbook.createSheet("MetFrag result list", 0);

		WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10);
		WritableCellFormat arial10format = new WritableCellFormat(arial10font);
		try {
			arial10font.setBoldStyle(WritableFont.BOLD);
		} catch (WriteException e1) {
			System.out.println("Warning: Could not set WritableFont");
		}
		int numberCells = 0;
		java.util.Map<String, Integer> labels = new java.util.HashMap<String, Integer>();
		int columnWidthAdd = withImages ? 3 : 0;
		int rowHeightAdd = withImages ? 9 : 1;
		List<RenderedImage> molImages = null;
		if (withImages) {

			molImages = convertMoleculesToImages(candidateList);
			for (int i = 0; i < molImages.size(); i++) {
				// File imageFile = new File(resultspath + fileSep + fileName+
				// "_" +i+".png");
				File imageFile = File.createTempFile("file" + i, ".png",
						new File(Constants.OS_TEMP_DIR));
				imageFile.deleteOnExit();
				if (ImageIO.write(molImages.get(i), "png", imageFile)) {
					WritableImage wi = new WritableImage(0,
							(i * rowHeightAdd) + 1, columnWidthAdd,
							rowHeightAdd, imageFile);
					sheet.addImage(wi);
				}

			}
		}

		for (int i = 0; i < correctIndeces.size(); i++) {
			java.util.Hashtable<String, Object> properties = candidateList
					.getElement(correctIndeces.get(i)).getProperties();
			Iterator<String> propNames = properties.keySet().iterator();

			while (propNames.hasNext()) {
				String propName = (String) propNames.next();
				if (!labels.containsKey(propName)) {
					labels.put(propName, Integer.valueOf(numberCells));
					try {
						sheet.addCell(new Label(labels.get(propName)
								+ columnWidthAdd, 0, propName, arial10format));
					} catch (RowsExceededException e) {
						e.printStackTrace();
					} catch (WriteException e) {
						e.printStackTrace();
					}
					numberCells++;
				}
				try {
					String prop = String.valueOf(properties.get(propName));
					if(prop.trim().length() == 0) prop = "NA";
					if(propName.equals(VariableNames.IDENTIFIER_NAME)) prop = prop.replaceAll("\\|[0-9]+", "");
					sheet.addCell(new Label(labels.get(propName)
							+ columnWidthAdd, (i * rowHeightAdd) + 1, prop));
				} catch (RowsExceededException e) {
					e.printStackTrace();
				} catch (WriteException e) {
					e.printStackTrace();
				}
			}

		}

		workbook.write();
		workbook.close();
		return true;

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

			RenderedImage renderedImage = imageGenerator
					.generateImage(candidateList.getElement(i));

			molImages.add(renderedImage);

		}
		return molImages;
	}

	public void nullify() {

	}

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR
				+ filename + ".xls"), list, null);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
