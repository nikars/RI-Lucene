package com.ri;


import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rome's on 15/12/2014.
 */
public class Index {

    private static WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
    private IndexWriter writer;
    private ArrayList<File> list = new ArrayList<File>(); // Creamos lista de files
    private List<Photo> photos = new ArrayList<Photo>();  // Creamos lista de archivos

// Contructor de la clase Index

    public void Index(String indexLocation) throws IOException {

        FSDirectory dir = FSDirectory.open(new File(indexLocation));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(dir, config);

    }

    // Creamos Documentos
    public static Document getPhoto

    {
        String Nombre, String path, String aperture, String shutspeed, String focalLength,boolean flashFired, String
        iso, String orientation, String latitude, String longitude, String[]tags, String date
    }

    {

        //Creamos documento
        Document docPhoto = new Document();

        TaxonomyWriter writertaxo = new DirectoryTaxonomyWriter(taxoDir, OpenMode.CREATE); // se usa?
        FacetFields facetFields = new FacetFields(writertaxo);

        docPhoto.add(new Field("docPhoto.fileName", TitleText, Field.Store.YES, Index.analyzer));

        // Creamos Campos
        docPhoto.add(new StringField("fileName", photo.fileName, Field.Store.YES));
        docPhoto.add(new StringField("pathPhoto", photo.); // Queremos guardar el path de la foto?
        docPhoto.add(new TextField("aperture", photo.aperture, Field.Store.YES));
        docPhoto.add(new TextField("shutspeed", photo.shutterSpeed, Field.Store.YES));
        docPhoto.add(new TextField("focalLength", photo.focalLength, Field.Store.YES));
        docPhoto.add(new BoolField("flashFired", photo.flashFired, Field.Store.YES));
        docPhoto.add(new LongField("iso", photo.iso, Field.Store.YES));
        docPhoto.add(new TextField("orientation", photo.orientation, Field.Store.YES));
        docPhoto.add(new TextField("latitude", photo.latitude, Field.Store.YES));
        docPhoto.add(new TextField("longitude", photo.longitude, Field.Store.YES));
        docPhoto.add(new TextField("tag", photo.tags, Field.Store.YES));
        String date = new datetoString(photo.creationDate);
        docPhoto.add(new TestField("date", date, Field.Store.YES));


        // Creamos Categorias

        CathegoryPath cathegory = new CathegoryPath(,);

        categories.add(cathegory);
        writertaxo.addCategory(cathegory);
        List<CategoryPath> categories = new ArrayList<CategoryPath>();
        categories.add(new CategoryPath("aperture", )); // faltan los tipos de apertura
        categories.add(new CategoryPath("shutterSpeed", )); // faltan los tipos de velocidad
        categories.add(new CategoryPath("flashFired", "yes"));
        categories.add(new CategoryPath("iso", )); // falta tipos iso
        categories.add(new CategoryPath("orientation", "horizontal"));

        DocumentBuilder categoryPhotoBuilder = new CategoryDocumentBuilder(wrytaxo);
        categoryPhotoBuilderBuilder.setCategoryPaths(categories);
        categoryPhotoBuilder.build(docPhoto);




        return docPhoto;

    }

    // Indexamos documentos

    private void indexPhotos(Photo photo) throws Exception {
        // Indexamos fotos
        for (Photo photos : photo) {
            Document docPhoto = getPhoto(photo);

            facetFields.addFields(docPhoto, categories);
            writer.addDocument(docPhoto);
            System.out.println("Added: " + Photo.fileName);
            numDocs++;

        }
        writer.numDocs();
        writer.optimize();
        writer.close();
    }

    int orignalNPhotos = writer.numDocs(); // Lo inicializamos a 0 para ir sumando los documentos?
