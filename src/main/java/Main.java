import model.Exploitant;
import model.Liaison;
import model.Reseau;
import model.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~> START <~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();

        boolean metroCheck = readMetroTEXT(new File("src/main/resources/metro.txt"), reseau);

        /*
        boolean busCheck = readJSONBus("src/main/resources/bus.json", reseau);
        //checkTextFile(new File("/Users/martinthibaut/Desktop/metro.txt"));
        boolean trainCheck = readTrainXML("src/main/resources/train.xml", reseau);
        boolean tramCheck = readTramXML("src/main/resources/tram.xml", reseau);

        if (tramCheck && trainCheck && busCheck) {
            System.out.println(reseau);
            reseau.getCourtChemin("Gare", "0810", "Avlon");
        }
         */
        //System.out.println(reseau.toString());


    }


    public static boolean readMetroTEXT(File fileName, Reseau reseau) throws Exception {

        List<String> allLines = Files.readAllLines(Paths.get(String.valueOf(fileName)));
        boolean success = true;
        Map<String, Double> info = new HashMap<>();

        // 0) Devra récupérer les infos pour la création des trajets time start/fin ..
        for(int i = allLines.size() -1   ; i >= 0 ; i--) {
            String line = allLines.get(i);

            if(line.contains("% dernier départ à ")) {
                String horaire = line.split("% dernier départ à ")[1];
                try {
                    String[] endInfo = horaire.split(":");

                    info.put("end", Double.parseDouble(endInfo[0] + endInfo[1]) / 100.0);
                } catch (Exception e) {
                    throwException("Error information -> can not parse " + horaire + " to horaire");
                    return false;
                }
            }

            if(line.contains("% départ de Gare toutes les") && line.contains("sinon")) {
                String startAprem = line.split("% départ de Gare toutes les")[1];
                String minute = startAprem.split(" minutes")[0];
                try {
                    info.put("intervalAprem", Double.parseDouble(minute) / 100.0);
                } catch (Exception e) {
                    throwException("Error information -> " + minute + " is not minute");
                    return false;
                }
            }
            else if (line.contains("% départ de Gare toutes les")) {
                System.out.println(Arrays.toString(line.split("% départ de Gare toutes les ")));
            }

            if(line.contains("% arrêt de ") && line.contains(" minutes en station")){
                String time = line.split("% arrêt de ")[1];
                String minute = time.split(" minutes en station")[0];
                try {
                    info.put("arretStation", Double.parseDouble(minute) / 100.0);

                } catch (Exception e) {
                    throwException("Error information -> " + minute + " is not minute");
                    return false;
                }
            }
        }

        System.out.println(info.toString());


        for(int i = 0 ; i < allLines.size() ; i ++) {
            int j = i;
            String line = allLines.get(i);

            if(line != null && line.contains("% depart arrivee duree")) {

                j++;
                List<String[]> circuitStocke = new ArrayList<>();

                success = getAllCircuit(circuitStocke, allLines, i, j);
                if (!success) return false;

                success = checkLineFormat(reseau, circuitStocke);
                if (!success) return false;

                success = createLiaison(reseau, circuitStocke, info);
                if (!success) return false;

                i = j;
            }

        }

        return true;
    }


    public static boolean getAllCircuit(List<String[]> circuitStocke, List<String> allLines, int i, int j) {

        while (allLines.get(j) != null && !allLines.get(j).equals("") && !allLines.get(j).contains("%")) {
            String[] eleLine = allLines.get(j).split(" ");

            if (eleLine.length == 3) circuitStocke.add(eleLine);
            else {
                throwException("Error format -> line does not comport 'StationDepartName StationEndName Time' : " + allLines.get(j));
                return false;
            }
            j++;
        }

        return true;
    }

    public static boolean checkLineFormat(Reseau reseau, List<String[]> circuitStocke) {

        for(int stringIndex = 0 ; stringIndex < circuitStocke.size() - 1 ; stringIndex ++) {
            if(!circuitStocke.get(stringIndex)[1].equals(circuitStocke.get(stringIndex + 1)[0]) ) {
                throwException("Error format -> end station is not the same than next line start station : " + Arrays.toString(circuitStocke.get(stringIndex)));
                return false;
            }
            if(reseau.findStationByName(circuitStocke.get(stringIndex)[0]) == null) {
                throwException("Error Station name -> station " + circuitStocke.get(stringIndex)[0] + " does not exist");
                return false;
            }
            if(reseau.findStationByName(circuitStocke.get(stringIndex)[1]) == null) {
                throwException("Error Station name -> station " + circuitStocke.get(stringIndex)[0] + " does not exist");
                return false;
            }
        }
        return true;
    }

    public static boolean createLiaison(Reseau reseau, List<String[]> circuitStocke, Map<String, Double> info) {

        double start = 7;
        double end = info.get("end");
        double finMatin = 9;
        double debutAprem = 16.30;
        double finAprem = 18;
        double intervalStartMatinAprem = 10 / 100.0;
        double intervalStartAprem = info.get("intervalAprem");
        double arretStation = Math.round((info.get("arretStation")) * 100) / 100.0;

        while (start <= end) {

            double circuitTime = start;
            for (String[] pseudoLiaison : circuitStocke) {
                try {
                    double duree = Double.parseDouble(pseudoLiaison[2]) / 100.0;
                    String startHour = formatDoubleToHoraire(circuitTime);
                    String endHour = formatDoubleToHoraire(Math.round(checkHoraireValueAfterComputed(circuitTime + duree) * 100.0) / 100.0);
                    Station startStation = reseau.findStationByName(pseudoLiaison[0]);
                    Station endStation = reseau.findStationByName(pseudoLiaison[1]);

                    if(startHour == null || endHour == null) return false;

                    Liaison liaison = new Liaison(
                            "Aucun",
                            startHour,
                            endHour,
                            getDureeByHour(startHour, endHour),
                            new Exploitant("Métro"),
                            startStation,
                            endStation
                    );
                    reseau.addLiaison(liaison);
                    circuitTime = Math.round(checkHoraireValueAfterComputed(circuitTime + duree + arretStation) * 100.0) / 100.0;

                } catch (Exception e) {
                    throwException("Parsing error -> impossible to parse " + pseudoLiaison[2] + " to a duration");
                    return false;
                }
            }

            if(start < finMatin || (start < finAprem && start >= debutAprem)) start += intervalStartMatinAprem;
            else start += intervalStartAprem;

            start = Math.round(start * 100.0) / 100.0;
            start = checkHoraireValueAfterComputed(start);

        }

        return true;
    }

    public static String formatDoubleToHoraire(Double value) {

        String[] valueParts = value.toString().split("\\.");

        if (valueParts.length != 0) {

            if(valueParts[0].length() == 1) {
                valueParts[0] = 0 + valueParts[0];
            }
            if(valueParts[1].length() == 1) {
                valueParts[1] += 0;
            }

            return valueParts[0] + valueParts[1];
        }
        else {
            throwException("Error format horaire -> " + value + " can not be split by '.' to create horaire");
            return null;
        }

    }


    public static double checkHoraireValueAfterComputed(double value) {

        int supValue = (int) Math.round(value);
        int infValue;
        double decimal;
        double reste;

        if(supValue > value) {
            infValue = supValue - 1;
        }
        else {
            infValue = supValue;
        }
        decimal = Math.abs(Math.round((infValue - value) * 100.0) / 100.0);

        if(decimal >= 0.6) {
            reste = decimal - 0.6;
            return infValue + 1 + reste;
        }
        else {
            return value;
        }

    }

    public static boolean readTramXML(String fileName, Reseau reseau) {

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

    public static boolean addLiaisonTrain(Reseau reseau, Node nodeJunction, String lineName) {

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

