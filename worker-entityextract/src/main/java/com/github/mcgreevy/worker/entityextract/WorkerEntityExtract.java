package com.github.mcgreevy.worker.entityextract;

import com.google.gson.Gson;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.model.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * This is an example implementation of the DocumentWorker interface.
 * <p>
 * Implementing the DocumentWorker interface provides an easy way to efficiently integrate into the Data Processing pipeline. Documents
 * passing through the pipeline can be routed to the worker and enriched from an external source such as a database.
 * <p>
 * The example implementation simply does a lookup from an internal in-memory map.
 * <p>
 * If it would be more efficient to process multiple documents together then the BulkDocumentWorker interface can be implemented instead
 * of the DocumentWorker interface.
 */
public class WorkerEntityExtract implements DocumentWorker
{
    public WorkerEntityExtract()
    {
    }

    @Override
    public void checkHealth(HealthMonitor healthMonitor)
    {
    }

    @Override
    public void processDocument(final Document document) throws InterruptedException, DocumentWorkerTransientException
    {
        final Gson gson = new Gson();
        final List<String> grammars = gson.fromJson(document.getCustomData("grammars"), ArrayList.class);
        final List<String> fieldsToDetect = gson.fromJson(document.getCustomData("fieldsToDetect"), ArrayList.class);
        final List<String> typesToReport = new ArrayList<>();
        for (final String grammar : grammars) {
            final String grammarName = grammar;
            double confidenceScore = 0;
            int hits = 0;
            try {
                final NameFinderME finder = getFinder(grammarName);
                for (final String field : fieldsToDetect) {
                    for (final String fieldValue : document.getField(field).getStringValues()) {
                        final String[] textToSearch = fieldValue.split("[^a-zA-Z0-9']+");
                        final Span[] span = finder.find(textToSearch);
                        hits = span.length;
                        for (final Span spanElement : span) {
                            if (!typesToReport.contains(grammarName)) {
                                document.getField("SUSPECTED_PII").add(grammarName);
                                confidenceScore = confidenceScore + spanElement.getProb();
                                typesToReport.add(grammarName);
                            }
                        }
                    }
                }
                if (hits > 0) {
                    document.getField("SUSPECTED_PII_" + (grammarName.toUpperCase(Locale.UK).replace("-", "_")) + "_AVERAGE_CONFIDENCE_SCORE")
                        .add(String.valueOf(new DecimalFormat("#.00").format((confidenceScore / hits)*100)));
                }
                finder.clearAdaptiveData();
            } catch (final IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private NameFinderME getFinder(final String grammar) throws FileNotFoundException, IOException
    {
        try (final InputStream is = this.getClass().getResourceAsStream("/" + grammar + ".bin")) {
            final TokenNameFinderModel model = new TokenNameFinderModel(is);
            return new NameFinderME(model);
        }
    }
}
