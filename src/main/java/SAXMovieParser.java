import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.sql.DataSource;

public class SAXMovieParser extends DefaultHandler {
    private DataSource dataSource;
    private int currentId;
    Map<String, Movie> myMovies;

    private String tempVal;

    //to maintain context
    private String tempDirector;
    private Movie tempMovie;
    private List<String> tempGenres;

    public SAXMovieParser() {
        try {
            Class.forName(LoginInfo.driver).newInstance();
            Connection connection = DriverManager.getConnection(LoginInfo.loginUrl, LoginInfo.loginUser, LoginInfo.loginPasswd);
            String query = "SELECT MAX(id) from movies";
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
        myMovies = new HashMap<String, Movie>();
    }

    public int getCurrentId(){
        return currentId;
    }
    public Map<String, Movie> run() {
        parseDocument();
        //printData();
        return myMovies;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", this);

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

        System.out.println("No of Movies '" + myMovies.size() + "'.");

        for(Entry entry : myMovies.entrySet()){
            System.out.println(entry.getValue());
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
            tempMovie.setId(String.format("tt%07d", currentId++));
        } else if(qName.equalsIgnoreCase("cats")){
            tempGenres = new ArrayList<String>();
            tempMovie.setGenresList(tempGenres);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).replace("'", "");
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if(qName.equalsIgnoreCase("dirname")){
            tempDirector = tempVal;
        } else if(qName.equalsIgnoreCase("fid")) {
            myMovies.put(tempVal, tempMovie);
            tempMovie.setDirector(tempDirector);
        } else if(qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal.replaceAll("[^0-9]", "")));
            } catch (NumberFormatException e) {

            }
        } else if (qName.equalsIgnoreCase("cat")) {
            tempGenres.add(tempVal);
        }
    }
}
