package de.ipbhalle.metfragweb.controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

@ManagedBean
@SessionScoped
public class CompareSpectraBean {
	
	private String peakList1;
	private String peakList2;
	
	protected LineChartModel spectrumModel;
	
	/*
	 * bean initialisation
	 */
	public CompareSpectraBean() {
		System.out.println("CompareSpectraBean");
	}
	
	@PostConstruct
	public void init() {
		this.peakList1 = "90.97445 681\n" +
				"106.94476 274\n" +
				"110.02750 110\n" +
				"115.98965 95\n";
		this.peakList2 = "115.98965 95\n" +
				"117.98540 384\n" +
				"124.93547 613\n" +
				"124.99015 146\n";
		this.generateSpectrumModelView();
    }
	
	public void setPeakList1(String peakList1) {
		this.peakList1 = peakList1;
	}

	public void setPeakList2(String peakList2) {
		this.peakList2 = peakList2;
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
		this.generateSpectrumModelView();
	}

	public LineChartModel getSpectrumModel() {
		return this.spectrumModel;
	}

	public boolean isSpectrumViewAvailable() {
		return this.spectrumModel != null;
	}

	public boolean generateSpectrumModelView() {
		this.spectrumModel = new LineChartModel();
		if(this.peakList1 == null || this.peakList2 == null) {
			System.out.println("Error: 2 peaklists required.");
			this.spectrumModel = null;
			return false;
		}
		String string1 = (this.peakList1).trim();
		String string2 = (this.peakList2).trim();
		try {
			if(string1.length() == 0) throw new Exception();
			if(string2.length() == 0) throw new Exception();
			DefaultPeakList peaklistObject1 = this.generatePeakListObject(string1);
			DefaultPeakList peaklistObject2 = this.generatePeakListObject(string2);
			double minMZ1 = ((TandemMassPeak)peaklistObject1.getElement(0)).getMass();
			double minMZ2 = ((TandemMassPeak)peaklistObject2.getElement(0)).getMass();
			double maxMZ1 = ((TandemMassPeak)peaklistObject1.getElement(peaklistObject1.getNumberElements() - 1)).getMass();
			double maxMZ2 = ((TandemMassPeak)peaklistObject2.getElement(peaklistObject2.getNumberElements() - 1)).getMass();
			double maxMZ = maxMZ2 > maxMZ1 ? maxMZ2 : maxMZ1;
			double minMZ = minMZ2 > minMZ1 ? minMZ2 : minMZ1;
			this.spectrumModel.getAxis(AxisType.Y).setMin(-1050);
			this.spectrumModel.getAxis(AxisType.Y).setMax(1050);
			this.spectrumModel.getAxis(AxisType.Y).setLabel("Intensity");
			this.spectrumModel.getAxis(AxisType.Y).setTickInterval("250");
			this.spectrumModel.getAxis(AxisType.Y).setTickCount(5);
			this.spectrumModel.getAxis(AxisType.X).setMin(minMZ - (maxMZ * 0.1));
			this.spectrumModel.getAxis(AxisType.X).setTickAngle(-30);
			this.spectrumModel.getAxis(AxisType.X).setLabel("m/z");
			this.spectrumModel.getAxis(AxisType.X).setTickFormat("%.2f");
			this.spectrumModel.setZoom(true);
			this.spectrumModel.setMouseoverHighlight(true);
			this.spectrumModel.setShowDatatip(false);
			this.spectrumModel.setShowPointLabels(false);
			this.spectrumModel.setExtender("spectrumViewExtender");
			String xTickInterval = "100.000";
			if(maxMZ <= 400) xTickInterval = "50.000";
			if(maxMZ <= 150) xTickInterval = "10.000"; 
			this.spectrumModel.getAxis(AxisType.X).setTickInterval(xTickInterval);
			String seriesColors = "";
			for(int i = 0; i < peaklistObject1.getNumberElements(); i++) 
			{
				TandemMassPeak peak = (TandemMassPeak)peaklistObject1.getElement(i);
				LineChartSeries newSeries = new LineChartSeries();
				newSeries.set(peak.getMass() + 0.0000001, 0);
				newSeries.set(peak.getMass(), peak.getRelativeIntensity());
				this.spectrumModel.addSeries(newSeries);
				seriesColors += "66cc66,";
			}
			for(int i = 0; i < peaklistObject2.getNumberElements(); i++) 
			{
				TandemMassPeak peak = (TandemMassPeak)peaklistObject2.getElement(i);
				LineChartSeries newSeries = new LineChartSeries();
				newSeries.set(peak.getMass() + 0.0000001, 0);
				newSeries.set(peak.getMass(), -peak.getRelativeIntensity());
				this.spectrumModel.addSeries(newSeries);
				seriesColors += "00749f,";
			}
			seriesColors = seriesColors.substring(0, seriesColors.length() - 1);
			this.spectrumModel.setSeriesColors(seriesColors);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error: Invalid peak list value.");
			this.spectrumModel = null;
			return false;	
		}
		return true;
	}
}


