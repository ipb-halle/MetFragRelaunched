package de.ipbhalle.metfraglib.mzml;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

public class TestMZML {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		java.io.File file = new java.io.File("/home/cruttkie/github/msdk/msdk-io/msdk-io-mzml/src/test/resources/5peptideFT.mzML");
		//java.io.File file = new java.io.File("/mnt/isilon/data/IPB/Transfer/sscharfe/fremde RSkripte/ms files/MIK_PP6A-459_test.mzML");
		if(!file.exists()) {
			System.err.println("File does not exist!");
			System.exit(1);
		}
		MzMLFileImportMethod mzml = new MzMLFileImportMethod(file);
		RawDataFile result;
		try {
			result = mzml.execute();
			java.util.List<MsScan> scans = result.getScans();
			SpectrumImage specImg = new SpectrumImage(1000, 600);
			int numberSpectra = 0;
			System.out.println("read " + scans.size() + " spectra");
			for(int i = 0; i < scans.size(); i++) {
				java.util.List<IsolationInfo> iso = scans.get(i).getIsolations();
				for(int k = 0; k < iso.size(); k++) {
					numberSpectra++;
					String filename = "/tmp/spec_" + scans.get(i).getChromatographyInfo().getRetentionTime() + "_" + scans.get(i).getScanNumber() + "_" + Math.round(iso.get(k).getPrecursorMz()) + "_" + (k + 1) + ".jpg";
					specImg.drawMS2SpectrumImage(scans.get(i), iso.get(k), filename, true);
				}
			}
			System.out.println(numberSpectra + " isolation spectra found");
		} catch (MSDKException e) {
			e.printStackTrace();
		}
	}

}
