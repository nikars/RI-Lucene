package com.ri;

/**
 * Created by Nikolai on 09/01/2015.
 */
public class Query {
    public String date;
    public String aperture;
    public String apertureOp = "OR";
    public String shutSpeed;
    public String ssOp = "OR";
    public double focalLength;
    public String flOp = "OR";
    public String flash;
    public String flashOp = "OR";
    public int iso;
    public String isoOp = "OR";
    public String orientation;
    public String orientOp = "OR";
    public String tags;
    public String tagOp = "OR";

    public String compose() {
        StringBuilder sb = new StringBuilder();

        if(!date.isEmpty())
            sb.append("created:" + date);

        if(!aperture.isEmpty()) {
            if(sb.toString().isEmpty())
                sb.append("aperture:" + aperture);
            else sb.append(" " + apertureOp + " created:" + aperture);
        }

        if(!shutSpeed.isEmpty()) {
            if(sb.toString().isEmpty())
                sb.append("shutSpeed:" + shutSpeed);
            else sb.append(" " + ssOp + " shutSpeed:" + shutSpeed);
        }

        if(focalLength != 0) {
            if(sb.toString().isEmpty())
                sb.append("focalLength:" + focalLength);
            else sb.append(" " + flOp + " focalLength:" + focalLength);
        }

        if(!flash.isEmpty()) {
            if(sb.toString().isEmpty())
                sb.append("flash:" + flash);
            else sb.append(" " + flashOp + " flash:" + flash);
        }

        if(iso != 0) {
            if(sb.toString().isEmpty())
                sb.append("iso:" + iso);
            else sb.append(" " + isoOp + " iso:" + iso);
        }

        if(!orientation.isEmpty()) {
            if(sb.toString().isEmpty())
                sb.append("orientation:" + orientation);
            else sb.append(" " + orientOp + " orientation:" + orientation);
        }

        if(!tags.isEmpty()) {
            if(sb.toString().isEmpty())
                sb.append("tags:" + tags);
            else sb.append(" " + tagOp + " tags:" + tags);
        }

        return sb.toString();
    }
}
