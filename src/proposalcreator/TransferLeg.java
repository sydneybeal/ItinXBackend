/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proposalcreator;

import java.io.Serializable;

/**
 *
 * @author sydneybeal
 */
public class TransferLeg implements Serializable{
    String from;
    String to;
    String mode;
    
    TransferLeg(){
    }
    
    public void setFrom(String infrom){
        from=infrom;
    }
    public String getFrom(){
        return from;
    }
    
    public void setTo(String into){
        to=into;
    }
    public String getTo(){
        return to;
    }
    
    public void setType(String inmode){
        mode=inmode;
    }
    public String getType(){
        return mode;
    }
}
