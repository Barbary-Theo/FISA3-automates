import model.Exploitant;
import model.Liaison;
import model.Reseau;
import model.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();
        //readJSON("src/main/resources/bus.json", reseau);
        readMetroTEXT(new File("src/main/resources/metro.txt"));
        //System.out.println(setStartTime("test", 15, new Exploitant("metro"), new Station("depart", new ArrayList<>()), new Station("arrive", new ArrayList<>())));
    }

    static void readMetroTEXT(File file) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get("src/main/resources/metro.txt"));

            // Récupération des données dans le fichier texte
            int timeBreakStation = Integer.parseInt(allLines.get(allLines.size() - 5).substring(11, 12));
            int timeStepHappyHour = Integer.parseInt(allLines.get(allLines.size() - 3).substring(28, 30));
            int timeStepNoHappyHour = Integer.parseInt(allLines.get(allLines.size() - 2).substring(28, 30));

            // Récupération des horaires du matin
            StringBuilder sb = new StringBuilder(allLines.get(allLines.size() - 3).substring(42, 47));
            sb.deleteCharAt(2);
            String startLineTime1 = sb.toString();

            sb = new StringBuilder(allLines.get(allLines.size() - 3).substring(50, 56));
            sb.deleteCharAt(2);
            String endLineTime1 = sb.toString();

            // Récupération des horaires de l'après-midi
            sb = new StringBuilder(allLines.get(allLines.size() - 3).substring(62, 67));
            sb.deleteCharAt(2);
            String startLineTime2 = sb.toString();

            sb = new StringBuilder(allLines.get(allLines.size() - 3).substring(69, 74));
            sb.deleteCharAt(2);
            String endLineTime2 = sb.toString();

            sb = new StringBuilder(allLines.get(allLines.size() - 1).substring(19, 24));
            sb.deleteCharAt(2);
            String endLineOfDay = sb.toString();

            // Traitement sur le fichier texte
            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);

                if (i == 0 && (!line.startsWith("%") || !line.contains("métro"))) {
                    String error = "The first line must be begin with a %. (Line " + ++i + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                if (i == 1 && (!line.startsWith("%") || !line.contains("stations"))) {
                    String error = "The station tag was not found. (Line " + ++i + " in " + file.getName() + ")";
                    try {
                        throw new Exception(error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (line.startsWith("%") && line.contains("stations")) {
                    String temp = allLines.get(++i);
                    String[] stations = temp.split(" ");
                    Reseau reseau = Reseau.getInstance();
                    for (String station: stations) {
                        if (!reseau.verifStationExist(station)){
                            String error = "The station \"" + station + "\" doesn't exist. (Line " + ++i + " in " + file.getName() + ")";
                            try {
                                throw new Exception(error);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                }

                if (line.startsWith("%") && line.contains("liaisons A/R")) {
                    try {
                        Reseau reseau = Reseau.getInstance();
                        int j = ++i;
                        String temp = "";
                        while (!(temp = allLines.get(j)).isEmpty()) {
                            if (!temp.startsWith("%")) {
                                System.out.println(temp);
                            }
                            j++;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }



            /*BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line;
            int lineNumber = 1;
            while ((line = bufferedReader.readLine()) != null) {
                if (lineNumber == 1 && !line.startsWith("%") && !line.contains("métro")) {
                    String error = "The first line must be begin with a %. (Line " + lineNumber + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                Exploitant metro = new Exploitant("Métro");

                // Checking stations in network
                if (line.startsWith("%") && line.contains("stations")) {
                    String temp = bufferedReader.readLine();
                    lineNumber++;
                    String[] stations = temp.split(" ");
                    int finalLineNumber = lineNumber;
                    Reseau reseau = Reseau.getInstance();
                    for (String station: stations) {
                        if (!reseau.verifStationExist(station)){
                            String error = "The station \"" + station + "\" doesn't exist. (Line " + finalLineNumber + " in " + file.getName() + ")";
                            try {
                                throw new Exception(error);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                }

                // Adding links in network
                if (line.startsWith("%") && line.contains("liaisons A/R") &&(bufferedReader.readLine().equals("% depart arrivee duree"))) {
                    String temp = "";
                    try {
                        Reseau reseau = Reseau.getInstance();
                        while (!(temp = bufferedReader.readLine()).startsWith("%") && !temp.isEmpty()) {
                            String[] liaison = temp.split(" ");
                            System.out.println(temp);
                            Liaison l = new Liaison(setLiaisonName(liaison[0], liaison[1]), "0700", setTripTime(07, Integer.parseInt(liaison[2])), Integer.parseInt(liaison[2]), metro, reseau.findStationByName(liaison[0]), reseau.findStationByName(liaison[1]));
                            System.out.println(l);

                        }
                        System.out.println("je sors");
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                // Adding circuit in network
                if (line.startsWith("%") && line.contains("liaisons circuit") &&(bufferedReader.readLine().equals("% depart arrivee duree"))) {
                    String temp = "";
                    try {
                        Reseau reseau = Reseau.getInstance();
                        while (!(temp = bufferedReader.readLine()).startsWith("%") && !temp.isEmpty()) {
                            String[] liaison = temp.split(" ");
                            System.out.println(temp);
                            Liaison l = new Liaison(setLiaisonName(liaison[0], liaison[1]), "0700", setTripTime(07, Integer.parseInt(liaison[2])), Integer.parseInt(liaison[2]), metro, reseau.findStationByName(liaison[0]), reseau.findStationByName(liaison[1]));
                            System.out.println(l);

                        }

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                lineNumber++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String setStartTime(int timeBreakStation, int timeStepHappyHour, int timeStepNoHappyHour, String startTimeLine1, String endTimeLine1, String startTimeLine2, String endTimeLine2, String endLineOfDay, String name, int duree, Exploitant type, Station stationDepart, Station stationDestination) {
        ArrayList<Liaison> liaisons = new ArrayList<Liaison>();
        for (int i = 700; i <= 900; i+=10) {
            if (i == 760) i = 800;
            if (i == 860) i = 900;
            liaisons.add(new Liaison(name, Integer.toString(i), setTripTime(i, duree), duree, type, stationDepart, stationDestination));
        }

        for (int i = 920; i <= 1630; i+=20) {
            if (i == 960) i = 1000;
            if (i == 1060) i = 1100;
            if (i == 1160) i = 1200;
            if (i == 1260) i = 1300;
            if (i == 1360) i = 1400;
            if (i == 1460) i = 1500;
            if (i == 1560) i = 1600;
            liaisons.add(new Liaison(name, Integer.toString(i), setTripTime(i, duree), duree, type, stationDepart, stationDestination));

        }

        for (int i = 1630; i <= 1800; i+=10) {
            if (i == 1660) i = 1700;
            if (i == 1760) i = 1800;
            liaisons.add(new Liaison(name, Integer.toString(i), setTripTime(i, duree), duree, type, stationDepart, stationDestination));

        }

        for (int i = 1820; i <= 2300; i+=20) {
            if (i == 1860) i = 1900;
            if (i == 1960) i = 2000;
            if (i == 2060) i = 2100;
            if (i == 2160) i = 2200;
            if (i == 2260) i = 2300;
            liaisons.add(new Liaison(name, Integer.toString(i), setTripTime(i, duree), duree, type, stationDepart, stationDestination));

        }
        return liaisons.toString();
    }

    public static String setTripTime(int heure, int duree) {
        return "" + (heure + duree);
    }

    public static String setLiaisonName(String depart, String arrive) {
        return depart + arrive;
    }


    public static void readJSON(String filePath, Reseau reseau) throws IOException, ParseException {

        try {
            FileReader fileReader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

            JSONObject horaireParsed;
            JSONArray horaires;

            List<Station> allStation = new ArrayList<>();

            horaires = (JSONArray) jsonObject.get("horaires");

            if(horaires != null) {
                for (Object horaire : horaires) {
                    horaireParsed = (JSONObject) horaire;

                    getStationByHoraires(reseau, horaireParsed, allStation);

                    updateAddLiaison(reseau, horaireParsed, allStation);

                    allStation.clear();
                }
            }
            else {
                TagException("horaires", jsonObject);
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error syntax in json file : " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void getStationByHoraires(Reseau reseau, JSONObject horaireParsed, List<Station> allStation) throws Exception {

        JSONArray stations = (JSONArray) horaireParsed.get("stations");

        if(stations != null) {
            JSONObject stationParsed;

            for (Object station : stations) {
                stationParsed = (JSONObject) station;

                if(stationParsed.get("station") != null) {
                    if (!reseau.verifStationExist(stationParsed.get("station").toString())) {
                        try {
                            throw new Exception("Unknown station -> Station does not exist : " + stationParsed.get("station").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                    else {
                        allStation.add(reseau.findStationByName(stationParsed.get("station").toString()));
                    }
                }
                else {
                    TagException("station", stationParsed);
                }

            }
        }
        else {
            TagException("stations", horaireParsed);
        }


    }

    public static void updateAddLiaison(Reseau reseau, JSONObject horaireParsed, List<Station> allStation) throws Exception {

        JSONArray passages = (JSONArray) horaireParsed.get("passages");

        if(passages != null) {
            JSONArray passageParsed;

            for (Object passage : passages) {
                passageParsed = (JSONArray) passage;

                if(allStation.size() != passageParsed.size()) {
                    try {
                        throw new Exception("Not  the same number of data between station name and time indications -> There are " + allStation.size() + " station(s) whereas there are " + passageParsed.size() + " time indication(s) in " + passageParsed.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }

                for (int j = 0; j < passageParsed.size() - 1; j++) {

                    //System.out.println(allStation.get(j).getName() + " TO " + allStation.get(j + 1).getName() + " : " + passageParsed.get(j).toString() + " - " + passageParsed.get(j + 1).toString());

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
                //System.out.println();

            }
        }
        else {
            TagException("passages", horaireParsed);
        }

    }

    public static void TagException(String baliseName, JSONObject json) {
        try {
            throw new Exception("Tag not found -> Tag " + baliseName + "  not found in " + json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
