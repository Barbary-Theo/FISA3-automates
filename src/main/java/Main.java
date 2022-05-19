import model.Exploitant;
import model.Liaison;
import model.Reseau;
import model.Station;

import java.io.*;
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
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();
        readJSON("src/main/resources/bus.json", reseau);
        //checkTextFile(new File("/Users/martinthibaut/Desktop/metro.txt"));
        readTrainXML("src/main/resources/train.xml", reseau);
        readTramXML("src/main/resources/tram.xml", reseau);

        System.out.println(reseau);

    }


    private static void readTramXML(String fileName, Reseau reseau) {

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
                return;
            }



            for(int temp = 0 ; temp < list.getLength() ; temp ++) {
                int counterRealTag = -1;

                Node node = list.item(temp);
                String lineName = node.getFirstChild().getNodeValue().replaceAll(" ", "").replaceAll("\n", "");


                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    succes = getInfoToUpdateReseau(reseau, node, counterRealTag, lineName);
                    if(!succes) return;

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private static void readTrainXML(String fileName, Reseau reseau) {
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
                        if(!succes) return;

                    }
                }

            }
            else {
                throwException("Tag exception -> tag 'line' does not exist");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

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

                }
                lineNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* ~~~~~~~~~~~~~~~~~~~ READ BUS ~~~~~~~~~~~~~~~~~~~ */

    public static void readJSON(String filePath, Reseau reseau) {
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
                return;
            }

            if (horaires != null) {
                for (Object horaire : horaires) {
                    horaireParsed = (JSONObject) horaire;

                    succes = getStationByHoraires(reseau, horaireParsed, allStation);
                    if(!succes) return;

                    succes = addLiaisonBus(reseau, horaireParsed, allStation, ligne);
                    if(!succes) return;

                    allStation.clear();
                }
            } else {
                TagException("horaires", jsonObject);
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error syntax in json file : " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

            int duree = Integer.parseInt(end) - Integer.parseInt(start);

            if(start.charAt(1) != end.charAt(1)) {
                duree -= 40;
            }
            return duree;
        }
        else {
            throwException("Horaire format exception -> horaire must contains 4 characters, in " + start + " or " + end);
            return -1;
        }

    }

}

