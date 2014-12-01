import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.*;


/**
 * Created by Nikolai on 01/12/2014.
*/
public class Reader {
    Photo photo = new Photo();

    public Photo readPhotoData(File file) {
        Tika tika = new Tika();

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Metadata metadata = new Metadata();
        ContentHandler ch = new BodyContentHandler(10 * 1024 * 1024); // Incrementar el límite de caracteres
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


//
//        for(String field : metadata.names()) {
//            System.out.print(field);
//            System.out.print(" : ");
//            System.out.println(metadata.get(field));
//        }
        for(String field : metadata.getValues("Keywords")) {
            photo.tags += ", " + field;
        }

        photo.aperture = metadata.get("Aperture Value");
        photo.shutterSpeed = metadata.get("Shutter Speed Value");
        photo.focalLength = metadata.get("exif:FocalLength");
        photo.flashFired =  metadata.get("exif:Flash").equals("false") ? false : true;
        photo.iso = Integer.parseInt(metadata.get("ISO Speed Ratings"));
        photo.orientation = metadata.get("Orientation").equals("[Left]") ? Photo.Orientation.HORIZONTAL :Photo.Orientation.HORIZONTAL;
        System.out.println(photo.aperture);
        System.out.println(photo.shutterSpeed);
        System.out.println(((Boolean) photo.flashFired).toString());
        System.out.println(photo.iso);
        System.out.println(photo.orientation.toString());

        return photo;
    }

    public void saveToXML() {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("metadata");

            // create data elements and place them under root
            e = dom.createElement("aperture");
            e.appendChild(dom.createTextNode(photo.aperture));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream("C:\\Users\\Nikolai\\Desktop\\test.xml")));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
}
