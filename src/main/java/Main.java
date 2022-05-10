import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    public static void main(String[] args) {
        System.out.println("oui");
    }



    public static void readJSON(String filePath) throws FileNotFoundException {

        filePath = "";

        try {
            new FileReader(filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error : File not found");
        }


    }

}
