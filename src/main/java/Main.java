import model.Reseau;
import reader.*;

import java.io.*;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println(" \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~> START <~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

        Reseau reseau = Reseau.getInstance();

        boolean busCheck = JsonReader.readJSONBus("src/main/resources/bus.json", reseau);
        boolean interCheck = TextReader.readInterCiteTEXT("src/main/resources/interCites.txt", reseau);
        boolean trainCheck = XmlReader.readTrainXML("src/main/resources/train.xml", reseau);
        boolean tramCheck = XmlReader.readTramXML("src/main/resources/tram.xml", reseau);
        boolean metroCheck = TextReader.readMetroTEXT(new File("src/main/resources/metro.txt"), reseau);


        if (tramCheck && trainCheck && busCheck && metroCheck && interCheck) {
            var res = reseau.getCourtChemin("Limo", "1300", "Neuville");
        }

    }
}

