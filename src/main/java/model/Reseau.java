package model;

import java.util.ArrayList;
import java.util.List;

public class Reseau {

    private static Reseau singleton;
    private List<Liaison> liaisons = new ArrayList<>();
    private List<Station> stations = new ArrayList<>();

    private Reseau() { }

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
}
