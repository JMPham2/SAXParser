import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

public class SAXCastParser extends DefaultHandler {

    Map<String, List<String>> myMovieRoles;

    private String tempVal;

    //to maintain context
    private String tempMovieId;
    private List<String> tempActorList;

    public SAXCastParser() {
        myMovieRoles = new HashMap<String, List<String>>();
    }

    public Map<String, List<String>> run() {
        parseDocument();
        //printData();
        return myMovieRoles;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */

    private void printData() {

        System.out.println("No of Movies '" + myMovieRoles.size() + "'.");

        for(Entry entry : myMovieRoles.entrySet()){
            System.out.println(entry.getValue());
        }
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if(qName.equalsIgnoreCase("filmc")){
            tempActorList = new ArrayList<String>();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).replace("'", "");
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("filmc")) {
            if(!tempActorList.isEmpty())
                myMovieRoles.put(tempMovieId, tempActorList);
        } else if(qName.equalsIgnoreCase("f")) {
            tempMovieId = tempVal;
        } else if(qName.equalsIgnoreCase("a")) {
            tempActorList.add(tempVal);
        }
    }
}
