/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proposalcreator;

import java.io.Serializable;

/**
 *
 * @author Baron
 */
public class Day implements Serializable{
    boolean isPrivate;    
    
    //example of header: "Day 2 / Johannesburg, South Africa to Maun, Botswana"
    //example of body: "Today you will be met ... afternoon at leisure."
    //example of footer: "Intercontinental Hotel (Deluxe Room)"
    //example of meals: "B/L/D"
    String dayHeader;
    String header;
    String body;
    String prop;
    String room;
    String web;
    
    String meals;
    
    Day(){
        
    }
    
    public boolean getPrivate(){
        return isPrivate;
    }
    public void setPrivate(boolean in){
        isPrivate = in;
    }
    
    public String getDayHeader(){
        return dayHeader;
    }
    public void setDayHeader(String in){
        dayHeader = in;
    }
    
    public String getHeader(){
        return header;
    }
    public void setHeader(String in){
        header = in;
    }
    
    public String getBody(){
        return body;
    }
    public void setBody(String in){
        body = in;
    }
    
    public String getProp(){
        return prop;
    }
    public void setProp(String in){
        prop = in;
    }
    
    public String getRoom(){
        return room;
    }
    public void setRoom(String in){
        room = in;
    }
    
    public String getWeb(){
        return web;
    }
    public void setWeb(String in){
        web = in;
    }
    
    public String getMeals(){
        return meals;
    }
    public void setMeals(String in){
        meals = in;
    }
    
    
}
