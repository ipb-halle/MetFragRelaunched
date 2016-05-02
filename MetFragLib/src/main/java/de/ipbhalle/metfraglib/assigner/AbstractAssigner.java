package de.ipbhalle.metfraglib.assigner;

import de.ipbhalle.metfraglib.interfaces.IAssigner;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractAssigner implements IAssigner {

	protected Settings settings;
	
	public AbstractAssigner(Settings settings) {
		this.settings = settings;
	}
	
}
