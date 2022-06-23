package model;

import reader.TextReader;

import java.util.*;
import java.util.stream.Collectors;

public class Reseau {

    private static Reseau singleton;
    /*
    ~~~~~~~> Map < Nom de la station de départ , Liste des liaisons avec comme départ le nom de la station clé >
    */
    private Map<String, List<Liaison>> liaisons = new HashMap<>();
    /*
    ~~~~~~~> Map < Nom de la station , Station correspondante à la clé >
    */
    private Map<String, Station> stations = new HashMap<>(); // Clé : départ de la liaison

    private Reseau() {
        Station Limo = new Station("Limo", new ArrayList<>());
        Station Arly = new Station("Arly", new ArrayList<>());
        Station Singha = new Station("Singha", new ArrayList<>());
        Station Neuville = new Station("Neuville", new ArrayList<>());
        Station Syen = new Station("Syen", new ArrayList<>());
        Station Gare = new Station("Gare", new ArrayList<>());
        Station Avlon = new Station("Avlon", new ArrayList<>());
        Station Mairie = new Station("Mairie", new ArrayList<>());
        Station Piscine = new Station("Piscine", new ArrayList<>());
        Station Ecole = new Station("Ecole", new ArrayList<>());
        Station Parc = new Station("Parc", new ArrayList<>());

        Limo.addVoisin(Arly);
        Limo.addVoisin(Syen);

        Arly.addVoisin(Limo);
        Arly.addVoisin(Singha);
        Arly.addVoisin(Syen);

        Singha.addVoisin(Arly);
        Singha.addVoisin(Syen);
        Singha.addVoisin(Gare);
        Singha.addVoisin(Avlon);
        Singha.addVoisin(Neuville);

        Neuville.addVoisin(Singha);
        Neuville.addVoisin(Gare);
        Neuville.addVoisin(Avlon);

        Syen.addVoisin(Limo);
        Syen.addVoisin(Arly);
        Syen.addVoisin(Singha);
        Syen.addVoisin(Gare);
        Syen.addVoisin(Mairie);
        Syen.addVoisin(Ecole);
        Syen.addVoisin(Piscine);

        Gare.addVoisin(Syen);
        Gare.addVoisin(Singha);
        Gare.addVoisin(Neuville);
        Gare.addVoisin(Avlon);
        Gare.addVoisin(Piscine);
        Gare.addVoisin(Parc);
        Gare.addVoisin(Ecole);

        Avlon.addVoisin(Neuville);
        Avlon.addVoisin(Gare);
        Avlon.addVoisin(Singha);
        Avlon.addVoisin(Piscine);

        Mairie.addVoisin(Syen);
        Mairie.addVoisin(Ecole);

        Ecole.addVoisin(Mairie);
        Ecole.addVoisin(Syen);
        Ecole.addVoisin(Gare);
        Ecole.addVoisin(Piscine);
        Ecole.addVoisin(Parc);

        Parc.addVoisin(Ecole);
        Parc.addVoisin(Gare);
        Parc.addVoisin(Piscine);

        Piscine.addVoisin(Avlon);
        Piscine.addVoisin(Gare);
        Piscine.addVoisin(Syen);
        Piscine.addVoisin(Ecole);
        Piscine.addVoisin(Parc);

        stations.put("Parc", Parc);
        stations.put("Piscine", Piscine);
        stations.put("Gare", Gare);
        stations.put("Ecole", Ecole);
        stations.put("Limo", Limo);
        stations.put("Singha", Singha);
        stations.put("Neuville", Neuville);
        stations.put("Mairie", Mairie);
        stations.put("Syen", Syen);
        stations.put("Avlon", Avlon);
        stations.put("Arly", Arly);
    }

    public static Reseau getInstance() {
        if (singleton == null) {
            singleton = new Reseau();
            return singleton;
        }
        return singleton;
    }


    public void addLiaison(Liaison liaison) {
        if (liaisons.containsKey(liaison.getStationDepart().getName() + "-" + liaison.getStationDestination().getName())) {
            var ele = liaisons.get(liaison.getStationDepart().getName() + "-" + liaison.getStationDestination().getName());
            ele.add(liaison);
            liaisons.put(liaison.getStationDepart().getName() + "-" + liaison.getStationDestination().getName(), ele);
        } else {
            List<Liaison> ele = new ArrayList<>();
            ele.add(liaison);
            liaisons.put(liaison.getStationDepart().getName() + "-" + liaison.getStationDestination().getName(), ele);
        }
    }

    public void addStation(Station station) {
        stations.put(station.getName(), station);
    }

    public Map<String, List<Liaison>> getLiaisons() {
        return liaisons;
    }

    public void setLiaisons(Map<String, List<Liaison>> liaisons) {
        this.liaisons = liaisons;
    }

    public Map<String, Station> getStations() {
        return stations;
    }

    public void setStations(Map<String, Station> stations) {
        this.stations = stations;
    }

    public boolean verifStationExist(String stationName) {
        return stations.containsKey(stationName);
    }

    public Station findStationByName(String stationName) {
        return stations.get(stationName);
    }

    public List<Liaison> findLiaisonByStartStation(String stationName) {
        return liaisons.get(stationName);
    }

    @Override
    public String toString() {
        System.out.println();
        int i = 0;

        String informations = "Stations : [\n";
        for (Station station : stations.values()) {
            informations += "\t" + station.toString() + "\n";
        }
        informations += "]\n";
        informations += "Liaisons : [\n";

        for (List<Liaison> liaisonList : liaisons.values()) {
            for (Liaison liaison : liaisonList) {
                informations += "\t" + liaison.toString() + "\n";
                i++;
            }
        }
        informations += "]\n";

        System.out.println("Nb liaison : " + i);

        return informations;
    }


    /*
    ~~~~~~~~~~~~~~> " Plus court chemin part "
    */

    private List<Liaison> currentPlusCourtChemin = new ArrayList<>();

    public List<Liaison> getCourtChemin(String startStationName, String startHourNotParsed, String endStationName) {

        if (parametersAreValid(startStationName, startHourNotParsed, endStationName)) {
                test(startStationName, endStationName, startHourNotParsed);
        } else {
            System.err.println("Error parameters -> station does not exist or hour not correctly defined, hour need to be like '0810' for 08h10.");
        }

        return new ArrayList<>();
    }

    public void findPossibleTrajet(String stationName, String startHour, Map<String, List<Liaison>> map, String endStation, List<Liaison> trajet) {

        if (stationName.equals(endStation)) {
            var copyTrajet = new ArrayList<>(trajet);

            if (currentPlusCourtChemin.size() > 0 && currentCheminInfToHourIndicated(copyTrajet.get(copyTrajet.size() - 1).getHeureArrive())) {
                currentPlusCourtChemin = copyTrajet;
            }
        } else {

            try {

                for (Liaison liaison : map.get(stationName)) {
                    if (Integer.parseInt(liaison.getHeureDepart()) >= Integer.parseInt(startHour) && !liaison.isChecked() &&
                            (currentPlusCourtChemin.size() == 0 || currentCheminInfToHourIndicated(liaison.getHeureArrive()))
                    ) {
                        liaison.setChecked(true);
                        trajet.add(liaison);
                        trajet.forEach(System.out::println);

                        findPossibleTrajet(liaison.getStationDestination().getName(), liaison.getHeureArrive(), map, endStation, trajet);
                        trajet.remove(trajet.size() - 1);
                        liaison.setChecked(false);
                        System.out.println();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public boolean currentCheminInfToHourIndicated(String hour) {
        return Integer.parseInt(hour) < Integer.parseInt(currentPlusCourtChemin.get(currentPlusCourtChemin.size() - 1).getHeureArrive());
    }

    public boolean parametersAreValid(String startStationName, String startHour, String endStationName) {

        return singleton.verifStationExist(startStationName) &&
                singleton.verifStationExist(endStationName) &&
                startHour != null && startHour.length() == 4;

    }


    public void throwException(String exceptionComment) {
        try {
            throw new Exception(exceptionComment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void test(String startStation, String endStationName, String startHour) {

        Station stationInit = stations.get(startStation);
        List<String> listParcours = new ArrayList<>();
        Map<String, Integer> distanceList = new HashMap<>();
        Map<String, String> couleurList = new HashMap<>();
        Map<String, String> predecesseurList = new HashMap<>();

        stations.keySet().forEach(key -> {distanceList.put(key, -1); couleurList.put(key, "blanc"); predecesseurList.put(key, ""); });

        listParcours.add(startStation);
        distanceList.put(startStation, 0);
        couleurList.put(startStation, "gris");
        predecesseurList.put(startStation, "NULL");

        while (!listParcours.isEmpty()) {
            String sommetRemoved = listParcours.remove(0);

            getPossibleVoisin(singleton.findStationByName(sommetRemoved), startHour).stream()
                    .filter(voisin -> couleurList.get(voisin.getName()).equals("blanc")).toList()
                    .forEach(whiteVoisin -> {
                        couleurList.put(whiteVoisin.getName(), "gris");
                        predecesseurList.put(whiteVoisin.getName(), sommetRemoved);
                        Liaison bestLiaison = getBestLiaison(sommetRemoved, whiteVoisin.getName(), TextReader.setTripTime( Integer.parseInt(startHour), distanceList.get(sommetRemoved)) );
                        distanceList.put(whiteVoisin.getName(), distanceList.get(sommetRemoved) + bestLiaison.getDuree());
                        listParcours.add(whiteVoisin.getName());
                        System.out.println(bestLiaison);

                    });

            couleurList.put(sommetRemoved, "noir");
        }


        // Récupérer le chemin
        String val = predecesseurList.get(endStationName);
        List<String> chemin = new ArrayList<>();
        chemin.add(endStationName);
        while(!val.equals("NULL")){
            chemin.add(val);
            val = predecesseurList.get(val);
        }

        // Affichage du chemin
        System.out.print("Chemin à prendre : ");
        for (int i = chemin.size() - 1; i >= 0; i--) {
            System.out.print(chemin.get(i) + " - ");
        }
        System.out.println();
        System.out.println();

    }


    public List<Station> getPossibleVoisin(Station station, String hour) {

        List<Station> voisinsPossible = new ArrayList<>();

        for(String key : liaisons.keySet()) {

            if(key.startsWith(station.getName()) &&
                liaisons.get(key).size() > 0) {

                for(Liaison liaison : liaisons.get(key)) {
                    if(Integer.parseInt(hour) <= Integer.parseInt(liaison.getHeureDepart()) && !voisinsPossible.contains(liaisons.get(key).get(0).getStationDestination())) {
                        voisinsPossible.add(liaisons.get(key).get(0).getStationDestination());
                    }
                }
            }

        }


        return voisinsPossible;
    }


    public Liaison getBestLiaison(String startStation, String endStation, String hour) {

        int min = 0;
        Liaison liaisonSelected = null;

        if(liaisons.containsKey(startStation + "-" + endStation)) {

            for(Liaison liaison : liaisons.get(startStation + "-" + endStation)) {

                if (Integer.parseInt(hour) <= Integer.parseInt(liaison.getHeureDepart()) && (min == 0 || min > liaison.getDuree())) {
                    min = liaison.getDuree();
                    liaisonSelected = liaison;
                }

            }

        }

        return liaisonSelected;

    }


}
