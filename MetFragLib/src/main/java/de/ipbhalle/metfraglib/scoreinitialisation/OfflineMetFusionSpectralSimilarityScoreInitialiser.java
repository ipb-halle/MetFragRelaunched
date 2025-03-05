package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.MultipleTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;

public class OfflineMetFusionSpectralSimilarityScoreInitialiser implements IScoreInitialiser {
    private Logger logger = Logger.getLogger(OfflineMetFusionSpectralSimilarityScoreInitialiser.class);

    private static volatile SpectralPeakListCollection spectralPeakLists = null;
    private static final Object lock = new Object();

    @Override
    public void initScoreParameters(Settings settings) throws Exception {
        logger.setLevel(Level.toLevel("info"));

        if (settings.containsKey(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) &&
                settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) != null) {
            return;
        }

        if (spectralPeakLists == null) {
            synchronized (lock) {
                if (spectralPeakLists == null) {
                    MultipleTandemMassPeakListReader multiplePeakListReader = new MultipleTandemMassPeakListReader(settings);
                    Path offlineSpectralFilePath = null;

                    if (settings.containsKey(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) &&
                            settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) != null) {
                        offlineSpectralFilePath = Paths.get((String) settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME));
                    } else {
                        logger.info("Load reference data from default resource \"MoNA-export-LC-MS.mb\".");
                        URI uri = Objects.requireNonNull(getClass().getClassLoader().getResource("MoNA-export-LC-MS.mb")).toURI();
                        if ("jar".equals(uri.getScheme())) {
                            try {
                                FileSystems.getFileSystem(uri);
                            } catch (FileSystemNotFoundException e) {
                                FileSystems.newFileSystem(uri, Collections.emptyMap());
                            }
                        }
                        offlineSpectralFilePath = Paths.get(uri);
                    }

                    if (Files.isRegularFile(offlineSpectralFilePath)) {
                        logger.info("Load reference data from file: " + offlineSpectralFilePath);
                        try (InputStream inStream = Files.newInputStream(offlineSpectralFilePath)) {
                            spectralPeakLists = multiplePeakListReader.readMultiple(inStream);
                        }
                    } else if (Files.isDirectory(offlineSpectralFilePath)) {
                        logger.info("Load reference data from directory: " + offlineSpectralFilePath);
                        List<Path> libFiles = Files.walk(offlineSpectralFilePath)
                                .filter(Files::isRegularFile)
                                .filter(p -> p.getFileName().toString().endsWith(".mb"))
                                .collect(Collectors.toList());
                        logger.info("Reference data files found: " + libFiles);
                        spectralPeakLists = multiplePeakListReader.readMultiple(libFiles);
                    }

                    if (spectralPeakLists != null) {
                        //check whether MoNA InChIKeys are given as resource
                        spectralPeakLists.calculateSimilarities((SortedTandemMassPeakList) settings.get(VariableNames.PEAK_LIST_NAME));
                    }
                }
            }
        }
		if (spectralPeakLists != null) {
			settings.set(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME, spectralPeakLists);
		}
    }

    public void postProcessScoreParameters(Settings settings) {
        return;
    }

}
