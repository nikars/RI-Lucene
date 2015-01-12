package com.ri;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
    public String filePath = "";
    public String mdFilePath = "";
    public String aperture = ""; // categoria
    public Date creationDate = new Date();
    public String shutterSpeed = ""; // categoria
    public double focalLength; // categoria
    public String flashFired = ""; // categoria
    public int iso; // categoria
    public String orientation = ""; //categoria
    public String tags = "";

    public Photo() {
    }

    public Photo(File file) {
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

        fileName = org.apache.commons.io.FilenameUtils.removeExtension(file.getName());
        filePath = file.getPath();
        aperture = metadata.get("Aperture Value");

        try {
            creationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                    Locale.ENGLISH).parse(metadata.get("Creation-Date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        shutterSpeed = metadata.get("Shutter Speed Value");
        focalLength = Double.parseDouble(metadata.get("exif:FocalLength"));
        flashFired = metadata.get("exif:Flash");
        iso = Integer.parseInt(metadata.get("ISO Speed Ratings"));

        // ¿1234? http://www.impulseadventure.com/photo/exif-orientation.html
        orientation = metadata.get("tiff:Orientation").matches("[1234]") ? "horizontal" : "vertical";

        for (String field : metadata.getValues("Keywords")) {
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

            e = dom.createElement("file-path");
            e.appendChild(dom.createTextNode(filePath));
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
            e.appendChild(dom.createTextNode(((Double) focalLength).toString()));
            rootEle.appendChild(e);

            e = dom.createElement("flash-fired");
            e.appendChild(dom.createTextNode(flashFired));
            rootEle.appendChild(e);

            e = dom.createElement("iso");
            e.appendChild(dom.createTextNode(((Integer) iso).toString()));
            rootEle.appendChild(e);

            e = dom.createElement("orientation");
            e.appendChild(dom.createTextNode(orientation));
            rootEle.appendChild(e);

            if (tags != null) {
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

    public void readFromXml(String path) {
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(path);

            Element doc = dom.getDocumentElement();

            fileName = getTextValue(fileName, doc, "file-name");
            mdFilePath = path;
            filePath = getTextValue(filePath, doc, "file-path");
            aperture = getTextValue(aperture, doc, "aperture");
            shutterSpeed = getTextValue(shutterSpeed, doc, "shutter-speed");
            focalLength = Double.parseDouble(getTextValue(((Double) focalLength).toString(), doc, "focal-length"));
            flashFired = getTextValue(flashFired, doc, "flash-fired");
            iso = Integer.parseInt(getTextValue(((Integer) iso).toString(), doc, "iso"));
            orientation = getTextValue(orientation, doc, "orientation");
            tags = getTextValue(tags, doc, "tags");

            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
            try {
                creationDate = formatter.parse(getTextValue(creationDate.toString(), doc, "creation-date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    private String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
}
