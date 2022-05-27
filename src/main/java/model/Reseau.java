package model;

import java.util.ArrayList;
import java.util.List;

public class Reseau {

    private static Reseau singleton;
    private List<Liaison> liaisons = new ArrayList<>();
    private List<Station> stations = new ArrayList<>();

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

        stations.add(Parc);
        stations.add(Piscine);
        stations.add(Gare);
        stations.add(Ecole);
        stations.add(Limo);
        stations.add(Singha);
        stations.add(Neuville);
        stations.add(Mairie);
        stations.add(Syen);
        stations.add(Avlon);
        stations.add(Arly);
    }

    public static Reseau getInstance() {
        if(singleton == null) {
            singleton = new Reseau();
            return singleton;
        }
        return singleton;
    }


    public void addLiaison(Liaison liaison) {
        liaisons.add(liaison);
    }

    public void addAllLiaison(ArrayList<Liaison> l) {
        liaisons.addAll(l);
    }

    public void addStation(Station station) {
        stations.add(station);
    }

    public List<Liaison> getLiaisons() {
        return liaisons;
    }

    public void setLiaisons(List<Liaison> liaisons) {
        this.liaisons = liaisons;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public boolean verifStationExist(String stationName) {
        return stations.stream().anyMatch(station -> station.getName().equals(stationName));
    }

    public Station findStationByName(String stationName) {

        for(Station station : stations) {
            if(station.getName().equals(stationName)) {
                return station;
            }
        }

        return null;
    }
}
