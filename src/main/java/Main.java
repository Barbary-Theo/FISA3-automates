import model.Reseau;
import model.Station;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(" --------------------- START --------------------- ");

        Reseau reseau = Reseau.getInstance();
        readJSON("src/main/resources/bus.json", reseau);
    }



    public static void readJSON(String filePath, Reseau reseau) throws IOException, ParseException {

        try {
            FileReader fileReader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
            JSONObject horaireParsed;
            JSONObject stationParsed;
            JSONArray stations;

            JSONArray horaires = (JSONArray) jsonObject.get("horaires");

            for(Object horaire : horaires) {
                horaireParsed = (JSONObject) horaire;
                stations = (JSONArray) horaireParsed.get("stations");

                for(Object station : stations) {
                    stationParsed = (JSONObject) station;

                    System.out.println(stationParsed.get("station"));
                }

            }

        }
        catch (IOException | ParseException e) {
            System.err.println("Error while reading file : " + e);
        }

    }

}
