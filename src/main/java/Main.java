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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();
        readJSON("src/main/resources/bus.json", reseau);
        //checkTextFile(new File("/Users/martinthibaut/Desktop/metro.txt"));
        readTrainXML("src/main/resources/train.xml", reseau);

        System.out.println(reseau);

    }


    private static void readTrainXML(String fileName, Reseau reseau) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(fileName));

            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("line");

            if(list != null) {

                for(int temp = 0 ; temp < list.getLength() ; temp ++) {

                    Node node = list.item(temp);
                    NodeList junctions = node.getChildNodes();
                    String lineName = node.getFirstChild().getNodeValue().replaceAll(" ", "").replaceAll("\n", "");

                    for(int tempJunction = 0 ; tempJunction < junctions.getLength() ; tempJunction ++) {

                        Node nodeJunction = junctions.item(tempJunction);

                        addLiaison(reseau, nodeJunction, lineName);


                    }
                }

            }
            else {
                try {
                    throw new Exception("Tag exception -> tag line does not existe");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // error balise
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void addLiaison(Reseau reseau, Node nodeJunction, String lineName) throws Exception {

        if (nodeJunction.getNodeType() == Node.ELEMENT_NODE) {

            Element element = (Element) nodeJunction;

            Node startStation = element.getElementsByTagName("start-station").item(0);
            Node endStation = element.getElementsByTagName("arrival-station").item(0);
            Node startHour = element.getElementsByTagName("start-hour").item(0);
            Node endHour = element.getElementsByTagName("arrival-hour").item(0);

            if(startStation == null || endStation == null || startHour == null || endHour == null) {
                try {
                    throw new Exception("Tag exception -> a tag 'start-station' or 'arrival-station' or 'start-hour' or 'arrival-hour' is not present in tag junction");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                int duree = getDureeByHour(startHour.getTextContent(), endHour.getTextContent());
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
                    try {
                        throw new Exception("Station name error -> station named " + startStation.getTextContent() + " or " + endStation.getTextContent() + " does not exist in th reseau");
                    } catch (Exception e ) {
                        e.printStackTrace();
                    }
                }
            }

        }
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
            JSONArray horaires;

            List<Station> allStation = new ArrayList<>();

            horaires = (JSONArray) jsonObject.get("horaires");

            if (horaires != null) {
                for (Object horaire : horaires) {
                    horaireParsed = (JSONObject) horaire;

                    getStationByHoraires(reseau, horaireParsed, allStation);

                    updateAddLiaison(reseau, horaireParsed, allStation);

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

    public static void getStationByHoraires(Reseau reseau, JSONObject horaireParsed, List<Station> allStation) throws Exception {

        JSONArray stations = (JSONArray) horaireParsed.get("stations");

        if (stations != null) {
            JSONObject stationParsed;

            for (Object station : stations) {
                stationParsed = (JSONObject) station;

                if (stationParsed.get("station") != null) {
                    if (!reseau.verifStationExist(stationParsed.get("station").toString())) {
                        try {
                            throw new Exception("Unknown station -> Station does not exist : " + stationParsed.get("station").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        allStation.add(reseau.findStationByName(stationParsed.get("station").toString()));
                    }
                } else {
                    TagException("station", stationParsed);
                }

            }
        } else {
            TagException("stations", horaireParsed);
        }


    }

    public static void updateAddLiaison(Reseau reseau, JSONObject horaireParsed, List<Station> allStation) throws Exception {

        JSONArray passages = (JSONArray) horaireParsed.get("passages");

        if (passages != null) {
            JSONArray passageParsed;

            for (Object passage : passages) {
                passageParsed = (JSONArray) passage;

                if (allStation.size() != passageParsed.size()) {
                    try {
                        throw new Exception("Not  the same number of data between station name and time indications -> There are " + allStation.size() + " station(s) whereas there are " + passageParsed.size() + " time indication(s) in " + passageParsed.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }

                for (int j = 0; j < passageParsed.size() - 1; j++) {

                    int duree = getDureeByHour(passageParsed.get(j).toString(), passageParsed.get(j + 1).toString());
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

            }
        } else {
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

    public static int getDureeByHour(String start, String end) throws Exception {

        if(start != null && end != null && start.length() == 4 && end.length() == 4) {

            int duree = Integer.parseInt(end.toString()) - Integer.parseInt(start.toString());

            if(start.charAt(1) != end.charAt(1)) {
                duree -= 40;
            }
            return duree;
        }
        else {
            try {
                throw new Exception("Horaire Format exception -> horaire must contains 4 characters, in " + start + " or " + end);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    }

}

