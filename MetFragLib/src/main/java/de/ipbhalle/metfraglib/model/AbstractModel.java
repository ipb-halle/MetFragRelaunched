package de.ipbhalle.metfraglib.model;

import de.ipbhalle.metfraglib.interfaces.IModel;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractModel implements IModel {

	protected Settings settings;
	
	public AbstractModel(Settings settings) {
		this.settings = settings;
	}

	public abstract void nullify();
}
