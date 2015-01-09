package com.ri;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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

    public List<Document> runQuery(Query queryTerm) throws IOException {
        List<Document> returnedDocuments = new ArrayList<Document>();
        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tags", new StandardAnalyzer());
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);
        QueryParser parser = new QueryParser("tags", aWrapper);

        try {
            System.out.println(queryTerm.compose());
            org.apache.lucene.search.Query query = parser.parse(queryTerm.compose());
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
