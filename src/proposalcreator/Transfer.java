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
public class Transfer implements Serializable{
    List<TransferLeg> Legs = new ArrayList<TransferLeg>();
    int size=Legs.size();
    
    Transfer(){
        
    }
    
    public int getSize(){
        return size;
    }
    
}
