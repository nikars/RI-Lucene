package com.ri;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Rome's on 15/12/2014.
 */
public class Index {
    private IndexWriter writer;
    private TaxonomyWriter taxo;
    private final FacetsConfig config = new FacetsConfig();


    // Contructor de la clase Index
    public Index(String indexLocation) throws IOException {
        File taxoDir = new File(indexLocation + "\\taxo");
        if (!taxoDir.exists()) {
            System.out.println("creating directory: taxo");
            try {
                taxoDir.mkdir();
            } catch (SecurityException se) {
            }
        }

        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tags", new StandardAnalyzer());
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);

        Directory dir = FSDirectory.open(new File(indexLocation));
        Directory dirTaxo = FSDirectory.open(taxoDir);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, aWrapper);
        taxo = new DirectoryTaxonomyWriter(dirTaxo, IndexWriterConfig.OpenMode.CREATE);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(dir, config);
    }

    public void indexDocs(List<Photo> photos) throws IOException {
        for (Photo photo : photos) {
            Document docPhoto = buildPhotoDoc(photo);
            writer.addDocument(config.build(taxo, docPhoto));
        }
        writer.close();
        taxo.close();
    }

    private Document buildPhotoDoc(Photo photo) {
        Document docPhoto = new Document();

        //Ruta al archivo de la photo
        Field pathField = new StringField("path", photo.filePath, Field.Store.YES);
        docPhoto.add(pathField);
        Field nameField = new StringField("name", photo.fileName, Field.Store.YES);
        docPhoto.add(nameField);
        Field mdPathField = new StringField("mdPath", photo.mdFilePath, Field.Store.YES);
        docPhoto.add(mdPathField);

        //Otros campos
        Calendar cal = Calendar.getInstance();
        cal.setTime(photo.creationDate);

        docPhoto.add(new StringField("date", new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()), Field.Store.NO));
        docPhoto.add(new TextField("month", new SimpleDateFormat("MMMM").format(cal.getTime()), Field.Store.NO));
        docPhoto.add(new StringField("year", new SimpleDateFormat("yyyy").format(cal.getTime()), Field.Store.NO));

        docPhoto.add(new StringField("aperture", photo.aperture, Field.Store.NO));
        docPhoto.add(new TextField("shutSpeed", photo.shutterSpeed, Field.Store.NO));
        docPhoto.add(new DoubleField("focalLength", photo.focalLength, Field.Store.NO));
        docPhoto.add(new StringField("flash", photo.flashFired, Field.Store.NO));
        docPhoto.add(new IntField("iso", photo.iso, Field.Store.NO));
        docPhoto.add(new TextField("tags", photo.tags, Field.Store.NO));

        //Facetas
        if (!photo.aperture.isEmpty())
            docPhoto.add(new FacetField("apertureCat", photo.aperture));

        if (!photo.shutterSpeed.isEmpty())
            docPhoto.add(new FacetField("shutSpeedCat", photo.shutterSpeed));

        if (photo.focalLength != 0)
            docPhoto.add(new FacetField("focalLengthCat", ((Double) photo.focalLength).toString()));

        if (photo.iso != 0)
            docPhoto.add(new FacetField("isoCat", ((Integer) photo.iso).toString()));

        if (!photo.flashFired.isEmpty())
            docPhoto.add(new FacetField("flashCat", photo.flashFired));

        if (!photo.orientation.isEmpty())
            docPhoto.add(new FacetField("orientationCat", photo.orientation));

        return docPhoto;
    }
}
