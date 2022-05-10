package model;

import java.util.Date;
import java.util.List;

public class Liaison {

    private Date heureArrive;
    private Date heureDepart;
    private List<Exploitant> allTypes;
    private Station stationDepart;
    private Station stationDestination;

    public Liaison(Date heureArrive, Date heureDepart, List<Exploitant> allTypes, Station stationDepart, Station stationDestination) {
        this.heureArrive = heureArrive;
        this.heureDepart = heureDepart;
        this.allTypes = allTypes;
        this.stationDepart = stationDepart;
        this.stationDestination = stationDestination;
    }

    public void addType(Exploitant type) {
        allTypes.add(type);
    }

    public Date getHeureArrive() {
        return heureArrive;
    }

    public void setHeureArrive(Date heureArrive) {
        this.heureArrive = heureArrive;
    }

    public Date getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(Date heureDepart) {
        this.heureDepart = heureDepart;
    }

    public List<Exploitant> getAllTypes() {
        return allTypes;
    }

    public void setAllTypes(List<Exploitant> allTypes) {
        this.allTypes = allTypes;
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
}
