/*
 * ProposalCreator V 0.1
 */
package proposalcreator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Baron Alloway, Sydney Beal, December 2017
 *
 */
public class ProposalCreator {

    static Database db = new Database();

    public static String inputStreamAsString(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        br.close();
        return sb.toString();
    }
    
    public static Itinerary convertJSONtoItinerary(JSONObject itinerary) throws JSONException{
        
        //get trip header info from JSONObject
        
        JSONObject header = itinerary.getJSONObject("header");
        String tripName = header.getString("tripName");
        String where = header.getString("destination");
        String when = header.getString("date");
        String agent = header.getString("agent");
        String agency = header.getString("agency");
        String firstAirport = header.getString("firstAirport");
        
        //initiate new itin with header info
        Itinerary itin = new Itinerary(tripName, where, when, agent, agency, firstAirport);
        
        //get itinerary as JSONObject
        JSONObject itineraryInfo = itinerary.getJSONObject("itineraryInfo");
        
        // set up initial transfer obj to first property from firstAirport
        Transfer transferIN = new Transfer();
        //get JSONArray of transferIN
        JSONObject transferINseq = itineraryInfo.getJSONObject("transferIN");
        JSONArray transferINarray = transferINseq.getJSONArray("legs");
        
        // iterate through transferLegs of transferIN
        for(int i=0; i<transferINarray.length(); i++){
            TransferLeg leg = new TransferLeg();
            leg.setFrom(transferINarray.getJSONObject(i).getString("from"));
            leg.setTo(transferINarray.getJSONObject(i).getString("to"));
            leg.setMode(transferINarray.getJSONObject(i).getString("mode"));
            transferIN.Legs.add(leg);
        }
        //set transferIN of itinerary as transferIN built by json object
        itin.transferIN = transferIN;
        
        
        //get JSONArray of trip segments
        
        JSONArray segments = itineraryInfo.getJSONArray("segments");
        
        //iterate through segments
        for(int i=0; i<segments.length(); i++){
            
            Segment tripSegment = new Segment();
            JSONArray dayList = segments.getJSONObject(i).getJSONArray("dayList");
            
            //iterate through list of days and add to trip segment
            for(int j=0; j<dayList.length(); j++){
               
                Day day = new Day();

                day.setPrivate(dayList.getJSONObject(j).getBoolean("isPrivate"));
                day.setDayHeader(dayList.getJSONObject(j).getString("dayHeader"));
                day.setHeader(dayList.getJSONObject(j).getString("header"));
                day.setBody(dayList.getJSONObject(j).getString("body"));
                day.setProp(dayList.getJSONObject(j).getString("prop"));
                day.setRoom(dayList.getJSONObject(j).getString("room"));
                day.setWeb(dayList.getJSONObject(j).getString("web"));
                day.setMeals(dayList.getJSONObject(j).getString("meals"));

                tripSegment.DayList.add(day);
                
            }
            
            //set nights and room type for segment
            tripSegment.setNights(segments.getJSONObject(i).getInt("nights"));
            tripSegment.setRoomType(segments.getJSONObject(i).getString("roomtype"));
        
            //set property
            
            Property prop = new Property();
            
            prop.setName(segments.getJSONObject(i).getJSONObject("property").getString("name"));
            prop.setCity(segments.getJSONObject(i).getJSONObject("property").getString("city"));
            prop.setCountry(segments.getJSONObject(i).getJSONObject("property").getString("country"));
            prop.setInclusions(segments.getJSONObject(i).getJSONObject("property").getString("inclusions"));
            prop.setNotes(segments.getJSONObject(i).getJSONObject("property").getString("notes"));
            prop.setType(segments.getJSONObject(i).getJSONObject("property").getString("type"));
            prop.setWebpage(segments.getJSONObject(i).getJSONObject("property").getString("webpage"));
            prop.setAirstrip(segments.getJSONObject(i).getJSONObject("property").getString("airstrip"));
       
            tripSegment.setProperty(prop);
            
            //add segment to segment list of itinerary
            
            itin.SegmentList.add(tripSegment);
        }
        
        // set up initial transfer obj to first property from firstAirport
        Transfer transferOUT = new Transfer();
        //get JSONArray of transferOUT
        JSONObject transferOUTseq = itineraryInfo.getJSONObject("transferOUT");
        JSONArray transferOUTarray = transferOUTseq.getJSONArray("legs");
        
        // iterate through transferLegs of transferOUT
        for(int i=0; i<transferOUTarray.length(); i++){
            TransferLeg leg = new TransferLeg();
            leg.setFrom(transferOUTarray.getJSONObject(i).getString("from"));
            leg.setTo(transferOUTarray.getJSONObject(i).getString("to"));
            leg.setMode(transferOUTarray.getJSONObject(i).getString("mode"));
            transferOUT.Legs.add(leg);
        }
        
        //set transferOUT of itinerary as transferOUT built by json object
        itin.transferOUT = transferOUT;
        
        return itin;
    }
    
    static String getString(String s, UserInterface ui) {
        String r;
        while (true) {
            r = ui.getInfo("Enter " + s + ":");
            if (r == null) {
                break;
            }
            if (r.length() == 0) {
                ui.sendMessage("Blank is not allowed.");
            }
            break;
        }
        return r;
    }

    static PrintWriter openOut(File file) {
        try {
            return new PrintWriter(file);
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }

    static String stripName(String s) {
        /* removes all spaces and sends all letters to lower case
        then removes terms that may or may not be included in property search */
        String r = s;
        String[] remove = {"\\s", "camp", "lodge", "hotel", "the", "&", "and",
            "boutique"};
        //sending all letters to lower case
        r = r.toLowerCase();
        //System.out.println(r);
        // removing spaces and instances of common extraneous words
        for (int i = 0; i < remove.length; i++) {
            r = r.replaceAll(remove[i], "");
        }
        //System.out.println(r);

        return r;
    }

    static Property searchProperty(String propertySearchTerm, List<Property> PropertyList) {
        Property property;
        int index = PropertyExists(propertySearchTerm, PropertyList);

        if (index != -1) {
            property = PropertyList.get(index);
        } else {
            return null;
        }

        return property;
    }

    static String getMeals(int dWritten, Property currProperty, String prevInclOrig, String travelMode, int dSeg) {

        String meals = "N/A";
        String currIncl = currProperty.getInclusions().toLowerCase();
        String prevIncl = prevInclOrig.toLowerCase();

        // if day 1 (i==0) and property includes all meals, only get dinner
        if (dWritten == 0) {
            if (currIncl.contains("all meals") || currIncl.contains("dinner")) {
                meals = "D";
            }
        } // else if day in middle of itinerary
        else {
            // full board
            if (currIncl.contains("all meals")) {
                meals = "B/L/D";
                if(dSeg==0 && (!prevIncl.contains("all meals") && !prevIncl.contains("breakfast")))
                    meals = meals.replace("B/", "");
            } // breakfast only
            else if ((currIncl.contains("breakfast") && !(currIncl.contains("dinner")))) {
                meals = "B";
                if(dSeg==0 && (!prevIncl.contains("all meals") && !prevIncl.contains("breakfast")))
                    meals = meals.replace("B", "N/A");
            } // half board (breakfast and dinner)
            else if (currIncl.contains("breakfast") && currIncl.contains("dinner")) { 
                meals = "B/D";
                if(dSeg==0 && (!prevIncl.contains("all meals") && !prevIncl.contains("breakfast")))
                    meals = meals.replace("B/", "");
            }
        }
        // if day one at new prop (j==0 from printProposal function) and
        // took flight to get there, remove lunch bc in flight
        if (dSeg == 0 && !travelMode.contains("flight")) {
            meals = meals.replace("L/", "");
        }

        return meals;
    }
    
    static String mealsFull(String meals){
        String mealsFull ="N/A";
        boolean b = meals.contains("B");
        boolean l = meals.contains("L");
        boolean d = meals.contains("D");
        
        if(b && !l && !d)
            mealsFull = "BREAKFAST";
        if(!b && l && !d)
            mealsFull = "LUNCH";
        if(!b && !l && d)
            mealsFull = "DINNER";
        if(b && l && !d)
            mealsFull = "BREAKFAST / LUNCH";
        if(!b && l && d)
            mealsFull = "LUNCH / DINNER";
        if(b && !l && d)
            mealsFull = "BREAKFAST / DINNER";
        if(b && l && d)
            mealsFull = "BREAKFAST / LUNCH / DINNER";
        
        
        return mealsFull;
    }

    static String activityCategory(Property p, UserInterface ui, boolean first) {
        String i = "";
        String activityType = "";
        //System.out.println(p.getType());
        if (p.getType().equals("camp")) {
            if (first) {
                activityType = getString("activity category for property"
                        + "(s/shared, p/private, or m/mixed)", ui);
            } else {
                activityType = getString("s/shared or  p/private", ui);
            }
            if (activityType.startsWith("p") || activityType.startsWith("P")) {
                i = "private";
            } else if (activityType.startsWith("s") || activityType.startsWith("S")) {
                i = "shared";
            } else if (activityType.startsWith("m") || activityType.startsWith("M")) {
                i = "mixed";
            }
        }
        return i;
    }

    /*
    static Transfer getTransferSequence(Property fromProperty, Property toProperty) {
        String[] travelModes = {
            "Private road transfer",
            "Commercial flight",
            "Shared light aircraft flight",
            "Private charter flight",
            "Done"};
        Transfer transfer = new Transfer();
        String fromApt = "", toApt = "";
        boolean done = false;
        boolean tripDone = false;
        int numLegs = 0;
        while (!done) {
            TransferLeg currLeg = new TransferLeg();
            String s = ui.selectItem(travelModes, "Choose travel mode:");
            switch (s) {
                case "Private road transfer":
                    currLeg.setType("Private road transfer");
                    transfer.Legs.add(currLeg);
                    done = true;
                    break;
                case "Commercial flight":
                    currLeg.setType("Commercial flight");
                    currLeg.setFrom(ui.getInfo("Enter departure airport city:"));
                    currLeg.setTo(ui.getInfo("Enter arrival airport city:"));
                    transfer.Legs.add(currLeg);
                    break;
                case "Shared light aircraft flight":
                    currLeg.setType("Shared light aircraft flight");
                    // if properties have their known airstrip in DB, use that
                    if (fromProperty != null && fromProperty.getAirstrip() != null) {
                        currLeg.setFrom(fromProperty.getAirstrip());
                    } // if not, ask user
                    else {
                        currLeg.setFrom(ui.getInfo("Enter departure airport/airstrip:"));
                    }
                    // same as above, for arrival airport
                    if (toProperty != null && toProperty.getAirstrip() != null) {
                        currLeg.setTo(toProperty.getAirstrip());
                    } else {
                        currLeg.setTo(ui.getInfo("Enter arrival airport/airstrip:"));
                    }
                    transfer.Legs.add(currLeg);
                    break;
                case "Private charter flight":
                    currLeg.setType("Private charter flight");
                    // if properties have known airstrip in DB, use that
                    if (fromProperty != null && fromProperty.getAirstrip() != null) {
                        currLeg.setFrom(fromProperty.getAirstrip());
                    } // if not, ask user
                    else {
                        currLeg.setFrom(ui.getInfo("Enter departure airport/airstrip:"));
                    }
                    // same as above, for arrival airport
                    if (toProperty != null && toProperty.getAirstrip() != null) {
                        currLeg.setTo(toProperty.getAirstrip());
                    } else {
                        currLeg.setTo(ui.getInfo("Enter arrival airport/airstrip:"));
                    }
                    transfer.Legs.add(currLeg);
                    break;
                case "Done":
                    done = true;
                    // if "done" before adding any legs to transfer,
                    // then this was the last property of the trip
                    if (transfer.Legs.size() == 0) {
                        tripDone = true;
                    }
                    break;
            }

        }
        // if not the last property of the trip, always add a private transfer
        // as the last leg if from landing from flight
        if (!tripDone) {
            // get the most recent transfer mode
            TransferLeg lastLeg = transfer.Legs.get(transfer.Legs.size() - 1);
            //System.out.println(lastLeg.from + " to " + lastLeg.to);
            if (lastLeg != null) {
                String lastLegType = lastLeg.getType();
                //System.out.println(lastLegType);
                if (lastLegType.contains("flight")) {
                    TransferLeg newLast = new TransferLeg();
                    newLast.setType("Private road transfer");
                    transfer.Legs.add(newLast);
                }
            }
        }
        return transfer;
    }

    */
    
    static String getTravelString(Transfer transfer, String agency, Property property) {
        String travelString = "";
        //System.out.println("getting travel mode");
        String fromAirport = "", toAirport = "";
        TransferLeg currLeg, prevLeg = null;
        for (int i = 0; i < transfer.Legs.size(); i++) {
            currLeg = transfer.Legs.get(i);
            fromAirport = currLeg.from;
            toAirport = currLeg.to;
            String mode = currLeg.mode;
            switch (mode) {
                case "Private road transfer":
                    // if followed by another leg of transfer
                    if (i > 0) {

                        if (prevLeg.mode.equals("Commercial flight")) {
                            travelString += "Upon arrival in " + prevLeg.getTo() + ","
                                    + " you will be met by a " + agency + " representative "
                                    + "for your private road transfer to ";
                        } else if (prevLeg.mode.equals("Shared light aircraft flight")) {
                            travelString += "Upon arrival,"
                                    + " you will be transferred to ";
                        } else if (prevLeg.mode.equals("Private charter flight")) {
                            travelString += "Upon arrival,"
                                    + " you will be transferred to ";
                        }

                        if (currLeg.to != null) {
                            travelString += currLeg.to;
                        }
                    }
                    if (i == 0) {
                        travelString += " you will be met by a " + agency + " representative "
                                + "for your private road transfer to ";
                    }

                    break;
                case "Commercial flight":
                    if (i > 0) {
                        if (prevLeg.mode.equals("Private road transfer")) {
                            travelString += currLeg.from + " for your commercial flight to "
                                    + currLeg.to + ". ";
                        }
                        if (prevLeg.mode.equals("Commercial flight")) {
                            travelString += "Upon arrival in " + prevLeg.getTo()
                                    + ", you will board your commercial flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Shared light aircraft flight")) {
                            travelString += "Upon arrival in  " + prevLeg.getTo()
                                    + ", you will board your commercial flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Private charter flight")) {
                            travelString += prevLeg.getTo() + ". Upon arrival in  "
                                    + prevLeg.getTo()
                                    + " you will board your commercial flight to "
                                    + currLeg.to + ". ";
                        }

                    } else if (i == 0) {
                        if (fromAirport.equals("") && toAirport.equals("")) {
                            travelString += "you will be transferred to the airport for your "
                                    + "commercial flight to ____ . ";
                        } else {
                            travelString += "you will be transferred to the " + fromAirport
                                    + " airport for your commercial flight to "
                                    + toAirport + ". ";
                        }
                    }
                    travelString+="<span style=\"color:#0E9BAC\">";
                    travelString+="Commercial flights not included in land costs. ";
                    travelString+="</span>";
                    break;
                case "Shared light aircraft flight":
                    if (i > 0) {
                        if (prevLeg.mode.equals("Private road transfer")) {
                            travelString += currLeg.from + " for your shared light aircraft flight to "
                                    + currLeg.to + ". ";
                        }
                        if (prevLeg.mode.equals("Commercial flight")) {
                            travelString += "Upon arrival in " + prevLeg.getTo()
                                    + ", you will board your shared light aircraft flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Shared light aircraft flight")) {
                            travelString += "Upon arrival in  " + prevLeg.getTo()
                                    + ", you will board your shared light aircraft flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Private charter flight")) {
                            travelString += prevLeg.getTo() + ". Upon arrival in  "
                                    + prevLeg.getTo()
                                    + ", you will board your shared light aircraft flight to "
                                    + currLeg.to + ". ";
                        }
                    } else if (i == 0) {
                        if (fromAirport.length() < 1 && toAirport.length() < 1) {
                            travelString += "you will be transferred to the airstrip for your "
                                    + "shared light aircraft flight to "+ currLeg.to + ". ";
                        } else {
                            if (fromAirport.contains("airstrip")) {
                                travelString += "you will be transferred to the " + fromAirport
                                        + " for your shared light aircraft flight to "
                                        + toAirport + ". ";
                            } else {
                                travelString += "you will be transferred to the " + fromAirport
                                        + " airport for your shared light aircraft flight to "
                                        + toAirport + ". ";
                            }
                        }
                    }
                    break;
                case "Private charter flight":
                    if (i > 0) {
                        if (prevLeg.mode.equals("Private road transfer")) {
                            travelString += currLeg.from + " for your private charter flight to "
                                    + currLeg.to + ". ";
                        }
                        if (prevLeg.mode.equals("Commercial flight")) {
                            travelString += "Upon arrival in " + prevLeg.getTo()
                                    + ", you will board your private charter flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Shared light aircraft flight")) {
                            travelString += "Upon arrival in  " + prevLeg.getTo()
                                    + ". you will board your private charter flight to "
                                    + currLeg.to + ". ";
                        } else if (prevLeg.mode.equals("Private charter flight")) {
                            travelString += prevLeg.getTo() + ". Upon arrival in  "
                                    + prevLeg.getTo()
                                    + " you will board your private charter flight to "
                                    + currLeg.to + ". ";
                        }

                    } else if (i == 0) {
                        if (fromAirport.equals("") && toAirport.equals("")) {
                            travelString += "you will be transferred to the airport for your "
                                    + "private charter flight to ___.";
                        } else {
                            travelString += "you will be transferred to the " + fromAirport
                                    + " airport for your private charter flight to "
                                    + toAirport;
                        }
                    }
                    break;
                case "Done":

                    break;
            }
            prevLeg = currLeg;
        }
        return travelString;

    }

    static String getDayString(boolean isPrivate, Property property) {
        String s = "";
        String type = property.getType();
        switch (type) {
            case "camp":
                if (isPrivate) {
                    s = "On safari! Enjoy morning and afternoon private game activities. ";
                } else {
                    s = "On safari! Enjoy morning and afternoon shared game activities. ";
                }
                String campGrammar = "camp";
                if (property.getName().contains("Lodge")) {
                    campGrammar = "the lodge";
                }
                s += "All meals will be served at " + campGrammar + ".";
                break;
            case "hotel":
                s = "Enjoy your day at leisure to explore the area.";
                break;
            case "resort":
                s = "Enjoy your day at leisure to participate in the activities on offer.";
                break;
        }

        return s;
    }

    static int PropertyExists(String propname, List<Property> properties) {
        /*This method uses the stripped names of each property to check
        to see if it exists in the databse. It returns -1 if its
        not found*/
        String currentpropname = "";
        propname = stripName(propname);

        for (int i = 0; i < properties.size(); i++) {
            currentpropname = properties.get(i).getName();
            currentpropname = stripName(currentpropname);
            if (currentpropname.equals(propname)) {
                return i;
            } else {
            }
        }

        return -1;
    }

    static String[] populateNameList(List<Integer> indexes, List<Property> properties) {
        String[] results = new String[indexes.size() + 1];
        for (int i = 0; i <= indexes.size(); i++) {
            if (i == indexes.size()) {
                results[i] = "New Property";
                break;
            }

            results[i] = properties.get(indexes.get(i)).getName();

        }

        return results;
    }

    static Property buildNewProperty(UserInterface ui) throws IOException {
        String propName = ui.getInfo("New Property Name");
        String propCity = ui.getInfo("Property City");
        String propCountry = ui.getInfo("Property Country");
        String propInclusions = ui.getInfo("Property Inclusions");
        String propNotes = ui.getInfo("Property Notes");
        String propType = ui.getInfo("Property Type");
        String propWebpage = ui.getInfo("Webpage");
        String propAirstrip = ui.getInfo("Airstrip (if applicable)");
        //create a new property and add it to the db
        Property newProperty = new Property(propName, propCity, propCountry,
                propInclusions, propNotes, propType, propWebpage, propAirstrip);
        db.addNewProperty(newProperty);
        return newProperty;
    }
    
    static void saveItinerary(Itinerary Proposal) throws IOException{
                String name = Proposal.name;
                FileOutputStream fout = new FileOutputStream("./Itins/"+ name +".itin");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(Proposal);
                oos.close();
                fout.close();
    }

    static void printProposalEmailHTML(Itinerary proposal) throws IOException {

        Itinerary proposal2 = fillProposalStrings(proposal);
        int numNights = proposal2.getNumNights();
        int daysWritten = 0;
        int n;
        String title = "";
        String definitions = "B = Breakfast / L = Lunch / D = Dinner\n";

        // setup trip file and stream
        String tripName = proposal2.name;
        if(tripName == null){
            System.exit(0);
        }
        File outFile = new File("./Proposals/ "+tripName + " Arial.html");
        PrintWriter out = openOut(outFile);

// set head of html
        out.println("<!DOCTYPE html> <html> <head>");
        out.println("<meta charset=\"utf-8\">");
        out.println("<title>" + proposal.name + "</title>");
        out.println("<style>");
        out.println("@font-face{ font-family:Arial,sans-serif;");
        //out.println("src: url(\"BrandonGrotesque.otf\")");
        out.println("format(\"opentype\");}");

        //set body of html
        out.println("body { font-family: Arial,sans-serif; }");
        out.println("</style></head>");
        out.println("<body style=\"font-size: 11; color:#47443e;\">");
        out.println("<p style=\"text-align:justify\">");

        // set and print title
        title += proposal2.where + " " + proposal2.when + ":";
        out.println("<b><u>" + title + "</u>");
        out.println("<br>" + definitions + "</b><br><br>");

        List<Segment> segments = proposal2.SegmentList;
        Segment currSegment = null;

        for (int i = 0; i < segments.size(); i++) {
            //System.out.println("I: " + i);
            if (i == segments.size() - 1) {
                n = segments.get(i).getNights() + 1;
            } else {
                n = segments.get(i).getNights();
            }
            for (int j = 0; j < n; j++, daysWritten++) {
                //System.out.println("J: " + j
                out.print("<b>");
                if(segments.get(i).DayList.get(j).dayHeader!=null)
                    out.print(segments.get(i).DayList.get(j).dayHeader);
                out.print(" / ");
                if(segments.get(i).DayList.get(j).header!=null)
                    out.print(segments.get(i).DayList.get(j).header);
                out.print("</b><br>");
                out.print(segments.get(i).DayList.get(j).body);
                out.print("<br>");
                out.print("<i><b>");
                if (j == 0) {
                    //Print bold blue and hyperlink if there is one\
                    //see below for previous hyperlink usage
                    out.print("<span style=\"color:#0E9BAC\">");
                    if (!segments.get(i).DayList.get(j).web.equals("")) {
                        out.print("<u><a href = \"");
                        out.print(segments.get(i).DayList.get(j).web + "\">");
                        out.print("<span style=\"color:#0E9BAC\">");
                        out.print(segments.get(i).DayList.get(j).prop);
                        out.print("</span></a></u>");
                    } else {
                        if(segments.get(i).DayList.get(j).prop!=null)
                            out.print(segments.get(i).DayList.get(j).prop);
                    }
                    out.print("</u></span>");
                    out.print("</b>");
                    if (segments.get(i).DayList.get(j).room != null
                            && !segments.get(i).DayList.get(j).room.equals("")) {
                        out.print(segments.get(i).DayList.get(j).room);
                    }
                    out.print("<br>");
                } else {
                    if(segments.get(i).DayList.get(j).prop!=null)
                        out.print(segments.get(i).DayList.get(j).prop);
                    out.print("</b>");
                    out.print("<br>");
                }
                out.print("Meals: " );
                out.print(segments.get(i).DayList.get(j).meals);
                out.print("</i><br><br>");
            }
           
        }
        out.print("</p></body></html>");
        out.close();
    }

    static void printProposalWordHTML(Itinerary proposal) throws IOException {

        Itinerary proposal2 = fillProposalStrings(proposal);
        int numNights = proposal2.getNumNights();
        int daysWritten = 0;
        int n;
        String title = "Proposed Itinerary";

        // setup trip file and stream
        String tripName = proposal2.name;
        if(tripName == null){
            System.exit(0);
        }
        File outFile = new File("./Proposals/ "+tripName + " Word.html");
        PrintWriter out = openOut(outFile);

// set head of html
        out.println("<!DOCTYPE html> <html> <head>");
        out.println("<meta charset=\"utf-8\">");
        out.println("<title>" + proposal.name + "</title>");
        out.println("<style>");
        out.println("@font-face{ font-family: \"Brandon Grotesque\",sans-serif;");
        out.println("src: url(\"BrandonGrotesque.otf\");}");
        out.println("@font-face{ font-family: \"Freeland\",sans-serif;");
        out.println("src: url(\"Freeland.otf\")");
        out.println("format(\"opentype\");}");

        //set body of html
        out.println("body { font-family: \"Brandon Grotesque\"; }");
        out.println("</style></head>");
        out.println("<body style=\"font-size: 15px; color:#47443e; font-family: Brandon Grotesque\">");
        out.println("<p style=\"text-align:center\">");

        // set and print title
        out.println("<b><span style=\"font-size: 30px;font-family: Freeland;\">" + title + "</span></b><br>");
        out.println("DESIGNED FOR:<br>");
        out.println("DESIGNED BY: " + proposal.getAgent() + "<br>");
        out.println("</p>");

        List<Segment> segments = proposal2.SegmentList;
        Segment currSegment = null;

        out.println("<p style=\"text-align:justify\">");
        for (int i = 0; i < segments.size(); i++) {
            //System.out.println("I: " + i);
            if (i == segments.size() - 1) {
                n = segments.get(i).getNights() + 1;
            } else {
                n = segments.get(i).getNights();
            }
            for (int j = 0; j < n; j++, daysWritten++) {
                //System.out.println("J: " + j);
                out.print("<span style=\"color:#0E9BAC\"><b>");
                out.print(segments.get(i).DayList.get(j).dayHeader.toUpperCase());
                out.print("</b></span><br>");
                out.print(segments.get(i).DayList.get(j).header);
                out.print("<br><p>");
                out.print(segments.get(i).DayList.get(j).body);
                out.print("</p>");
                out.print("<b>");
                if (j == 0) {
                    //Print bold blue and hyperlink if there is one\
                    //see below for previous hyperlink usage
                    out.print("<span style=\"color:#0E9BAC\">");
                    if (segments.get(i).DayList.get(j).web!=null) {
                        out.print("<u><a href = \"");
                        out.print(segments.get(i).DayList.get(j).web + "\">");
                        out.print("<span style=\"color:#0E9BAC\">");
                        out.print(segments.get(i).DayList.get(j).prop.toUpperCase());
                        out.print("</span></a></u>");
                    } else {
                        if(segments.get(i).DayList.get(j).prop!=null)
                            out.print(segments.get(i).DayList.get(j).prop.toUpperCase());
                    }
                    out.print("</u></span>");
                    out.print("</b>");
                    if (segments.get(i).DayList.get(j).room != null
                            && !segments.get(i).DayList.get(j).room.equals("")) {
                        out.print(segments.get(i).DayList.get(j).room.toUpperCase());
                    }
                    out.print("<br>");
                } else {
                    if(segments.get(i).DayList.get(j).prop!=null)
                        out.print(segments.get(i).DayList.get(j).prop.toUpperCase());
                    out.print("</b>");
                    out.print("<br>");
                }

                out.print(mealsFull(segments.get(i).DayList.get(j).meals));
                out.print("<br><br>");
            }
           
        }
        out.print("</p></body></html>");
        out.close();
    }
    
    static Itinerary fillProposalStrings(Itinerary proposal) throws IOException {

        String travelMode = "you will be met by a " + proposal.agency + " representative for"
                + " your private road transfer to ";
        String prevCity = "", prevCountry = "", prevInclusions="";
        String title = "";

        NumberConverter conv = new NumberConverter();

        int n;
        int daysWritten = 0;
        List<Segment> segments = proposal.SegmentList;
        Segment currSegment = null;
        Day currDay;
        int i = 0, j = 0;
        
        
        for (i = 0; i < segments.size(); i++) {
            currSegment = segments.get(i);

            String inclusions = currSegment.getProperty().getInclusions();
            n = currSegment.getNights();

            for (j = 0; j < n; j++, daysWritten++) {
                String dayHeader="", header="", body = "", prop = "", room = "";
                String web = "", meals = "";
                currDay = currSegment.DayList.get(j);

                dayHeader += "Day " + (daysWritten + 1);

                if (j == 0) {

                    // print from previous city to new city (if different)
                    if (daysWritten == 0) {
                        header += "Arrive ";
                        header += currSegment.property.getCity() + ", "
                                + currSegment.property.getCountry();
                    } else if (!prevCity.equals("") && !(prevCity.equals(currSegment.property.getCity()))) {
                        // if changing countries, include countries in header
                        if (!prevCountry.equals(currSegment.property.getCountry())) {
                            header += prevCity + ", " + prevCountry + " to ";
                            header += currSegment.property.getCity() + ", " + currSegment.property.getCountry();
                        } // if same country, exclude country in header
                        else if (prevCountry.equals(currSegment.property.getCountry())) {
                            header += prevCity + " to ";
                            header += currSegment.property.getCity();
                        }
                    } else if (prevCity.equals(currSegment.property.getCity())) {
                        header += currSegment.property.getCity();
                    }

                    // convert nights entered to string and use proper grammar
                    String nstring = conv.convert(n);
                    String nightgrammar = "nights";
                    if (n == 1) {
                        nightgrammar = "night";
                    }
                    // first sentence

                    if (daysWritten == 0) {
                        body += "Upon arrival in " + proposal.getFirstAirport() + ", ";
                        body += getTravelString(proposal.transferIN,proposal.agency,currSegment.property);
                    } else if (daysWritten > 0) {
                        body += "Today " + travelMode;
                    }

//                        if (commercialFlight) {
//                            out.print(property.getCity() + ". Upon arrival, "
//                                    + "you will be met by a " + agency
//                                    + " representative for your private transfer to ");
//                        }
                    body += currSegment.property.getName() + ", where you will spend"
                            + nstring + " " + nightgrammar + ". ";

                    //if private, replace "shared" with "private" in inclusions
                    if (currDay.isPrivate) {
                        inclusions = inclusions.replace("shared", "private");
                    }
                    body += inclusions + " included in your stay. ";

                    //print extra string under notes in column E
                    if (currSegment.property.getNotes() != null) {
                        body += currSegment.property.getNotes() + " ";
                    }

                    //TODO: if camp/lodge "This afternoon, meet your guide
                    // (and other guests if shared) for a (private or shared) game drive."
                    // if hotel "Enjoy the remainder of your evening at leisure."
                    if (currSegment.property.getType().equals("camp")) {
                        if (currDay.isPrivate) {
                            body += "This afternoon, meet your guide for a private "
                                    + "game activity. ";
                        } else {
                            body += "This afternoon, meet your guide and other guests "
                                    + "for a shared game activity. ";
                        }

                        String campGrammar = "camp";
                        if (currSegment.property.getName().contains("Lodge")) {
                            campGrammar = "the lodge";
                        }
                        body += "Dinner will be served at " + campGrammar + ".";

                    } else {
                        body += "Enjoy the remainder of your evening at leisure.";
                    }

                    prop += currSegment.property.getName();
                    room += " (" + currSegment.roomType + ")";
                    web += currSegment.property.getWebpage();

                } else if (j > 0) {
                    header += currSegment.property.getCity();
                    // if camp "On safari! Enjoy (private or shared) game drives"
                    // if hotel "Enjoy your days at leisure to explore property.getCity()"
                    String dayString = getDayString(currDay.isPrivate, currSegment.property);

                    body += dayString;

                    prop += currSegment.property.getName();
                }

                // meals
                String mealString = getMeals(daysWritten, currSegment.property, prevInclusions, travelMode, j);
                meals += mealString;
                //System.out.println(header);
                proposal.SegmentList.get(i).DayList.get(j).dayHeader = dayHeader;
                proposal.SegmentList.get(i).DayList.get(j).header = header;
                //System.out.println(body);
                proposal.SegmentList.get(i).DayList.get(j).body = body;
                //System.out.println(meals);
                proposal.SegmentList.get(i).DayList.get(j).prop = prop;
                proposal.SegmentList.get(i).DayList.get(j).room = room;
                proposal.SegmentList.get(i).DayList.get(j).web = web;
                proposal.SegmentList.get(i).DayList.get(j).meals = meals;

            }

            // get travel mode to next property
            //System.out.println("Segment recorded.");
            // input transfer sequence of current segment, agency of the booking, and current property
            travelMode = getTravelString(currSegment.getTransferOut(), proposal.agency, currSegment.property);
            if (!travelMode.equals("")) {
                // save city/country status for next itinerary location
                prevCity = currSegment.property.getCity();
                //System.out.println("City: " + prevCity);
                prevCountry = currSegment.property.getCountry();
                //System.out.println("Country: " + prevCountry);
                prevInclusions= currSegment.property.getInclusions();
            }

        }
        if (currSegment != null) {
            // give string indicating it is departure day
            Day newLast = new Day();
            newLast.setPrivate(false);
            newLast.dayHeader = "Day " + (daysWritten + 1);
            newLast.header = "Depart " + prevCountry;
            newLast.body = getTravelString(proposal.transferOUT,proposal.agency,currSegment.property);
            newLast.prop = "In flight";
            newLast.meals = "Meals: B";
            currSegment.DayList.add(newLast);
        }

        return proposal;
    }

    static List<Integer> checkSimilarity(String propname, List<Property> properties) {
        double ratio = 100.0;
        int difference;
        List<Integer> possibleMatches = new ArrayList<Integer>();
        String currname;

        LevenshteinDistance dist = new LevenshteinDistance();
        for (int i = 0; i < properties.size(); i++) {
            ratio = 100.0;
            currname = (properties.get(i).getName());
            difference = dist.apply(propname, currname);
            ratio = ((double) difference) / (Math.max(propname.length(), currname.length()));
            if (ratio < 0.2) {
                possibleMatches.add(i);
            }
        }

        return possibleMatches;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, JSONException {
        
        ServerSocket server;
        Socket client;
        InputStream input;
        JSONObject itinJSON = null;
        
        try {
            server = new ServerSocket(1010);
            client = server.accept();

            input = client.getInputStream();
            String inputString = inputStreamAsString(input);

            itinJSON = new JSONObject(inputString);
            
            System.out.println(inputString);
            

            client.close();
            server.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        //  set up GUI
        UserInterface ui = new GUI();

        // Load properties into arraylist
        List<Property> PropertyList = db.loadProperties();

        Itinerary itin = convertJSONtoItinerary(itinJSON);
        // start taking in properties from user
        if(itin!=null){
            printProposalEmailHTML(itin);
            printProposalWordHTML(itin);
        }
        saveItinerary(itin);

    }

}