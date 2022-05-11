import model.Exploitant;
import model.Liaison;
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
import java.util.List;

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
            JSONArray passageParsed;

            JSONArray horaires;
            JSONArray stations;
            JSONArray passages;

            List<Station> allStation = new ArrayList<>();

            horaires = (JSONArray) jsonObject.get("horaires");

            for (Object horaire : horaires) {
                horaireParsed = (JSONObject) horaire;

                stations = (JSONArray) horaireParsed.get("stations");
                passages = (JSONArray) horaireParsed.get("passages");


                for (Object station : stations) {
                    stationParsed = (JSONObject) station;

                    if (!reseau.verifStationExist(stationParsed.get("station").toString())) {
                        throw new Exception("Station does not exist : " + stationParsed.get("station").toString());
                    }
                    else {
                        allStation.add(reseau.findStationByName(stationParsed.get("station").toString()));
                    }

                }

                for (Object passage : passages) {
                    passageParsed = (JSONArray) passage;

                    for (int j = 0; j < passageParsed.size() - 1; j++) {



                        int duree = Integer.parseInt(passageParsed.get(j + 1).toString()) - Integer.parseInt(passageParsed.get(j).toString());
                        reseau.addLiaison(
                                new Liaison(
                                    "A", // name
                                    passageParsed.get(j + 1).toString(),
                                    passageParsed.get(j).toString(),
                                    duree,
                                    new Exploitant("Bus"),
                                    allStation.get(j),
                                    allStation.get(j + 1)
                                )
                        );
                    }
                    System.out.println();

                }

                allStation.clear();
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error while reading file : " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
