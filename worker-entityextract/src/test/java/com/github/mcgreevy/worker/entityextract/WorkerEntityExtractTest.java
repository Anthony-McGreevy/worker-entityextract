/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
