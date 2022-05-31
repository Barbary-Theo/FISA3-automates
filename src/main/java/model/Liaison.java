package model;

import java.util.Date;
import java.util.List;

public class Liaison {

    private String name;
    private String heureArrive;
    private String heureDepart;
    private int duree;
    private Exploitant type;
    private Station stationDepart;
    private Station stationDestination;

    public Liaison(String heureDepart, String heureArrive, int duree, Exploitant type, Station stationDepart, Station stationDestination) {
        this.heureArrive = heureArrive;
        this.heureDepart = heureDepart;
        this.duree = duree;
        this.type = type;
        this.stationDepart = stationDepart;
        this.stationDestination = stationDestination;
    }

    public Liaison(String name, String heureDepart, String heureArrive, int duree, Exploitant type, Station stationDepart, Station stationDestination) {
        this.name = name;
        this.heureArrive = heureArrive;
        this.heureDepart = heureDepart;
        this.duree = duree;
        this.type = type;
        this.stationDepart = stationDepart;
        this.stationDestination = stationDestination;
    }


    public Liaison(String name, String heureDepart, String heureArrive,  Exploitant type, Station stationDepart, Station stationDestination) {
        this.name = name;
        this.heureArrive = heureArrive;
        this.heureDepart = heureDepart;
        this.type = type;
        this.stationDepart = stationDepart;
        this.stationDestination = stationDestination;
    }

    public String getHeureArrive() {
        return heureArrive;
    }

    public void setHeureArrive(String heureArrive) {
        this.heureArrive = heureArrive;
    }

    public String getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Exploitant getAllTypes() {
        return type;
    }

    public void setAllTypes(Exploitant type) {
        this.type = type;
    }

    public Station getStationDepart() {
        return stationDepart;
    }

    public void setStationDepart(Station stationDepart) {
        this.stationDepart = stationDepart;
    }

    public Station getStationDestination() {
        return stationDestination;
    }

    public void setStationDestination(Station stationDestination) {
        this.stationDestination = stationDestination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;

    }

    @Override
    public String toString() {
        return "~~> line " + name + " : from " + stationDepart.getName() + " at " + formatHour(heureDepart) + " to " + stationDestination.getName() + " at " + formatHour(heureArrive) + ", in " + type.getType() + " during " + duree + " minutes";
    }


    public String formatHour(String horaire) {
        String infos = "";
        infos = horaire.charAt(0) + "" + horaire.charAt(1) + "h" + horaire.charAt(2) + "" + horaire.charAt(3);
        return infos;
    }

}
