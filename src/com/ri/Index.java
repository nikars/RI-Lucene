package com.ri;


import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.ArrayList;


/**
 * Created by Rome's on 15/12/2014.
 */
public class Index {

    private static EnglishAnalyzer analyzer = new EnglishAnalyzer();
    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();



}
