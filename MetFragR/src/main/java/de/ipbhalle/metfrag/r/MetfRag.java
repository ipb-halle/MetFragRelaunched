package de.ipbhalle.metfrag.r;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fragmenter.TopDownNeutralLossFragmenter;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

class MetfRag {
	
	public static CandidateList runMetFrag(MetFragGlobalSettings settings) {
		SettingsChecker checker = new SettingsChecker();
		if(!checker.check(settings, false)) {
			CandidateList candidateList = new CandidateList();
			return candidateList;
		}

		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.resolver.ArtefactResolver", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.source.JarSource", Level.ERROR);

		//ChemSpider
		Configurator.setLevel("httpclient.wire.content", Level.ERROR);
		Configurator.setLevel("httpclient.wire.header", Level.ERROR);
		
		Configurator.setLevel("org.apache.commons.httpclient.HeaderElement", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpConnection", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpMethodBase", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpClient", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.util.IdleConnectionHandler", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.MultiThreadedHttpConnectionManager", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpClient", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HeaderElement", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpParser", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.EntityEnclosingMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.PostMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.EntityEnclosingMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.cookie.CookieSpec", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpState", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.ExpectContinueMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.EntityEnclosingMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpMethodDirector", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.params.DefaultHttpParams", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.PostMethod", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.HttpParser", Level.ERROR);
		Configurator.setLevel("org.apache.commons.httpclient.methods.EntityEnclosingMethod", Level.ERROR);
		
		Configurator.setLevel("org.apache.axis2.description.AxisOperation", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.llom.OMElementImpl", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.engine.Phase", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.http.CommonsHTTPTransportSender", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.context.ConfigurationContext", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.addressing.AddressingHelper", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.dispatchers.AddressingBasedDispatcher", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.engine.AxisEngine", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.builder.BuilderUtil", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.TransportUtils", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.client.Options", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.context.MessageContext", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.http.HTTPSender", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.http.SOAPMessageFormatter", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.description.AxisOperation", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.engine.AxisConfiguration", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.description.OutInAxisOperationClient", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.description.AxisService", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.addressing.EndpointReference", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.http.AbstractHTTPSendere", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.i18n.ProjectResourceBundle", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.description.ParameterIncludeImpl", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.deployment.ModuleBuilder", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.handlers.addressing.AddressingInHandler", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.deployment.DeploymentEngine", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.deployment.ModuleDeployer", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.transport.http.AbstractHTTPSender", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.util.Loader", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.deployment.RepositoryListener", Level.ERROR);
		Configurator.setLevel("org.apache.axis2.context.AbstractContext", Level.ERROR);
		
		Configurator.setLevel("org.apache.axiom.om.impl.builder.StAXOMBuilder", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.locator.DefaultOMMetaFactoryLocator", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.util.StAXUtils", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.MTOMXMLStreamWriter", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.OMOutputFormat", Level.ERROR);		
		Configurator.setLevel("org.apache.axiom.soap.impl.llom.SOAPEnvelopeImpl", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.llom.OMContainerHelper", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.locator.ImplementationFactory", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.locator.PriorityBasedOMMetaFactoryLocator", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.locator.ImplementationFactory", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.common.AxiomContainerSupport", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.common.serializer.pull.PullSerializer", Level.ERROR);
		Configurator.setLevel("org.apache.axiom.om.impl.common.serializer.pull.Navigator", Level.ERROR);	
		Configurator.setLevel("org.apache.axiom.om.impl.builder.StAXBuilder", Level.ERROR);	
		
		settings.set(VariableNames.LOG_LEVEL_NAME, Level.INFO);
		
		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
		
		try {
			boolean candidatesRetrieved = mp.retrieveCompounds();
			if(!candidatesRetrieved) return new CandidateList();
		} catch (Exception e2) {
			System.err.println("Error retrieving candidates");
			e2.printStackTrace();
			return new CandidateList();
		}
		
		try {
			mp.run();
		} catch (Exception e) {
			System.err.println("Error running MetFrag process.");
			e.printStackTrace();
			return new CandidateList();
		}
		
		SortedScoredCandidateList scoredCandidateList = (SortedScoredCandidateList)mp.getCandidateList();
		
		addPropertiesToCandidateList(scoredCandidateList);
		
		return scoredCandidateList;
	}
	
	/**
	 * fragment and score molecules stored in a sdf file
	 * 
	 * @param _pathToSDF
	 * @param _masses
	 * @param _intensities
	 * @param _exactMass
	 * @param _numberThreads
	 * @param _mzabs
	 * @param _mzppm
	 * @param _searchppm
	 * @param _posCharge
	 * @param _mode
	 * @param _treeDepth
	 * @return
	 */
	public static IAtomContainer[] scoreMoleculesAgainstSpectrum(String _pathToSDF, double[] _masses, 
			double[] _intensities, double _exactMass, int _numberThreads, double _mzabs, double _mzppm, 
			double _searchppm, boolean _posCharge, int _mode, int _treeDepth, String[] scoreNames, double[] scoreWeights) {

		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.resolver.ArtefactResolver", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.source.JarSource", Level.ERROR);
		
		double mzabs = _mzabs;
		double mzppm = _mzppm;
		double searchppm = _searchppm;
		double exactMass = _exactMass;
		int treeDepth = _treeDepth;
		int mode = _mode;
		int numberThreads = _numberThreads;
		boolean posCharge = _posCharge;
		double[] masses = _masses;
		double[] intensities = _intensities;
		
		IAtomContainer[] resultMols = new IAtomContainer[0];
		if(masses == null) return resultMols;
		if(intensities == null) return resultMols;
		if(masses.length != intensities.length) return resultMols;
		if(exactMass <= 0.0) return resultMols;
		if(numberThreads < -1 || numberThreads > 8) return resultMols;
		if(mzabs < 0.0 || mzppm < 0.0 || searchppm < 0.0) return resultMols;
		if(mode != -1 && mode != 0 && mode != 1) return resultMols;
		if(treeDepth < 1 || treeDepth > 5) return resultMols;
		File sdf = new File(_pathToSDF);
		if(!sdf.exists() || !sdf.canRead()) return resultMols;

		String peaksString = "";
		if(masses.length > 0) peaksString += masses[0] + " " + intensities[0];
		for(int i = 1; i < masses.length; i++) {
			peaksString += "\n" + masses[i] + " " + intensities[i];
		}

		Double[] scoreWeightsObject = new Double[scoreWeights.length];
		for(int i = 0; i < scoreWeightsObject.length; i++)
			scoreWeightsObject[i] = Double.valueOf(scoreWeights[i]);
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaksString);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, exactMass);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalSDF");
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, sdf.getAbsolutePath());
		settings.set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, "de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		settings.set(VariableNames.METFRAG_SCORE_TYPES_NAME, scoreNames);
		settings.set(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, scoreWeightsObject);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, mzppm);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzabs);
		settings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, posCharge);
		settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, mode);
		settings.set(VariableNames.RESET_PRECURSOR_AFTER_PROCESSING, false);

		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
		
		try {
			mp.retrieveCompounds();
		} catch (Exception e2) {
			System.err.println("Error retrieving candidates");
		}
		
		try {
			mp.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error running MetFrag process.");
		}
	
		SortedScoredCandidateList scoredCandidateList = (SortedScoredCandidateList) mp.getCandidateList();
		resultMols = new IAtomContainer[scoredCandidateList.getNumberElements()];
		int numberOfPeaksUsed = scoredCandidateList.getNumberPeaksUsed();
		
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			ICandidate candidate = scoredCandidateList.getElement(i);
			IAtomContainer tmp = null;
			try {
				tmp = candidate.getAtomContainer();
				MoleculeFunctions.prepareAtomContainer(tmp, false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_1_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_1_NAME, candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_2_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_2_NAME, candidate.getProperty(VariableNames.INCHI_KEY_2_NAME));
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_NAME, candidate.getProperty(VariableNames.INCHI_KEY_NAME));
			
			tmp.setProperty(VariableNames.IDENTIFIER_NAME, candidate.getIdentifier());
			IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(tmp);
			Double massDoubleOrig = null;
			try {
				massDoubleOrig = candidate.getMolecularFormula().getMonoisotopicMass();
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
			massDoubleOrig = (double)Math.round((massDoubleOrig) * 10000)/10000;
			tmp.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, massDoubleOrig);
			tmp.setProperty(VariableNames.FINAL_SCORE_COLUMN_NAME, candidate.getProperty(VariableNames.FINAL_SCORE_COLUMN_NAME));
			for(int ii = 0; ii < scoreNames.length; ii++) {
				String scoreClassName = scoreNames[ii];
				tmp.setProperty(scoreClassName, candidate.getProperty(scoreClassName));
				tmp.setProperty(scoreClassName + "_Values", candidate.getProperty(scoreClassName + "_Values"));
			}
			if(candidate.getMatchList() != null) tmp.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, candidate.getMatchList().getNumberElements());
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			if(candidate.getMatchList() != null) {
				for(int ii = 0; ii < candidate.getMatchList().getNumberElements(); ii++) {
					try {
						peaksExplained += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + candidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity() + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						e1.printStackTrace();
					}
					sumFormulasOfFragmentsExplainedPeaks += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
							+ ":" + candidate.getMatchList().getElement(ii).getBestMatchedFragment().getMolecularFormula(candidate.getPrecursorMolecule()) + ";";
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				tmp.setProperty(VariableNames.EXPLAINED_PEAKS_COLUMN, peaksExplained);
				tmp.setProperty(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, sumFormulasOfFragmentsExplainedPeaks);
			}
			
			tmp.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, MolecularFormulaManipulator.getString(molFormula));
			tmp.setProperty(VariableNames.NUMBER_PEAKS_USED_COLUMN, numberOfPeaksUsed);
			resultMols[i] = tmp;
		}
		
		return resultMols;

	}
	
	/**
	 * fragment and score molecules stored in a sdf file
	 * 
	 * @param _pathToSDF
	 * @param _masses
	 * @param _intensities
	 * @param _exactMass
	 * @param _numberThreads
	 * @param _mzabs
	 * @param _mzppm
	 * @param _searchppm
	 * @param _posCharge
	 * @param _mode
	 * @param _treeDepth
	 * @return
	 */
	public static IAtomContainer[] scoreMoleculesAgainstSpectrum(IAtomContainer[] atomContainerArray, double[] _masses, 
			double[] _intensities, double _exactMass, int _numberThreads, double _mzabs, double _mzppm, boolean _posCharge, 
			int _mode, int _treeDepth, String[] scoreNames, double[] scoreWeights) {
		
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.resolver.ArtefactResolver", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.source.JarSource", Level.ERROR);
		
		double mzabs = _mzabs;
		double mzppm = _mzppm;
		double exactMass = _exactMass;
		int treeDepth = _treeDepth;
		int mode = _mode;
		int numberThreads = _numberThreads;
		boolean posCharge = _posCharge;
		double[] masses = _masses;
		double[] intensities = _intensities;
		
		IAtomContainer[] resultMols = new IAtomContainer[0];
		if(masses == null) return resultMols;
		if(intensities == null) return resultMols;
		if(masses.length != intensities.length) return resultMols;
		if(exactMass <= 0.0) return resultMols;
		if(numberThreads < -1 || numberThreads > 8) return resultMols;
		if(mzabs < 0.0 || mzppm < 0.0) return resultMols;
		if(mode != -1 && mode != 0 && mode != 1) return resultMols;
		if(treeDepth < 1 || treeDepth > 5) return resultMols;

		String peaksString = "";
		if(masses.length > 0) peaksString += masses[0] + " " + intensities[0];
		for(int i = 1; i < masses.length; i++) {
			peaksString += "\n" + masses[i] + " " + intensities[i];
		}
		
		Double[] scoreWeightsObject = new Double[scoreWeights.length];
		for(int i = 0; i < scoreWeightsObject.length; i++)
			scoreWeightsObject[i] = Double.valueOf(scoreWeights[i]);
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.MOLECULES_IN_MEMORY, atomContainerArray);
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaksString);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalInMemoryDatabase");
		settings.set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, "de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		settings.set(VariableNames.METFRAG_SCORE_TYPES_NAME, scoreNames);
		settings.set(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, scoreWeightsObject);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, mzppm);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzabs);
		settings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, posCharge);
		settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, mode);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, exactMass);
		settings.set(VariableNames.MAXIMUM_TREE_DEPTH_NAME, (byte)treeDepth);

		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);

		try {
			mp.retrieveCompounds();
		} catch (Exception e2) {
			System.err.println("Error retrieving candidates");
			e2.printStackTrace();
			return new IAtomContainer[0];
		}
		
		try {
			mp.run();
		} catch (Exception e) {
			System.err.println("Error running MetFrag process");
			e.printStackTrace();
			return new IAtomContainer[0];
		}
	
		SortedScoredCandidateList scoredCandidateList = (SortedScoredCandidateList) mp.getCandidateList();
		resultMols = new IAtomContainer[scoredCandidateList.getNumberElements()];
		int numberOfPeaksUsed = scoredCandidateList.getNumberPeaksUsed();
		
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			ICandidate candidate = scoredCandidateList.getElement(i);
			IAtomContainer tmp = null;
			try {
				tmp = candidate.getAtomContainer();
				MoleculeFunctions.prepareAtomContainer(tmp, false);
			} catch (Exception e1) {
				e1.printStackTrace();
				continue;
			}
			
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_1_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_1_NAME, candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_2_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_2_NAME, candidate.getProperty(VariableNames.INCHI_KEY_2_NAME));
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_NAME) != null)
				tmp.setProperty(VariableNames.INCHI_KEY_NAME, candidate.getProperty(VariableNames.INCHI_KEY_NAME));
			
			tmp.setProperty(VariableNames.IDENTIFIER_NAME, candidate.getIdentifier());
			IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(tmp);
			Double massDoubleOrig = null;
			try {
				massDoubleOrig = candidate.getMolecularFormula().getMonoisotopicMass();
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
			massDoubleOrig = (double)Math.round((massDoubleOrig)*10000)/10000;
			tmp.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, massDoubleOrig);
			tmp.setProperty(VariableNames.FINAL_SCORE_COLUMN_NAME, candidate.getProperty(VariableNames.FINAL_SCORE_COLUMN_NAME));
			for(int ii = 0; ii < scoreNames.length; ii++) {
				String scoreClassName = scoreNames[ii];
				tmp.setProperty(scoreClassName, candidate.getProperty(scoreClassName));
				tmp.setProperty(scoreClassName + "_Values", candidate.getProperty(scoreClassName + "_Values"));
			}
			if(candidate.getMatchList() != null) tmp.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, candidate.getMatchList().getNumberElements());
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			if(candidate.getMatchList() != null) {
				for(int ii = 0; ii < candidate.getMatchList().getNumberElements(); ii++) {
					try {
						peaksExplained += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + candidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity() + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						e1.printStackTrace();
					}
					sumFormulasOfFragmentsExplainedPeaks += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
							+ ":" + candidate.getMatchList().getElement(ii).getBestMatchedFragment().getMolecularFormula(candidate.getPrecursorMolecule()) + ";";
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				tmp.setProperty(VariableNames.EXPLAINED_PEAKS_COLUMN, peaksExplained);
				tmp.setProperty(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, sumFormulasOfFragmentsExplainedPeaks);
			}
			
			tmp.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, MolecularFormulaManipulator.getString(molFormula));
			tmp.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, numberOfPeaksUsed);
			resultMols[i] = tmp;
		}
		
		return resultMols;

	}
	
	public static IAtomContainer[] scoreMoleculesAgainstSpectrum(String databaseName, double[] _masses, 
			double[] _intensities, double _exactMass, int _numberThreads, double _mzabs, double _mzppm, boolean _posCharge, 
			int _mode, int _treeDepth, String[] scoreNames, Double[] scoreWeights, Double databaseRelativeMassDeviation, String molecularFormula,
			String[] databaseIdentifiers) {

		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		
		double mzabs = _mzabs;
		double mzppm = _mzppm;
		double exactMass = _exactMass;
		int treeDepth = _treeDepth;
		int mode = _mode;
		int numberThreads = _numberThreads;
		boolean posCharge = _posCharge;
		double[] masses = _masses;
		double[] intensities = _intensities;
		
		IAtomContainer[] resultMols = new IAtomContainer[0];
		if(masses == null) return resultMols;
		if(intensities == null) return resultMols;
		if(masses.length != intensities.length) return resultMols;
		if(exactMass <= 0.0) return resultMols;
		if(numberThreads < -1 || numberThreads > 8) return resultMols;
		if(mzabs < 0.0 || mzppm < 0.0) return resultMols;
		if(mode != -1 && mode != 0 && mode != 1) return resultMols;
		if(treeDepth < 1 || treeDepth > 5) return resultMols;

		String peaksString = "";
		if(masses.length > 0) peaksString += masses[0] + " " + intensities[0];
		for(int i = 1; i < masses.length; i++) {
			peaksString += "\n" + masses[i] + " " + intensities[i];
		}
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaksString);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, databaseName);
		if(databaseRelativeMassDeviation != null) settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, databaseRelativeMassDeviation);
		if(molecularFormula != null) settings.set(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME, molecularFormula);
		if(databaseIdentifiers != null) settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, databaseIdentifiers);
		settings.set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, "de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		settings.set(VariableNames.METFRAG_SCORE_TYPES_NAME, scoreNames);
		settings.set(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, scoreWeights);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, mzppm);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzabs);
		settings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, posCharge);
		settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, mode);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, exactMass);
		settings.set(VariableNames.MAXIMUM_TREE_DEPTH_NAME, (byte)treeDepth);
		settings.set(VariableNames.RESET_PRECURSOR_AFTER_PROCESSING, false);

		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);

		try {
			mp.retrieveCompounds();
		} catch (Exception e2) {
			System.err.println("Error retrieving candidates");
			e2.printStackTrace();
		}
		
		try {
			mp.run();
		} catch (Exception e) {
			System.err.println("Error running MetFrag process");
			e.printStackTrace();
		}
	
		SortedScoredCandidateList scoredCandidateList = (SortedScoredCandidateList) mp.getCandidateList();
		resultMols = new IAtomContainer[scoredCandidateList.getNumberElements()];
		int numberOfPeaksUsed = scoredCandidateList.getNumberPeaksUsed();
		
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			ICandidate candidate = scoredCandidateList.getElement(i);
			IAtomContainer tmp = null;
			try {
				tmp = candidate.getAtomContainer();
				MoleculeFunctions.prepareAtomContainer(tmp, false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			tmp.setProperty(VariableNames.IDENTIFIER_NAME, candidate.getIdentifier());
			IMolecularFormula molFormula = MolecularFormulaManipulator.getMolecularFormula(tmp);
			Double massDoubleOrig = null;
			try {
				massDoubleOrig = candidate.getMolecularFormula().getMonoisotopicMass();
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
			massDoubleOrig = (double)Math.round((massDoubleOrig)*10000)/10000;
			tmp.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, massDoubleOrig);
			tmp.setProperty(VariableNames.FINAL_SCORE_COLUMN_NAME, candidate.getProperty(VariableNames.FINAL_SCORE_COLUMN_NAME));
			for(int ii = 0; ii < scoreNames.length; ii++) {
				String scoreClassName = scoreNames[ii];
				tmp.setProperty(scoreClassName, candidate.getProperty(scoreClassName));
				tmp.setProperty(scoreClassName + "_Values", candidate.getProperty(scoreClassName + "_Values"));
			}
			if(candidate.getMatchList() != null) tmp.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, candidate.getMatchList().getNumberElements());
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			if(candidate.getMatchList() != null) {
				for(int ii = 0; ii < candidate.getMatchList().getNumberElements(); ii++) {
					try {
						peaksExplained += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + candidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity() + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						e1.printStackTrace();
					}
					sumFormulasOfFragmentsExplainedPeaks += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
							+ ":" + candidate.getMatchList().getElement(ii).getBestMatchedFragment().getMolecularFormula(candidate.getPrecursorMolecule()) + ";";
				}
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				tmp.setProperty(VariableNames.EXPLAINED_PEAKS_COLUMN, peaksExplained);
				tmp.setProperty(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, sumFormulasOfFragmentsExplainedPeaks);
			}
			
			tmp.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, MolecularFormulaManipulator.getString(molFormula));
			tmp.setProperty(VariableNames.NUMBER_PEAKS_USED_COLUMN, numberOfPeaksUsed);
			resultMols[i] = tmp;
		}
		
		return resultMols;

	}
	
	/**
	 * 
	 * @param molecule
	 * @param maximumTreeDepth
	 * @return
	 */
	public static IAtomContainer[] generateAllFragments(IAtomContainer molecule, int maximumTreeDepth) {

		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		MoleculeFunctions.prepareAtomContainer(molecule, true);
		ICandidate candidate = null;
		try {
			candidate = new TopDownPrecursorCandidate(MoleculeFunctions.getInChIInfoFromAtomContainer(molecule)[0], "1");
			candidate.initialisePrecursorCandidate();
		} catch (AtomTypeNotKnownFromInputListException e1) {
			e1.printStackTrace();
			return new IAtomContainer[0];
		} catch (Exception e1) {
			e1.printStackTrace();
			return new IAtomContainer[0];
		}
		settings.set(VariableNames.CANDIDATE_NAME, candidate);
		settings.set(VariableNames.MAXIMUM_TREE_DEPTH_NAME, (byte)2);
		settings.set(VariableNames.MINIMUM_FRAGMENT_MASS_LIMIT_NAME, 0.0);
		settings.set(VariableNames.MAXIMUM_NUMBER_OF_TOPDOWN_FRAGMENT_ADDED_TO_QUEUE, (byte)maximumTreeDepth);
		TopDownNeutralLossFragmenter fragmenter = null;
		try {
			fragmenter = new TopDownNeutralLossFragmenter(settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FragmentList fragmentList = fragmenter.generateFragments();
		IAtomContainer[] fragments = new IAtomContainer[fragmentList.getNumberElements()];
		for(int i = 0; i < fragmentList.getNumberElements(); i++) {
			fragments[i] = fragmentList.getElement(i).getStructureAsIAtomContainer(candidate.getPrecursorMolecule());
		}
		return fragments;
	}	
	
	/**
	 * 
	 * @param molecule
	 * @param masses
	 * @param exactMass
	 * @param mzabs
	 * @param mzppm
	 * @param posCharge
	 * @param mode
	 * @param treeDepth
	 * @return
	 */
	public static IAtomContainer[] generateMatchingFragments(IAtomContainer molecule, double[] masses, double exactMass, 
			double mzabs, double mzppm, boolean posCharge, int mode, int treeDepth) {

		Configurator.setLevel("net.sf.jnati.deploy.artefact.ConfigManager", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.ClasspathRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.repository.LocalRepository", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.artefact.ManifestReader", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeArtefactLocator", Level.ERROR);
		Configurator.setLevel("net.sf.jnati.deploy.NativeLibraryLoader", Level.ERROR);
		
		MoleculeFunctions.prepareAtomContainer(molecule, false);
		
		IAtomContainer[] moleculeAsArray = {molecule};

		String peaksString = "";
		if(masses.length > 0) peaksString += masses[0] + " 100";
		for(int i = 1; i < masses.length; i++) {
			peaksString += "\n" + masses[i] + " 100";
		}
		
		String[] score_names = {VariableNames.METFRAG_FRAGMENTER_SCORE_NAME};
		Double[] score_weights = {1.0};
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.MOLECULES_IN_MEMORY, moleculeAsArray);
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, peaksString);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalInMemoryDatabase");
		settings.set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, "de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		settings.set(VariableNames.METFRAG_SCORE_TYPES_NAME, score_names);
		settings.set(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, score_weights);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, mzppm);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzabs);
		settings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, posCharge);
		settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, mode);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, exactMass);
		settings.set(VariableNames.MAXIMUM_TREE_DEPTH_NAME, (byte)treeDepth);
		settings.set(VariableNames.RESET_PRECURSOR_AFTER_PROCESSING, false);

		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);

		try {
			mp.retrieveCompounds();
		} catch (Exception e2) {
			System.err.println("Error retrieving candidates");
		}
		
		try {
			mp.run();
		} catch (Exception e) {
			System.err.println("Error running MetFrag process");
		}
		
		SortedScoredCandidateList scoredCandidateList = (SortedScoredCandidateList) mp.getCandidateList();
		
		MatchList assignedFragmentList = scoredCandidateList.getElement(0).getMatchList();
		IAtomContainer[] assignedFragments = new IAtomContainer[assignedFragmentList.getNumberElements()];
		
		for(int i = 0; i < assignedFragmentList.getNumberElements(); i++) {
			IAtomContainer currentFragment = assignedFragmentList.getElement(i).getBestMatchedFragment().getStructureAsIAtomContainer(scoredCandidateList.getElement(0).getPrecursorMolecule());
			currentFragment.setProperty("AssignedMassPeak", assignedFragmentList.getElement(i).getMatchedPeak().getMass());
			currentFragment.setProperty("FragmentMass", assignedFragmentList.getElement(i).getBestMatchedFragment().getMonoisotopicMass(scoredCandidateList.getElement(0).getPrecursorMolecule()));
			assignedFragments[i] = currentFragment;
		}
		
		return assignedFragments;
	}
	
	/**
	 * adds properties 
	 * 
	 * EXPLAINED_PEAKS_COLUMN, FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, NUMBER_PEAKS_USED_COLUMN
	 * 
	 * to candidate list
	 * 
	 * @param scoredCandidateList
	 */
	public static void addPropertiesToCandidateList(SortedScoredCandidateList scoredCandidateList) {
		int numberOfPeaksUsed = scoredCandidateList.getNumberPeaksUsed();
		
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			ICandidate candidate = scoredCandidateList.getElement(i);
		
			if(candidate.getMatchList() != null) candidate.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, candidate.getMatchList().getNumberElements());
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			if(candidate.getMatchList() != null) {
				for(int ii = 0; ii < candidate.getMatchList().getNumberElements(); ii++) {
					try {
						peaksExplained += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + candidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity() + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						e1.printStackTrace();
					}
					sumFormulasOfFragmentsExplainedPeaks += candidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
							+ ":" + candidate.getMatchList().getElement(ii).getBestMatchedFragment().getMolecularFormula(candidate.getPrecursorMolecule()) + ";";
				}
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				else peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				else sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				candidate.setProperty(VariableNames.EXPLAINED_PEAKS_COLUMN, peaksExplained);
				candidate.setProperty(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, sumFormulasOfFragmentsExplainedPeaks);
			}
			candidate.setProperty(VariableNames.NUMBER_PEAKS_USED_COLUMN, numberOfPeaksUsed);
		}
	}
}
