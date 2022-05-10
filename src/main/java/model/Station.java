package model;

import java.util.List;

public class Station {

    private String name;
    private List<Station> voisines;

    public Station(String name, List<Station> voisines) {
        this.name = name;
        this.voisines = voisines;
    }


    public void addVoisin(Station station) {
        voisines.add(station);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Station> getVoisines() {
        return voisines;
    }

    public void setVoisines(List<Station> voisines) {
        this.voisines = voisines;
    }
}
