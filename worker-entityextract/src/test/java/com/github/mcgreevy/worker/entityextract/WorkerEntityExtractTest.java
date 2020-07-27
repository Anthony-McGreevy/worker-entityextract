package com.github.mcgreevy.worker.entityextract;

import com.hpe.caf.worker.document.DocumentWorkerFieldEncoding;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.testing.DocumentBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integration test for WorkerEntityExtract, running the testing framework.
 */
public class WorkerEntityExtractTest
{
    @Test
    public void exampleTest() throws Exception
    {
        final Document document = DocumentBuilder.configure()
            .withFields()
            .addFieldValues("REFERENCE", "/mnt/fs/docs/hr policy.doc", "/mnt/fs/docs/strategy.doc")
            .addFieldValue("REFERENCED_VALUE", "VGhpcyBpcyBhIHRlc3QgdmFsdWU", DocumentWorkerFieldEncoding.base64)
            .addFieldValue("CONTENT", "Philip Crooks, Anthony McGreevy, 07955027429, this shouldnt be detected. New York is awesome, how is Australia, do you like Melbourne?", DocumentWorkerFieldEncoding.utf8)
            .documentBuilder()
            .withCustomData()
            .add("fieldsToDetect", "[CONTENT]")
            .add("grammars", "[\"en-ner-location\", \"en-ner-person\"]")
            .documentBuilder()
            .build();

        final WorkerEntityExtract sut = new WorkerEntityExtract();

        sut.processDocument(document);
        Assert.assertTrue(document.getField("SUSPECTED_PII").hasChanges());
        Assert.assertTrue(document.getField("SUSPECTED_PII").hasValues());
        Assert.assertTrue(document.getField("SUSPECTED_PII").getStringValues().size() == 2);
    }
}
