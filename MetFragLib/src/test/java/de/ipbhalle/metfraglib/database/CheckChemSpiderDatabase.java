package de.ipbhalle.metfraglib.database;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

public class CheckChemSpiderDatabase {

	private IDatabase database;
	private Settings settings;
	
	@Before
	public void setUp() {
		this.settings = this.readDatabaseConfigFromFile();
		org.junit.Assume.assumeNotNull(this.settings);
	}

	@Test
	public void testMassSearch() {
		this.database = new OnlineChemSpiderDatabase(this.settings);
		try {
			assertTrue("No result for mass search", this.database.getCandidateIdentifiers(253.966126, 5).size() > 0);
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testForumlaSearch() {
		this.database = new OnlineChemSpiderDatabase(this.settings);
		try {
			assertTrue("No result for formula search", this.database.getCandidateIdentifiers("C7H5Cl2FN2O3").size() > 0);
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testIdentifierSearch() {
		this.database = new OnlineChemSpiderDatabase(this.settings);
		try {
			assertTrue("No result for identifier search", this.database.getCandidateIdentifiers(new String[] {"45757"}).size() > 0);
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRetrievingCandidateList() {
		this.database = new OnlineChemSpiderDatabase(this.settings);
		try {
			java.util.Vector<String> ids = new java.util.Vector<String>();
			ids.add("45757");
			assertTrue("No result for identifier search", this.database.getCandidateByIdentifier(ids).getNumberElements() > 0);
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
