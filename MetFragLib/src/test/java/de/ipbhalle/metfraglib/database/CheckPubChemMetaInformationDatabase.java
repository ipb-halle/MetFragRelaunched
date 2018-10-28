package de.ipbhalle.metfraglib.database;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

public class CheckPubChemMetaInformationDatabase {

	private IDatabase database;
	private Settings settings;
	
	@Before
	public void setUp() {
		this.settings = this.readDatabaseConfigFromFile();
		org.junit.Assume.assumeNotNull(this.settings);
	}

	@Test
	public void testIdentifierSearch() {
		this.database = new OnlineExtendedPubChemDatabase(this.settings);
		try {
			java.util.ArrayList<String> identifiers = this.database.getCandidateIdentifiers(new String[] {"50465"});
			CandidateList candidateList = this.database.getCandidateByIdentifier(identifiers);
			assertTrue("No result for in candidate list", candidateList.getNumberElements() > 0);;
			assertTrue("Could not fetch references", (Double)candidateList.getElement(0).getProperty(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME) > 0);
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public MetFragGlobalSettings readDatabaseConfigFromFile() {
		try {
			String peakListFilePath = ClassLoader.getSystemResource("settings.properties").getFile();
			MetFragGlobalSettings settings = MetFragGlobalSettings.readSettings(new java.io.File(peakListFilePath), null);
			return settings;
		} catch (Exception e) {
			return null;
		}
	}
	
}
