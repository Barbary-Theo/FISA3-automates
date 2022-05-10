import model.Reseau;
import model.Station;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

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
        //checkTextFile(new File("/Users/martinthibaut/Desktop/metro.txt"));

    }

    static void checkTextFile(File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line;
            int lineNumber = 1;
            while ((line = bufferedReader.readLine()) != null) {
                if (lineNumber == 1 && !line.startsWith("%")) {
                    String error = "The first line must be begin with a %. (Line " + lineNumber + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                if (line.startsWith("%") && line.contains("stations")) {
                    String temp = bufferedReader.readLine();
                    String[] stations = temp.split(" ");
                    Arrays.stream(stations).map(station -> {
                        Reseau reseau = Reseau.getInstance();
                        return null;
                    });
                }
                lineNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            for (Object horaire : horaires) {
                horaireParsed = (JSONObject) horaire;
                stations = (JSONArray) horaireParsed.get("stations");

                for (Object station : stations) {
                    stationParsed = (JSONObject) station;

                    if (!reseau.verifStationExist(stationParsed.get("station").toString())) {
                        throw new Exception("Station does not exist : " + stationParsed.get("station").toString());
                    }

                }

            }

        } catch (IOException | ParseException e) {
            System.err.println("Error while reading file : " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
