import java.io.File;

/**
 * Created by Nikolai on 01/12/2014.
 */
public class Application {
    public static void main(String[] args) {
        File dng = new File("C:\\Users\\Nikolai\\Desktop\\Tokyo Downtown\\2014-03_Tokyo_Downtown_0025.dng");
        Reader reader = new Reader();
        reader.readPhotoData(dng);
        reader.saveToXML();
    }
}
