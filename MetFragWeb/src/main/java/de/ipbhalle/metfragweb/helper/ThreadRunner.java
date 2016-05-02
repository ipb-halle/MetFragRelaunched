package de.ipbhalle.metfragweb.helper;

import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;

public class ThreadRunner implements Runnable {

	protected BeanSettingsContainer beanSettingsContainer;
	protected Messages errorMessages;
	protected Messages infoMessages;
	protected boolean interrupted = false;
	
	public ThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages) {
		this.beanSettingsContainer = beanSettingsContainer;
		this.errorMessages = errorMessages;
		this.infoMessages = infoMessages;
	}
	
	public boolean isInterrupted() {
		return this.interrupted;
	}
	
	public void setInterrupted(boolean value) {
		this.interrupted = value;
	}
	
	public BeanSettingsContainer getBeanSettingsContainer() {
		return beanSettingsContainer;
	}

	public void setBeanSettingsContainer(BeanSettingsContainer beanSettingsContainer) {
		this.beanSettingsContainer = beanSettingsContainer;
	}

	public Messages getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(Messages errorMessages) {
		this.errorMessages = errorMessages;
	}

	public Messages getInfoMessages() {
		return infoMessages;
	}

	public void setInfoMessages(Messages infoMessages) {
		this.infoMessages = infoMessages;
	}

	@Override
	public void run() {
		System.out.println("here");
	}
}
