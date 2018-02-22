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
 * @author sydneybeal
 */
public class Segment implements Serializable {
    List<Day> DayList = new ArrayList<Day>();
    int nights;
    String roomType;
    Property property;
    Transfer transferOut;

    
    Segment(){

    }
    
    public void setProperty(Property inProp){
        property = inProp;
    }
    public Property getProperty(){
        return property;
    }
    
    public void setRoomType(String inRoomType){
        roomType=inRoomType;
    }
    public String getRoomType(){
        return roomType;
    }
    
    public void setNights(int inNights){
        nights = inNights;
    }
    public int getNights(){
        return nights;
    }
    
    
    public void setTransferOut(Transfer transfer){
        transferOut=transfer;
    }
    
    public Transfer getTransferOut(){
        return transferOut;
    }
    
}
