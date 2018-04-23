package de.ipbhalle.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import okhttp3.Response;

public class ExecPod {
	public static void main(String[] args) throws InterruptedException {
        String master = "https://192.168.99.100:8443/";
        if (args.length == 1) {
            master = args[0];
        }
        
        Config config = new ConfigBuilder().withMasterUrl(master).build();
        config.setClientCertFile("/home/chrisr/.minikube/client.crt");
        config.setClientKeyFile("/home/chrisr/.minikube/client.key");
        config.setTrustCerts(true);
        
        try (final KubernetesClient client = new DefaultKubernetesClient(config);
             ExecWatch watch = client.pods().inNamespace("default").withName("hello-minikube-6c47c66d8-chm2c")
                .readingInput(System.in)
                .writingOutput(System.out)
                .writingError(System.err)
                .withTTY()
                .usingListener(new SimpleListener())
                .exec()){

            Thread.sleep(10 * 1000);
        }	
	}
	private static class SimpleListener implements ExecListener {

        @Override
        public void onOpen(Response response) {
            System.out.println("The shell will remain open for 10 seconds.");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            System.err.println("shell barfed");
        }

        @Override
        public void onClose(int code, String reason) {
            System.out.println("The shell will now close.");
        }

}
}
