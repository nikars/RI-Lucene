package com.ri;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolai on 19/12/2014.
 */
public class QueryEngine {
    private int hitsPerPage = 20;
    private IndexReader reader;
    private IndexSearcher searcher;

    QueryEngine(File index) throws IOException {
        reader = DirectoryReader.open(FSDirectory.open(index));
        searcher = new IndexSearcher(reader);
    }

    public List<Document> tagQuery(String queryTerm) throws IOException {
        List<Document> returnedDocuments = new ArrayList<Document>();
        Analyzer analyzer = new WhitespaceAnalyzer();
        QueryParser parser = new QueryParser("tags", analyzer);

        try {
            Query query = parser.parse(queryTerm);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            for(int i=0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                returnedDocuments.add(d);
            }

        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        return returnedDocuments;
    }
}
