package de.ipbhalle.metfragweb.controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
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
				"115.98965 95\n" +
				"117.98540 384\n" +
				"124.93547 613\n" +
				"124.99015 146\n" +
				"125.99793 207\n" +
				"133.95592 777\n" +
				"143.98846 478\n" +
				"144.99625 352\n" +
				"146.00410 999\n" +
				"151.94641 962\n" +
				"160.96668 387\n" +
				"163.00682 782\n" +
				"172.99055 17\n" +
				"178.95724 678\n" +
				"178.97725 391\n" +
				"180.97293 999\n" +
				"196.96778 720\n" +
				"208.96780 999\n" +
				"236.96245 999\n" +
				"254.97312 999";;
		this.peakList2 = "90.97445 681\n" +
				"106.94476 274\n" +
				"110.02750 110\n" +
				"115.98965 95\n" +
				"117.98540 384\n" +
				"124.93547 613\n" +
				"124.99015 146\n" +
				"125.99793 207\n" +
				"133.95592 777\n" +
				"143.98846 478\n" +
				"144.99625 352\n" +
				"146.00410 999\n" +
				"151.94641 962\n" +
				"160.96668 387\n" +
				"163.00682 782\n" +
				"172.99055 17\n" +
				"178.95724 678\n" +
				"178.97725 391\n" +
				"180.97293 999\n" +
				"196.96778 720\n" +
				"208.96780 999\n" +
				"236.96245 999\n" +
				"254.97312 999";
		this.generateSpectrumModelView();
    }
	
	public void setPeakList1(String peakList1) {
		System.out.println("setPeakList1");
		this.peakList1 = peakList1;
	}

	public void setPeakList2(String peakList2) {
		this.peakList1 = peakList2;
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
		System.out.println("getSpectrumModelView " + this.spectrumModel.getSeries().size());
		return this.spectrumModel;
	}

	public boolean isSpectrumViewAvailable() {
		return this.spectrumModel != null;
	}

	public boolean generateSpectrumModelView() {
		this.spectrumModel = new LineChartModel();
		 
		ChartSeries boys = new ChartSeries();
        boys.setLabel("Boys");
        boys.set("2004", 120);
        boys.set("2005", 100);
        boys.set("2006", 44);
        boys.set("2007", 150);
        boys.set("2008", 25);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Girls");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 110);
        girls.set("2007", 90);
        girls.set("2008", 120);
 
        this.spectrumModel.addSeries(boys);
        this.spectrumModel.addSeries(girls);
         
        return true;
	}
	
	public boolean generateSpectrumModelView2() {
		System.out.println("generateSpectrumModelView");
		System.out.println(this.peakList1);
		this.spectrumModel = new LineChartModel();
		if(this.peakList1 == null || this.peakList2 == null) {
			System.out.println("Error: 2 peaklists required.");
			this.spectrumModel = null;
			return false;
		}
		String string1 = (this.peakList1).trim();
		//String string2 = (this.peakList1).trim();
		try {
			if(string1.length() == 0) throw new Exception();
			DefaultPeakList peaklistObject1 = this.generatePeakListObject(string1);
			double maxMZ = ((TandemMassPeak)peaklistObject1.getElement(peaklistObject1.getNumberElements() - 1)).getMass();
			this.spectrumModel.getAxis(AxisType.Y).setMin(0);
			this.spectrumModel.getAxis(AxisType.Y).setMax(1050);
			this.spectrumModel.getAxis(AxisType.Y).setLabel("Intensity");
			this.spectrumModel.getAxis(AxisType.Y).setTickInterval("250");
			this.spectrumModel.getAxis(AxisType.Y).setTickCount(5);
			this.spectrumModel.getAxis(AxisType.X).setMin(0.0);
			this.spectrumModel.getAxis(AxisType.X).setTickAngle(-30);
			this.spectrumModel.getAxis(AxisType.X).setLabel("m/z");
			this.spectrumModel.getAxis(AxisType.X).setTickFormat("%.2f");
			this.spectrumModel.setZoom(true);
			this.spectrumModel.setMouseoverHighlight(true);
			this.spectrumModel.setShowDatatip(false);
			this.spectrumModel.setShowPointLabels(false);
			String xTickInterval = "100.000";
			if(maxMZ <= 400) xTickInterval = "50.000";
			if(maxMZ <= 150) xTickInterval = "10.000"; 
			this.spectrumModel.getAxis(AxisType.X).setTickInterval(xTickInterval);
			for(int i = 0; i < peaklistObject1.getNumberElements(); i++) 
			{
				TandemMassPeak peak = (TandemMassPeak)peaklistObject1.getElement(i);
				LineChartSeries newSeries = new LineChartSeries();
				newSeries.set(peak.getMass() + 0.0000001, -10000000.0);
				newSeries.set(peak.getMass(), peak.getRelativeIntensity());
				this.spectrumModel.addSeries(newSeries);
			}
			this.spectrumModel.setSeriesColors("00749f");
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


