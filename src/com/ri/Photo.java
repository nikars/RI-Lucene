package com.ri;

import javafx.util.Pair;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Atributos relevantes de una foto y métodos para leerlos de un fichero DNG
 * y guardar/recuperar de un XML.
 */
public class Photo {
    public String fileName = "";
    public String aperture = "";
    public Date creationDate = new Date();
    public String shutterSpeed = "";
    public double focalLength;
    public boolean flashFired;
    public int iso;
    public String orientation = "";
    public String latitude = ""; //TODO convertir a double si hace falta
    public String longitude = ""; //TODO convertir a double si hace falta
    public String tags = "";

    public Photo (File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Metadata metadata = new Metadata();
        ContentHandler ch = new BodyContentHandler();
        ParseContext parseContext = new ParseContext();
        AutoDetectParser parser = new AutoDetectParser();
        try {
            parser.parse(is, ch, metadata, parseContext);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }

//        for(String field : metadata.names()) {
//            System.out.print(field);
//            System.out.print(" : ");
//            System.out.println(metadata.get(field));
//        }

        fileName = org.apache.commons.io.FilenameUtils.removeExtension(file.getName());
        aperture = metadata.get("Aperture Value");

        try {
            creationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                    Locale.ENGLISH).parse(metadata.get("Creation-Date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        shutterSpeed = metadata.get("Shutter Speed Value");
        focalLength = Double.parseDouble(metadata.get("exif:FocalLength"));
        flashFired =  metadata.get("exif:Flash").equals("false") ? false : true;
        iso = Integer.parseInt(metadata.get("ISO Speed Ratings"));

        // ¿1234? Pero de dónde salen estos números?? http://www.impulseadventure.com/photo/exif-orientation.html
        orientation = metadata.get("tiff:Orientation").matches("[1234]") ? "horizontal" : "vertical";

        latitude = metadata.get("GPS Latitude");
        longitude = metadata.get("GPS Longitude");
        for(String field : metadata.getValues("Keywords")) {
            if (tags.equals(""))
                tags = field;
            else
                tags += " " + field;
        }
    }

    public void saveToXml(String path) {
        Document dom;
        Element e = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            Element rootEle = dom.createElement("photo");

            e = dom.createElement("file-name");
            e.appendChild(dom.createTextNode(fileName));
            rootEle.appendChild(e);

            e = dom.createElement("aperture");
            e.appendChild(dom.createTextNode(aperture));
            rootEle.appendChild(e);

            e = dom.createElement("creation-date");
            e.appendChild(dom.createTextNode(creationDate.toString()));
            rootEle.appendChild(e);

            e = dom.createElement("shutter-speed");
            e.appendChild(dom.createTextNode(shutterSpeed));
            rootEle.appendChild(e);

            e = dom.createElement("focal-length");
            e.appendChild(dom.createTextNode(((Double)focalLength).toString()));
            rootEle.appendChild(e);

            e = dom.createElement("flash-fired");
            e.appendChild(dom.createTextNode(((Boolean) (flashFired)).toString()));
            rootEle.appendChild(e);

            e = dom.createElement("iso");
            e.appendChild(dom.createTextNode(((Integer)iso).toString()));
            rootEle.appendChild(e);

            e = dom.createElement("orientation");
            e.appendChild(dom.createTextNode(orientation));
            rootEle.appendChild(e);

            if(latitude != null) {
                e = dom.createElement("gps-latitude");
                e.appendChild(dom.createTextNode(latitude));
                rootEle.appendChild(e);
            }

            if(longitude != null) {
                e = dom.createElement("gps-longitude");
                e.appendChild(dom.createTextNode(longitude));
                rootEle.appendChild(e);
            }

            if(tags != null) {
                e = dom.createElement("tags");
                e.appendChild(dom.createTextNode(tags));
                rootEle.appendChild(e);
            }

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                tr.transform(new DOMSource(dom), new StreamResult(
                        new FileOutputStream(path + "\\" + fileName + ".xml")));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    public void printData() {
        System.out.println(fileName);
        System.out.println("  Creation Date: " + creationDate);
        System.out.println("  Aperture: " + aperture);
        System.out.println("  Shutter Speed: " + shutterSpeed);
        System.out.println("  Focal Length: " + focalLength);
        System.out.println("  Flash Fired: " + flashFired);
        System.out.println("  ISO: " + iso);
        System.out.println("  Orientation: " + orientation);
        System.out.println("  Latitud GPS: " + latitude);
        System.out.println("  Longitud GPS: " + longitude);
        System.out.println("  Tags: " + tags);

        System.out.println();
    }
}
