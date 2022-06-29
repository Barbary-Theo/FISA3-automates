package reader;

import model.Exploitant;
import model.Liaison;
import model.Reseau;
import model.Station;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TextReader {

    /* ~~~~~~~~~~~~~~~~~~~ READ INTER-CITE ~~~~~~~~~~~~~~~~~~~ */

    public static boolean readInterCiteTEXT(String path, Reseau reseau) {
        try {
            File file = new File(path);
            List<String> allLines = Files.readAllLines(Paths.get(path));

            Exploitant car = new Exploitant("Car");
            ArrayList<String> lines = new ArrayList<String>();
            boolean liaison = false;
            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);

                if (i == 0 && (!line.startsWith("%") || !line.contains("Car Inter-Cité"))) {
                    String error = "The first line is not in the expected format. (Line " + ++i + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                if (i == 3 && (!line.startsWith("%") || !line.contains("liste des liaisons"))) {
                    String error = "The first line is not in the expected format. (Line " + ++i + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                if (i == 6 && (!line.startsWith("%") || !line.contains("liste d'horaires"))) {
                    String error = "The first line is not in the expected format. (Line " + ++i + " in " + file.getName() + ")";
                    throw new Exception(error);
                }

                if (!line.startsWith("%")) {
                    if (line.startsWith("//")) {
                        liaison = true;
                    }

                    if (!liaison) {
                        lines.add(line);
                    }else {
                        for (String element : lines) {
                            String[] tableau = element.split("[ \t]+");
                            String[] temp = line.split("[ \t]+");
                            String debut = tableau[0];
                            String fin = tableau[1];
                            if (line.startsWith(debut) && line.contains(fin)) {
                                if(temp[2].length() != 4) {
                                    throwException("Error Format horaire -> horaire " + temp[2] + " does not respect format ^[0-9]{4}$");
                                    return false;
                                }
                                reseau.addLiaison(new Liaison(setLiaisonName(debut, fin), temp[2], setTripTime(Integer.parseInt(temp[2]), Integer.parseInt(tableau[2])), Integer.parseInt(tableau[2]), new Exploitant("Car Inter-Cité"), reseau.findStationByName(temp[0]), reseau.findStationByName(temp[1])));
                            }
                            else if (line.startsWith(fin) && line.contains(debut)) {
                                if(temp[2].length() != 4) {
                                    throwException("Error Format horaire -> horaire " + temp[2] + " does not respect format ^[0-9]{4}$");
                                    return false;
                                }
                                reseau.addLiaison(new Liaison(setLiaisonName(fin, debut), temp[2], setTripTime(Integer.parseInt(temp[2]), Integer.parseInt(tableau[2])), Integer.parseInt(tableau[2]), new Exploitant("Car Inter-Cité"), reseau.findStationByName(temp[0]), reseau.findStationByName(temp[1])));
                            }

                        }
                    }
                }
            }


        } catch (Exception e) {
            System.err.println("The file is not in the expected format. Please correct it !");
        }
        return true;
    }

    public static String setTripTime(int heure, int duree) {

        int toInt = Integer.parseInt("" + (heure + duree));
        String toString = Integer.toString(toInt);

        if(toString.length() < 4 ) {
            toString = "0"  + toString;
        }

        int hour = Integer.parseInt(toString.substring(0, 2));

        if(Integer.parseInt(toString.substring(0, 2) + "60") <= toInt ) {
            hour += 1;
            int reste = toInt - Integer.parseInt(toString.substring(0, 2) + "60");

            if((hour+"").length() < 2 ) {
                toString = "0"  + hour;
                return toString + "" + reste;
            }
            else {
                if((reste+"").length() < 2) {
                    return hour + "0" + reste;
                }
                return hour + "" + reste;
            }

        }

        return toString;


    }

    public static String setLiaisonName(String depart, String arrive) {
        return depart + "-" + arrive;
    }

    /* ~~~~~~~~~~~~~~~~~~~ READ INTER-CITE ~~~~~~~~~~~~~~~~~~~ */

    public static boolean readMetroTEXT(File fileName, Reseau reseau) throws Exception {

        List<String> allLines = Files.readAllLines(Paths.get(String.valueOf(fileName)));
        boolean success = true;
        Map<String, Double> info = new HashMap<>();

        success = getAllInfoDepart(info, allLines);
        if (!success) return false;

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

    public static boolean getAllCircuit(List<String[]> circuitStocke, List<String> allLines, int i, int j) {

        while (j < allLines.size() && allLines.get(j) != null && !allLines.get(j).equals("") && !allLines.get(j).contains("%")) {
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



    public static boolean createLiaison(Reseau reseau, List<String[]> circuitStocke, Map<String, Double> info) {

        double start = info.get("startMatin");
        double end = info.get("end");
        double finMatin = info.get("finMatin");
        double debutAprem = info.get("startAprem");
        double finAprem = info.get("finAprem");
        double intervalStartMatinAprem = info.get("each");
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

    public static boolean getAllInfoDepart(Map<String, Double> info, List<String> allLines) {

        boolean success = true;

        for(int i = allLines.size() -1   ; i >= 0 ; i--) {
            String line = allLines.get(i);

            if(line.contains("% depart arrivee duree")) {
                // dire qu'il existe au moins une ligne d'indiquée
                info.put("line", 1.0);
            }

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
                success = getInfoTiming(info, line);
                if (!success) return false;
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

        if(!info.containsKey("line")) {
            throwException("Error information -> There is any line indicated with key line '% depart arrivee duree' ");
        }

        if(!info.containsKey("each") || !info.containsKey("end") || !info.containsKey("intervalAprem") || !info.containsKey("arretStation")) {
            throwException("Error information -> all information are not indicated check if following sentence are indicated : \n" +
                    "\t % arrêt de X minutes en station\n" +
                    "\t % départ de Gare toutes les X minutes de hh:mm à hh:mm et de hh:mm à hh:mm\n" +
                    "\t % départ de Gare toutes les X minutes sinon\n" +
                    "\t % dernier départ à hh:mm");
            return false;
        }

        return true;

    }


    public static boolean getInfoTiming(Map<String, Double> info, String line) {
        try {
            String[] firstPart = line.split("% départ de Gare toutes les");
            String[] secondPart = firstPart[1].split(" minutes de ");
            String eachString = secondPart[0];
            String[] thirdPart = secondPart[1].split(" à ");
            String startMatinString = thirdPart[0];
            String finApremString = thirdPart[2];
            String[] fourthPart = thirdPart[1].split(" et de ");
            String finMatinString = fourthPart[0];
            String startApremString = fourthPart[1];

            String[] startMatinTemp = startMatinString.split(":");
            double startMatin = Double.parseDouble(startMatinTemp[0] + startMatinTemp[1]);

            String[] finMatinTemp = finMatinString.split(":");
            double finMatin = Double.parseDouble(finMatinTemp[0] + finMatinTemp[1]);

            String[] startApremTemp = startApremString.split(":");
            double startAprem = Double.parseDouble(startApremTemp[0] + startApremTemp[1]);

            String[] finApremTemp = finApremString.split(":");
            double finAprem = Double.parseDouble(finApremTemp[0] + finApremTemp[1]);

            info.put("each", Math.round((Double.parseDouble(eachString) / 100.0) * 100 )/ 100.0);
            info.put("startMatin", startMatin/ 100.0);
            info.put("finMatin", finMatin / 100.0);
            info.put("startAprem", startAprem / 100.0);
            info.put("finAprem", (double) Math.round(finAprem / 100.0));
        } catch (Exception e) {
            throwException("Error information -> line " + line + " does not respect format '% départ de Gare toutes les X minutes de hh:mm à hh:mm et de hh:mm à hh:mm'");
            return false;
        }
        return true;
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
