package com.ri;


import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Rome's on 15/12/2014.
 */
public class Index {
    private IndexWriter writer;

    // Contructor de la clase Index
    public Index(String indexLocation) throws IOException {
        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tags", new StandardAnalyzer());
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);

        Directory dir = FSDirectory.open(new File(indexLocation));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, aWrapper);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(dir, config);
    }

    public void indexDocs(List<Photo> photos) throws IOException {
        for(Photo photo : photos){
            Document docPhoto = buildPhotoDoc(photo);
            writer.addDocument(docPhoto);
        }
        writer.close();
    }

    private Document buildPhotoDoc(Photo photo) {
        Document docPhoto = new Document();
//        TaxonomyWriter writertaxo = new DirectoryTaxonomyWriter(taxoDir, IndexWriterConfig.OpenMode.CREATE); // se usa?
//        FacetFields facetFields = new FacetFields(writertaxo);

        //Ruta al archivo de la photo
        Field pathField = new StringField("path", photo.filePath, Field.Store.YES);
        docPhoto.add(pathField);
        Field nameField = new StringField("name", photo.fileName, Field.Store.YES);
        docPhoto.add(nameField);
        Field mdPathField = new StringField("mdPath", photo.mdFilePath, Field.Store.YES);
        docPhoto.add(mdPathField);

        //Otros campos
        docPhoto.add(new LongField("created", photo.creationDate.getTime(), Field.Store.NO));
        docPhoto.add(new TextField("aperture", photo.aperture, Field.Store.NO));
        docPhoto.add(new TextField("shutSpeed", photo.shutterSpeed, Field.Store.NO));
        docPhoto.add(new DoubleField("focalLength", photo.focalLength, Field.Store.NO));
        docPhoto.add(new TextField("flash", photo.flashFired, Field.Store.NO));
        docPhoto.add(new IntField("iso", photo.iso, Field.Store.NO));
        docPhoto.add(new TextField("orientation", photo.orientation, Field.Store.NO));
        docPhoto.add(new TextField("tags", photo.tags, Field.Store.NO));

        // Creamos Categorias
//        CathegoryPath cathegory = new CathegoryPath(,);
//
//        categories.add(cathegory);
//        writertaxo.addCategory(cathegory);
//        List<CategoryPath> categories = new ArrayList<CategoryPath>();
//        categories.add(new CategoryPath("aperture", )); // faltan los tipos de apertura
//        categories.add(new CategoryPath("shutterSpeed", )); // faltan los tipos de velocidad
//        categories.add(new CategoryPath("flashFired", "yes"));
//        categories.add(new CategoryPath("iso", )); // falta tipos iso
//        categories.add(new CategoryPath("orientation", "horizontal"));
//
//        DocumentBuilder categoryPhotoBuilder = new CategoryDocumentBuilder(wrytaxo);
//        categoryPhotoBuilderBuilder.setCategoryPaths(categories);
//        categoryPhotoBuilder.build(docPhoto);

        return docPhoto;
    }
}



//    // Indexamos documentos
//
//    private void indexPhotos(Photo photo) throws Exception {
//        // Indexamos fotos
//        for (Photo photos : photo) {
//            Document docPhoto = getPhoto(photo);
//
//            facetFields.addFields(docPhoto, categories);
//            writer.addDocument(docPhoto);
//            System.out.println("Added: " + Photo.fileName);
//            numDocs++;
//
//        }
//        writer.numDocs();
//        writer.optimize();
//
//    }
//
//    int orignalNPhotos = writer.numDocs(); // Lo inicializamos a 0 para ir sumando los documentos?
