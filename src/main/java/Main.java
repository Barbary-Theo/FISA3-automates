import model.Exploitant;
import model.Liaison;
import model.Reseau;
import model.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~> START <~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();

        boolean busCheck = readJSONBus("src/main/resources/bus.json", reseau);
        //readMetroTEXT(new File("src/main/resources/metro.txt"));
        //checkTextFile(new File("/Users/martinthibaut/Desktop/metro.txt"));
        boolean trainCheck = readTrainXML("src/main/resources/train.xml", reseau);
        boolean tramCheck = readTramXML("src/main/resources/tram.xml", reseau);

        if (tramCheck && trainCheck && busCheck) {
            System.out.println(reseau);
            reseau.getCourtChemin("Gare", "0810", "Avlon");
        }
    }

    private static boolean readTramXML(String fileName, Reseau reseau) {

        boolean succes;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(fileName));

            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("ligne");

            if(list.getLength() == 0) {
                throwException("Error syntax -> Tags 'ligne' not found");
                return false;
            }



            for(int temp = 0 ; temp < list.getLength() ; temp ++) {
                int counterRealTag = -1;

                Node node = list.item(temp);
                String lineName = node.getFirstChild().getNodeValue().replaceAll(" ", "").replaceAll("\n", "");


                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    succes = getInfoToUpdateReseau(reseau, node, counterRealTag, lineName);
                    if(!succes) return false;

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private static boolean getInfoToUpdateReseau(Reseau reseau, Node node, int counterRealTag, String lineName) {
        Element element = (Element) node;
        NodeList line = node.getChildNodes();
        boolean succes;
        String[] stations;

        for(int tempJunction = 0 ; tempJunction < line.getLength() ; tempJunction ++) {

            Node nodeJunction = line.item(tempJunction);

            if (nodeJunction.getNodeType() == Node.ELEMENT_NODE ) {
                counterRealTag ++;


                if(counterRealTag == 0) {
                    if(!nodeJunction.getChildNodes().toString().contains("stations")) {
                        throwException("Error Tag -> the first tag of tag 'ligne' need to be 'stations' within stations");
                        return false;
                    }
                }
                else {

                    stations = element.getElementsByTagName("stations").item(0).getTextContent().split(" ");

                    Node currentHorairesNode = nodeJunction.getChildNodes().item(0);
                    String[] currentHoraires = currentHorairesNode.getTextContent().split(" ");

                    if(stations.length == currentHoraires.length) {

                        succes = addLiaisonTram(reseau, currentHoraires, stations, lineName);
                        if(!succes) return false;

                    }
                    else {
                        throwException("Error syntax -> the number of timestamp indicated "+ Arrays.toString(currentHoraires) +" is not the same than stations "+ Arrays.toString(stations));
                        return false;
                    }

                }

            }
        }
        return true;
    }


    private static boolean addLiaisonTram(Reseau reseau, String[] currentHoraires, String[] stations, String lineName) {

        for(int indexCurrentHoraires = 0 ; indexCurrentHoraires < currentHoraires.length - 1 ; indexCurrentHoraires++) {

            if(reseau.verifStationExist(stations[indexCurrentHoraires]) && reseau.verifStationExist(stations[indexCurrentHoraires + 1])) {

                int duree = getDureeByHour(currentHoraires[indexCurrentHoraires], currentHoraires[indexCurrentHoraires + 1]);
                if (duree == -1) return false;

                reseau.addLiaison(
                        new Liaison(
                                lineName,
                                currentHoraires[indexCurrentHoraires + 1],
                                currentHoraires[indexCurrentHoraires],
                                duree,
                                new Exploitant("Tram"),
                                reseau.findStationByName(stations[indexCurrentHoraires]),
                                reseau.findStationByName(stations[indexCurrentHoraires + 1])
                        )
                );
            }
            else {
                throwException("Error station name -> station " + stations[indexCurrentHoraires] + " or " + stations[indexCurrentHoraires + 1] + " does not exist in the reseau");
                return false;
            }
        }

        return true;

    }



    /* ~~~~~~~~~~~~~~~~~~~ READ TRAIN ~~~~~~~~~~~~~~~~~~~ */

    private static boolean readTrainXML(String fileName, Reseau reseau) {
        boolean succes;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(fileName));

            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("line");

            if(list != null && list.getLength() != 0) {

                for(int temp = 0 ; temp < list.getLength() ; temp ++) {

                    Node node = list.item(temp);
                    NodeList junctions = node.getChildNodes();
                    String lineName = node.getFirstChild().getNodeValue().replaceAll(" ", "").replaceAll("\n", "");

                    for(int tempJunction = 0 ; tempJunction < junctions.getLength() ; tempJunction ++) {

                        Node nodeJunction = junctions.item(tempJunction);

                        succes = addLiaisonTrain(reseau, nodeJunction, lineName);
                        if(!succes) return false;

                    }
                }

            }
            else {
                throwException("Tag exception -> tag 'line' does not exist");
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public static boolean addLiaisonTrain(Reseau reseau, Node nodeJunction, String lineName) throws Exception {

        if (nodeJunction.getNodeType() == Node.ELEMENT_NODE) {

            Element element = (Element) nodeJunction;

            Node startStation = element.getElementsByTagName("start-station").item(0);
            Node endStation = element.getElementsByTagName("arrival-station").item(0);
            Node startHour = element.getElementsByTagName("start-hour").item(0);
            Node endHour = element.getElementsByTagName("arrival-hour").item(0);

            if(startStation == null || endStation == null || startHour == null || endHour == null) {
                throwException("Tag syntax error -> a tag 'start-station' or 'arrival-station' or 'start-hour' or 'arrival-hour' is not present in tag junction or wrongly spelt");
            }
            else {
                int duree = getDureeByHour(startHour.getTextContent(), endHour.getTextContent());
                if (duree == -1) return false;
                Station stationDepart = reseau.findStationByName(startStation.getTextContent());
                Station stationEnd = reseau.findStationByName(endStation.getTextContent());

                if(reseau.verifStationExist(startStation.getTextContent()) && reseau.verifStationExist(endStation.getTextContent())) {
                    reseau.addLiaison(
                            new Liaison(
                                    lineName,
                                    endHour.getTextContent(),
                                    startHour.getTextContent(),
                                    duree,
                                    new Exploitant("Train"),
                                    stationDepart,
                                    stationEnd
                            )
                    );
                }
                else {
                    throwException("Station name error -> station named " + startStation.getTextContent() + " or " + endStation.getTextContent() + " does not exist in the reseau");
                    return false;
                }
            }

        }
        return true;
    }


    static void checkTextFile(File file) {
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
<<<<<<< HEAD
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
=======

>>>>>>> 1b2478f30f0b9873dadafbb30a63e2b2bf0d351e
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


    /* ~~~~~~~~~~~~~~~~~~~ READ BUS ~~~~~~~~~~~~~~~~~~~ */

    public static boolean readJSONBus(String filePath, Reseau reseau) {
        boolean succes;

        try {
            FileReader fileReader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

            JSONObject horaireParsed;
            JSONArray horaires;

            List<Station> allStation = new ArrayList<>();

            horaires = (JSONArray) jsonObject.get("horaires");

            String ligne = (String) jsonObject.get("ligne");

            if (ligne == null) {
                TagException("ligne", jsonObject);
                return false;
            }

            if (horaires != null) {
                for (Object horaire : horaires) {
                    horaireParsed = (JSONObject) horaire;

                    succes = getStationByHoraires(reseau, horaireParsed, allStation);
                    if(!succes) return false;

                    succes = addLiaisonBus(reseau, horaireParsed, allStation, ligne);
                    if(!succes) return false;

                    allStation.clear();
                }
            } else {
                TagException("horaires", jsonObject);
                return false;
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error syntax in json file : " + e);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public static boolean getStationByHoraires(Reseau reseau, JSONObject horaireParsed, List<Station> allStation) {

        JSONArray stations = (JSONArray) horaireParsed.get("stations");

        if (stations != null) {
            JSONObject stationParsed;

            for (Object station : stations) {
                stationParsed = (JSONObject) station;

                if (stationParsed.get("station") != null) {

                    if (!reseau.verifStationExist(stationParsed.get("station").toString())) {
                        throwException("Unknown station -> Station does not exist in the reseau : " + stationParsed.get("station").toString());
                        return false;
                    }
                    else {
                        allStation.add(reseau.findStationByName(stationParsed.get("station").toString()));
                    }

                }
                else {
                    TagException("station", stationParsed);
                    return false;
                }

            }
        }
        else {
            TagException("stations", horaireParsed);
            return false;
        }

        return true;


    }

    public static boolean addLiaisonBus(Reseau reseau, JSONObject horaireParsed, List<Station> allStation, String ligne) {

        JSONArray passages = (JSONArray) horaireParsed.get("passages");

        if (passages != null) {
            JSONArray passageParsed;

            for (Object passage : passages) {
                passageParsed = (JSONArray) passage;

                if (allStation.size() != passageParsed.size()) {
                    throwException("Not  the same number of data between station name and timestamp -> There are " + allStation.size() + " station(s) whereas there are " + passageParsed.size() + " timestamp in " + passageParsed);
                    return false;
                }

                for (int j = 0; j < passageParsed.size() - 1; j++) {

                    int duree = getDureeByHour(passageParsed.get(j).toString(), passageParsed.get(j + 1).toString());
                    if (duree == -1) return false;

                    reseau.addLiaison(
                            new Liaison(
                                    ligne, // name
                                    passageParsed.get(j + 1).toString(),
                                    passageParsed.get(j).toString(),
                                    duree,
                                    new Exploitant("Bus"),
                                    allStation.get(j),
                                    allStation.get(j + 1)
                            )
                    );
                }

            }
        }
        else {
            TagException("passages", horaireParsed);
            return false;
        }

        return true;

    }

    public static void TagException(String baliseName, JSONObject json) {
        try {
            throw new Exception("Tag not found -> Tag " + baliseName + "  not found in " + json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void throwException(String exceptionComment) {
        try {
            throw new Exception(exceptionComment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getDureeByHour(String start, String end) {

        if(start != null && end != null && start.length() == 4 && end.length() == 4) {

            try {
                int duree = Integer.parseInt(end) - Integer.parseInt(start);

                if(start.charAt(1) != end.charAt(1)) {
                    duree -= 40;
                }
                return duree;

            } catch (Exception e) {
                throwException("Horaire format exception -> horaire can't be parse in number, in " + start + " or " + end);
                return -1;
            }
        }
        else {
            throwException("Horaire format exception -> horaire must contains 4 characters, in " + start + " or " + end);
            return -1;
        }

    }

}

