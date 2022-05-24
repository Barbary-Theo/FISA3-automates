package model;

import java.util.*;

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
        Station Arly = new Station("Arly",  new ArrayList<>());
        Station Singha = new Station("Singha",  new ArrayList<>());
        Station Neuville = new Station("Neuville",  new ArrayList<>());
        Station Syen = new Station("Syen",  new ArrayList<>());
        Station Gare = new Station("Gare",  new ArrayList<>());
        Station Avlon = new Station("Avlon",  new ArrayList<>());
        Station Mairie = new Station("Mairie",  new ArrayList<>());
        Station Piscine = new Station("Piscine",  new ArrayList<>());
        Station Ecole = new Station("Ecole",  new ArrayList<>());
        Station Parc = new Station("Parc",  new ArrayList<>());

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
        if(singleton == null) {
            singleton = new Reseau();
            return singleton;
        }
        return singleton;
    }


    public void addLiaison(Liaison liaison) {
        if(liaisons.containsKey(liaison.getStationDepart().getName())) {
            var ele = liaisons.get(liaison.getStationDepart().getName());
            ele.add(liaison);
            liaisons.put(liaison.getStationDepart().getName(), ele);
        }
        else {
            List<Liaison> ele = new ArrayList<>();
            ele.add(liaison);
            liaisons.put(liaison.getStationDepart().getName(), ele);
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

    public void setStations( Map<String, Station> stations) {
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

        String informations = "Stations : [\n";
        for(Station station : stations.values()) {
            informations += "\t" + station.toString() + "\n";
        }
        informations += "]\n";
        informations += "Liaisons : [\n";

        for(List<Liaison> liaisonList : liaisons.values()) {
            for (Liaison liaison : liaisonList) {
                informations += "\t" + liaison.toString() + "\n";
            }
        }
        informations += "]\n";

        return informations;
    }


    /*
    ~~~~~~~~~~~~~~> " Plus court chemin part "
    */

    public static List<Liaison> getCourtChemin(String startStationName, String startHour, String endStationName, String endHour) {

        return new ArrayList<>();
    }

}
