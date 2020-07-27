package com.github.mcgreevy.worker.entityextract;

import com.hpe.caf.worker.document.DocumentWorkerTestControllerProvider;
import com.hpe.caf.worker.testing.TestControllerSingle;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.UseAsTestName;
import com.hpe.caf.worker.testing.UseAsTestName_TestBase;
import com.hpe.caf.worker.testing.execution.TestControllerProvider;
import com.hpe.caf.worker.testing.execution.TestRunnerSingle;
import org.testng.annotations.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Integration test for worker-entityextract, running the testing framework.
 */
public class WorkerEntityExtractAcceptanceIT extends UseAsTestName_TestBase
{
    TestControllerProvider testControllerProvider;
    TestControllerSingle controller;

    @BeforeClass
    public void setUp() throws Exception
    {
        testControllerProvider = new DocumentWorkerTestControllerProvider();
        controller = TestRunnerSingle.getTestController(testControllerProvider, true);
        controller.initialise();
    }

    @AfterClass
    public void tearDown() throws Exception
    {
        controller.close();
    }

    @DataProvider(name = "MainTest")
    public Iterator<Object[]> createData() throws Exception
    {
        Set<Object[]> s = TestRunnerSingle.setUpTest(testControllerProvider);
        return s.iterator();
    }

    @UseAsTestName(idx = 1)
    @Test(dataProvider = "MainTest")
    public void testWorker(TestItem testItem, String testName) throws Exception
    {
        controller.runTests(testItem);
    }
}
