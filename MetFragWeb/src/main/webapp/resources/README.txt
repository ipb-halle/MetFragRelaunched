Requirements: Installed Java version >= 1.6

To process the downloaded parameters from MetFragWeb you have to perform the following steps:

	1 - Download the commandline tool MetFrag2.2 CL from https://msbi.ipb-halle.de/~cruttkie/metfrag/MetFrag2.2-CL.jar 
		to the download directory (MetFragWeb_Parameters per default)
	2 - Open a terminal and navigate to the download directory (MetFragWeb_Parameters per default)
	3 - Run the commandline tool by typing "java -jar MetFrag2.2-CL.jar MetFragWeb_Parameters.cfg"

The result file is stored (as XLS named as MetFragWeb_Sample.xls per default) within the download directory (MetFragWeb_Parameters) and contains the 
processed candidates ranked by the specified scoring terms. You are free to modify the settings in the config file
(MetFragWeb_Parameters.cfg). Find more information at https://c-ruttkies.github.io/MetFrag/projects/metfrag22cl/.

Contact: Christoph Ruttkies, cruttkie@ipb-halle.de