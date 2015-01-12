package com.ri;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Combina una consulta estandar con las listas de las facetas
 */
public class QueryWithFacets {
    private List<Document> documents;
    private List<FacetResult> facetResults;

    public QueryWithFacets(IndexSearcher searcher, ScoreDoc[] hits, List<FacetResult> results) {
        documents = new ArrayList<Document>();
        facetResults = results;

        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = null;
            try {
                d = searcher.doc(docId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            documents.add(d);
        }
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public List<FacetResult> getFacetResults() {
        return facetResults;
    }
}
