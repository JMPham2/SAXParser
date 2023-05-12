import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.sql.DataSource;

public class SAXStarParser extends DefaultHandler {
    private DataSource dataSource;
    private int currentId;
    private Map<String, Star> myStars;
    private String tempVal;
    //to maintain context
    private Star tempStar;

    public SAXStarParser() {
        try {
            Class.forName(LoginInfo.driver).newInstance();
            Connection connection = DriverManager.getConnection(LoginInfo.loginUrl, LoginInfo.loginUser, LoginInfo.loginPasswd);
            String query = "SELECT MAX(id) from stars";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                currentId = Integer.parseInt(rs.getString(1).replaceAll("[^0-9]", "")) + 1;
            }
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println(String.format("Error: %s", e.getMessage()));
            currentId = 0;
        }
        myStars = new HashMap<String, Star>();
    }
    public int getCurrentId(){
        return currentId;
    }
    public Map<String, Star> run() {
        parseDocument();
        //printData();
        return myStars;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("actors63.xml", this);

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

        System.out.println("No of Stars '" + myStars.size() + "'.");

        for(Entry entry : myStars.entrySet()){
            System.out.println(entry.getValue());
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of star
            tempStar = new Star();
            tempStar.setId(String.format("nm%07d", currentId++));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).replace("'", "");
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if(qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
            myStars.put(tempVal, tempStar);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempStar.setDob(Integer.parseInt(tempVal.replaceAll("[^0-9]", "")));
            } catch (NumberFormatException e) {

            }
        }

    }
}
