Requirements: Installed Java version >= 1.6

To process the downloaded parameters from MetFragWeb you have to perform the following steps:

	1 - Download the commandline tool MetFrag CL from https://msbi.ipb-halle.de/~cruttkie/metfrag/MetFrag2.3.1-CL.jar
		to the download directory (MetFragWeb_Parameters per default)
	2 - Open a terminal and navigate to the download directory (MetFragWeb_Parameters per default)
	3 - Run the commandline tool by typing "java -jar MetFrag2.3.1-CL.jar MetFragWeb_Parameters.cfg"

The result file is stored (as XLS named as MetFragWeb_Sample.xls per default) within the download directory (MetFragWeb_Parameters) and contains the 
processed candidates ranked by the specified scoring terms. You are free to modify the settings in the config file
(MetFragWeb_Parameters.cfg). Find more information at http://c-ruttkies.github.io/MetFrag/projects/metfragcl/.

Note: The hosted MetFragWeb tool on msbi.ipb-halle.de connects to local installed compound databases. This is the case for PubChem, KEGG, ChEBI, LipidMaps,
HMDB and KEGG (derivatized). In the exported parameter file PubChem and KEGG are configured as an online query. The other databases are not provided as an online
service and cannot be run locally unless the relating databases are provided locally. If you have questions on running local compound databases for MetFrag just
contact the developer.

Contact: Christoph Ruttkies, cruttkie@ipb-halle.de