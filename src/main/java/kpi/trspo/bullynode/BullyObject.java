package kpi.trspo.bullynode;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Data
public class BullyObject {

    private boolean election;
    private boolean leader;
    private int id;
    private Map<Integer, String> otherBullies;

    public boolean getLeader(){
        return leader;
    }

    public boolean getElection(){
        return election;
    }

    public BullyObject(BullyObject old){
        election = old.election;
        leader = old.leader;
        id = old.id;
        otherBullies = new HashMap<>(old.otherBullies);
    }

    public BullyObject(){
        election = false;
        leader = false;
        id = 0;
        otherBullies = new HashMap<>();
    }

}
