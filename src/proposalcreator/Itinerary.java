/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proposalcreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Baron
 */
public class Itinerary implements Serializable{
    String name;
    String where;
    String when;
    String agent;
    String agency;
    String firstAirport;
    int numNights;
    List<Segment> SegmentList = new ArrayList<Segment>();
    Transfer transferIN;
    Transfer transferOUT;
    
    
    Itinerary(String inname, String inwhere, String inwhen, String inagent,
            String inagency, String infirstAirport){
       name = inname;
       where = inwhere;
       when = inwhen;
       agent = inagent;
       agency = inagency;
       firstAirport = infirstAirport;
    }
    
    public int getNumNights(){
        return numNights;
    }
    
    public String getFirstAirport(){
        return firstAirport;
    }
    
    public String getAgent(){
        return agent;
    }
    
    
}
