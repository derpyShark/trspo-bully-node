package kpi.trspo.bullynode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BullyNodeApplication {

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String initializerURL="http://10.98.193.120:80";
    private final static String listenerURL = "http://10.98.193.121:80";

    @Value("${SERVICE_IP}")
    private String serviceIp;

    private static String stServiceIp;

    @Value("${SERVICE_IP}")
    public void setServiceIpStatic(String serviceIp){
        BullyNodeApplication.stServiceIp = serviceIp;
    }

    private static void sendAddressToInitializer(){
        String workingURL = initializerURL + "/initializer/add";
        restTemplate.put(listenerURL,"A node was born " + workingURL + stServiceIp);
        restTemplate.put(workingURL, stServiceIp);
    }

    public static void main(String[] args) {
        SpringApplication.run(BullyNodeApplication.class, args);
        sendAddressToInitializer();
    }

}
