package de.ipbhalle.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

public class ExecPod {
	public static void main(String[] args) throws InterruptedException {
        String master = "https://localhost:8443/";
        if (args.length == 1) {
            master = args[0];
        }
        
        Config config = new ConfigBuilder().withMasterUrl(master).build();
        
        
	}
}
