package de.ipbhalle.metfraglib.process;

import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.concurrent.atomic.AtomicInteger;

public class ProcessingStatus {

	protected AtomicInteger nextPercentageValue;
	protected String processStatusString;
	protected String retrievingStatusString;
	protected AtomicInteger numberCandidates;
	protected AtomicInteger numberFinishedCandidates;
	protected AtomicInteger numberErrorCandidates;
	protected AtomicInteger numberPreFilteredCandidates;
	protected AtomicInteger percentageValue;
    protected static final Logger logger = LogManager.getLogger();

    public ProcessingStatus(Settings settings) {
		this.processStatusString = "Processing Candidates";
		this.retrievingStatusString = "Retrieving Candidates";
		this.nextPercentageValue = new AtomicInteger(1);
		this.percentageValue = new AtomicInteger(0);
		this.numberCandidates = new AtomicInteger(0);
		this.numberFinishedCandidates = new AtomicInteger(0);
		this.numberErrorCandidates = new AtomicInteger(0);
		this.numberPreFilteredCandidates = new AtomicInteger(0);
        Configurator.setLevel(logger.getName(), (Level)settings.get(VariableNames.LOG_LEVEL_NAME));
	}

	public synchronized void checkNumberFinishedCandidates() {
		if(((double)(numberFinishedCandidates.incrementAndGet()) / numberCandidates.get()) * 10.0 >= nextPercentageValue.doubleValue()) {
			int old = nextPercentageValue.get();
			this.nextPercentageValue.set((int)Math.ceil((numberFinishedCandidates.doubleValue() / numberCandidates.doubleValue()) * 10.0));
			if(old == nextPercentageValue.get()) nextPercentageValue.incrementAndGet();
			this.percentageValue.set((int)Math.round(((numberFinishedCandidates.doubleValue() / numberCandidates.doubleValue()) * 10.0)) * 10);
			//this.processStatusString = "Processing Candidates - " + percentageValue + " %";
			this.processStatusString = "Processing Candidates";
			this.logger.info(this.percentageValue + " %");
			// run garage collector after clearing fragments
		//	System.gc();
		}
	}
	
	public synchronized void increaseNumberErrorCandidates() {
		this.numberErrorCandidates.incrementAndGet();
	}

	public synchronized void increaseNumberPreFilteredCandidates() {
		this.numberPreFilteredCandidates.incrementAndGet();
	}
	
	public AtomicInteger getNextPercentageValue() {
		return nextPercentageValue;
	}

	public void setNextPercentageValue(int nextPercentageValue) {
		this.nextPercentageValue.set(nextPercentageValue);
	}

	public String getProcessStatusString() {
		return this.processStatusString;
	}

	public void setProcessStatusString(String processStatusString) {
		this.processStatusString = processStatusString;
	}

	public AtomicInteger getNumberCandidates() {
		return numberCandidates;
	}

	public void setNumberCandidates(int numberCandidates) {
		this.numberCandidates = new AtomicInteger(numberCandidates);
	}

	public AtomicInteger getNumberFinishedCandidates() {
		return numberFinishedCandidates;
	}

	public void setNumberFinishedCandidates(int numberFinishedCandidates) {
		this.numberFinishedCandidates.set(numberFinishedCandidates);
	}

	public AtomicInteger getNumberErrorCandidates() {
		return numberErrorCandidates;
	}

	public void setNumberErrorCandidates(AtomicInteger numberErrorCandidates) {
		this.numberErrorCandidates = numberErrorCandidates;
	}

	public AtomicInteger getNumberPreFilteredCandidates() {
		return numberPreFilteredCandidates;
	}

	public void setNumberPreFilteredCandidates(int numberPreFilteredCandidates) {
		this.numberPreFilteredCandidates.set(numberPreFilteredCandidates);
	}

	public String getRetrievingStatusString() {
		return retrievingStatusString;
	}

	public void setRetrievingStatusString(String retrievingStatusString) {
		this.retrievingStatusString = retrievingStatusString;
	}
	
	public AtomicInteger getPercentageValue() {
		return percentageValue;
	}

	public void resetValues() {
		this.processStatusString = "Processing Candidates";
		this.retrievingStatusString = "Retrieving Candidates";
		this.nextPercentageValue = new AtomicInteger(1);
		this.percentageValue = new AtomicInteger(0);
		this.numberCandidates = new AtomicInteger(0);
		this.numberFinishedCandidates = new AtomicInteger(0);
		this.numberErrorCandidates = new AtomicInteger(0);
		this.numberPreFilteredCandidates = new AtomicInteger(0);
	}
}
