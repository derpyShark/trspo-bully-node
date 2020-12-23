package kpi.trspo.bullynode;


import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("bully")
public class BullyController {

    @Value("${SERVICE_IP}")
    private String serviceIp;

    @Autowired
    private RestTemplate restTemplate;

    private final String listenerURL = "http://10.98.193.121:80";

    private BullyObject bullyObject = new BullyObject();

    @PutMapping("/init")
    public void initializeBully(@RequestBody BullyObject initBullyObject)
    {
        bullyObject = new BullyObject(initBullyObject);
        restTemplate.put(listenerURL,"I was initialised " + bullyObject.toString());
    }

    @GetMapping
    public String getServiceIp(){ return serviceIp;}

    private boolean checkLivingLeaders(){
        restTemplate.put(listenerURL,"Node " + bullyObject.getId() + " is checking LP");
        if(bullyObject.getLeader()){
            restTemplate.put(listenerURL,"Node " + bullyObject.getId() + " is the Leader");
            return true;
        }
        for(Map.Entry<Integer, String> entry : bullyObject.getOtherBullies().entrySet()){
            String URL = "http://" + entry.getValue() + "/bully/";
            try{
                ResponseEntity<Boolean> leader = restTemplate.getForEntity(URL+"leader",Boolean.class);
                restTemplate.put(listenerURL, leader.toString());
                if(leader.getBody()) {
                    restTemplate.put(listenerURL, "Node " + entry.getKey() + " is the Leader");
                    return true;
                }
            }
            catch(Exception e){
                restTemplate.put(listenerURL, "We had a timeout");
            }
        }
        restTemplate.put(listenerURL, "There are no leaders");
        return false;
    }


    @PutMapping("/algo")
    public void startAlgo(){
        if(!checkLivingLeaders()){
            if(!bullyObject.getElection()){
                bullyObject.setElection(true);
                sendElectionMessages();
            }
        }
    }

    @PutMapping("/electionMessage")
    public void takeElectionMessage(@RequestBody boolean elected) {
        if(!bullyObject.getElection() && !elected){
            bullyObject.setElection(true);
            sendElectionMessages();
        }

    }

    private void sendElectionMessages(){
        boolean gotResponse = false;
        for(Map.Entry<Integer, String> entry : bullyObject.getOtherBullies().entrySet())
        {
            String URL = "http://" + entry.getValue() + "/bully/";
            if(entry.getKey() > bullyObject.getId())
            {
                try{
                    restTemplate.put(listenerURL, bullyObject.getId() + " sends election to " + entry.getKey());
                    restTemplate.put(URL+"electionMessage", bullyObject.getLeader());
                    restTemplate.put(listenerURL,"we got to the break");
                    gotResponse = true;
                    break;
                }
                catch(Exception e){
                    restTemplate.put(listenerURL, "We had a timeout sending EM");
                }
            }
        }
        if(!gotResponse){
            bullyObject.setLeader(true);
            bullyObject.setElection(false);
            calmEveryoneDown();
            restTemplate.put(listenerURL, bullyObject.getId() + " was elected as the new leader");
        }
    }

    private void calmEveryoneDown(){
        for(Map.Entry<Integer, String> entry : bullyObject.getOtherBullies().entrySet())
        {
            String URL = "http://" + entry.getValue() + "/bully/";
            try{
                restTemplate.put(listenerURL, bullyObject.getId() + " calms down " + entry.getKey());
                restTemplate.delete(URL+"calm");
            }
            catch(Exception e){
                restTemplate.put(listenerURL, "We had a timeout calming down");
            }
        }
    }

    @DeleteMapping("/calm")
    public void calmDown(){
        bullyObject.setElection(false);
    }

    @GetMapping("/leader")
    public boolean checkLeader(){
        return bullyObject.getLeader();
    }

    @GetMapping("/leaders")
    public boolean checkLeaders(){
        return checkLivingLeaders();
    }
}
