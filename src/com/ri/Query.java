package com.ri;

/**
 * Created by Nikolai on 09/01/2015.
 */
public class Query {
    public int maxResults;
    public boolean pick;
    public String date;
    public String dateOp = "OR";
    public String aperture;
    public String apertureOp = "OR";
    public String shutSpeed;
    public String ssOp = "OR";
    public double focalLengthStart;
    public double focalLengthEnd;
    public String flOp = "OR";
    public String flash = "";
    public String flashOp = "OR";
    public int iso;
    public String isoOp = "OR";
    public String tags;
    public String tagOp = "OR";
}
