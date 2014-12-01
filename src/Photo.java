import javafx.util.Pair;

import java.util.Date;

/**
 * Created by Nikolai on 01/12/2014.
 */
public class Photo {
    public String fileName;
    public String aperture;
    public Date creationDate;
    public String shutterSpeed;
    public String focalLength;
    public boolean flashFired;
    public int iso;
    public Orientation orientation;
    public Pair<Double, Double> location;
    public String tags;

    public enum Orientation {HORIZONTAL, VERTICAL}
}
