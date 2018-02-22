/*
 *The Property Class - Defined template for each property
 *
 */
package proposalcreator;

import java.io.Serializable;

/**
 *
 * @author Baron
 */
public class Property implements Serializable {
    private String name;
    private String city;
    private String country;
    private String inclusions;
    private String notes;
    private String type;
    private String webpage;
    private String airstrip;
    
   //Constructor for Property 
   Property(String name,String city, String country, String inclusions,
           String notes, String type, String webpage, String airstrip){
       super();
       this.setName(name);
       this.setCity(city);
       this.setCountry(country);
       this.setInclusions(inclusions);
       this.setNotes(notes);
       this.setType(type); 
       this.setWebpage(webpage);
       this.setAirstrip(airstrip);
   }
   
   Property(){
       
   }
   
   
   public void setName(String inputName){
       name = inputName;
   }
    
   public void setCity(String inputCity){
       city = inputCity;
   }
   public void setCountry(String inputCountry){
       country = inputCountry;
   }
   public void setInclusions(String inputInclusions){
       inclusions = inputInclusions;
   }
   public void setNotes(String inputNotes){
       notes = inputNotes;
   }
   public void setType(String inputType){
       type = inputType;
   }
   public void setWebpage(String inputWebpage){
       webpage = inputWebpage;
   }
   public void setAirstrip(String inputAirstrip){
       airstrip = inputAirstrip;
   }
   
   public String getName(){
       return name;
   }
   public String getCity(){
       return city;
   }
   public String getCountry(){
       return country;
   }
   public String getInclusions(){
       return inclusions;
   }
   public String getNotes(){
       return notes;
   }
   public String getType(){
       return type;
   }
   public String getWebpage(){
       return webpage;
   }
   public String getAirstrip(){
       return airstrip;
   }
}
