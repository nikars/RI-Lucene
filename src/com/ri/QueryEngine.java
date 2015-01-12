package com.ri;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikolai on 19/12/2014.
 */
public class QueryEngine {

    private IndexReader reader;
    private IndexSearcher searcher;
    private TaxonomyReader taxoReader;
    private int maxResults;
    private List<FacetResult> results;
    private org.apache.lucene.search.Query latestQuery;
    private FacetsConfig config = new FacetsConfig();

    private File index;

    QueryEngine(File index) {
        this.index = index;
    }

    private void init() throws IOException {
        reader = DirectoryReader.open(FSDirectory.open(index));
        searcher = new IndexSearcher(reader);
        File taxoDir = new File(index.getPath() + "\\taxo");
        taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(taxoDir));
    }

    public QueryWithFacets runQuery(Query queryTerm) throws IOException {
        init();
        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tags", new StandardAnalyzer());
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);
        ScoreDoc[] hits = null;

        try {
            maxResults = queryTerm.maxResults;
            TopScoreDocCollector collector = TopScoreDocCollector.create(maxResults, true);
            FacetsCollector fc = new FacetsCollector();
            BooleanQuery combined = new BooleanQuery();

            // Cadenas de texto
            if (!queryTerm.aperture.isEmpty()) {
                QueryParser parser = new QueryParser("aperture", aWrapper);
                org.apache.lucene.search.Query apertureQuery = parser.parse(queryTerm.aperture);
                if (queryTerm.apertureOp.equals("AND"))
                    combined.add(apertureQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.apertureOp.equals("NOT"))
                    combined.add(apertureQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(apertureQuery, BooleanClause.Occur.SHOULD);
            }

            if (!queryTerm.shutSpeed.isEmpty()) {
                QueryParser parser = new QueryParser("shutSpeed", aWrapper);
                org.apache.lucene.search.Query ssQuery = parser.parse(queryTerm.shutSpeed);
                if (queryTerm.ssOp.equals("AND"))
                    combined.add(ssQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.ssOp.equals("NOT"))
                    combined.add(ssQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(ssQuery, BooleanClause.Occur.SHOULD);

                System.out.println(ssQuery.toString());
            }

            if (!queryTerm.flash.isEmpty()) {
                QueryParser parser = new QueryParser("flash", aWrapper);
                org.apache.lucene.search.Query flashQuery = parser.parse(queryTerm.flash);
                if (queryTerm.flashOp.equals("AND"))
                    combined.add(flashQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.flashOp.equals("NOT"))
                    combined.add(flashQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(flashQuery, BooleanClause.Occur.SHOULD);
            }

            if (!queryTerm.tags.isEmpty()) {
                QueryParser parser = new QueryParser("tags", aWrapper);
                org.apache.lucene.search.Query tagQuery = parser.parse(queryTerm.tags);
                if (queryTerm.tagOp.equals("AND"))
                    combined.add(tagQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.tagOp.equals("NOT"))
                    combined.add(tagQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(tagQuery, BooleanClause.Occur.SHOULD);
            }

            if (queryTerm.pick) {
                QueryParser parser = new QueryParser("tags", aWrapper);
                org.apache.lucene.search.Query tagQuery = parser.parse("pick");
                combined.add(tagQuery, BooleanClause.Occur.MUST);
            }

            // Fecha
            if (!queryTerm.date.isEmpty()) {
                MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
                        new String[]{"date", "month", "year"}, aWrapper);
                org.apache.lucene.search.Query dateQuery = queryParser.parse(queryTerm.date);

                if (queryTerm.dateOp.equals("AND"))
                    combined.add(dateQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.dateOp.equals("NOT"))
                    combined.add(dateQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(dateQuery, BooleanClause.Occur.SHOULD);
            }

            // Numericos
            if (queryTerm.iso != 0) {
                org.apache.lucene.search.Query isoQuery = NumericRangeQuery.newIntRange("iso", 1, queryTerm.iso,
                        queryTerm.iso, true, true);

                if (queryTerm.isoOp.equals("AND"))
                    combined.add(isoQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.isoOp.equals("NOT"))
                    combined.add(isoQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(isoQuery, BooleanClause.Occur.SHOULD);
            }
            if (queryTerm.focalLengthStart != 0) {
                org.apache.lucene.search.Query flQuery = NumericRangeQuery.newDoubleRange("focalLength", 1,
                        queryTerm.focalLengthStart, queryTerm.focalLengthEnd, true, true);

                if (queryTerm.flOp.equals("AND"))
                    combined.add(flQuery, BooleanClause.Occur.MUST);
                else if (queryTerm.flOp.equals("NOT"))
                    combined.add(flQuery, BooleanClause.Occur.MUST_NOT);
                else
                    combined.add(flQuery, BooleanClause.Occur.SHOULD);
            }

            latestQuery = combined;
            searcher.search(combined, collector);
            hits = collector.topDocs().scoreDocs;

            //Facetas
            FacetsCollector.search(searcher, combined, 10, fc);
            results = new ArrayList<FacetResult>();

            Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
            results.add(facets.getTopChildren(10, "orientationCat"));
            results.add(facets.getTopChildren(10, "apertureCat"));
            results.add(facets.getTopChildren(10, "shutSpeedCat"));
            results.add(facets.getTopChildren(10, "focalLengthCat"));
            results.add(facets.getTopChildren(10, "isoCat"));
            results.add(facets.getTopChildren(10, "flashCat"));


        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        QueryWithFacets res = new QueryWithFacets(searcher, hits, results);

        taxoReader.close();
        reader.close();

        return res;
    }

    public QueryWithFacets drillDown(String facet, String value)
            throws IOException {
        init();

        DrillDownQuery q = new DrillDownQuery(config, latestQuery);
        q.add(facet, value);

        DrillSideways ds = new DrillSideways(searcher, config, taxoReader);
        DrillSideways.DrillSidewaysResult result = ds.search(q, maxResults);

        List<FacetResult> facets = result.facets.getAllDims(10);

        QueryWithFacets res = new QueryWithFacets(searcher, result.hits.scoreDocs, facets);

        taxoReader.close();
        reader.close();
        return res;
    }
}
