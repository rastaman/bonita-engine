/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ BPMSPTests.class })
@Initializer(BPMSPTestsForServers.class)
public class BPMSPTestsForServers {

    private static APITestSPUtil apiTestUtil = new APITestSPUtil();

    public static void beforeAll() throws BonitaException {
        System.err.println("=================== BPMSPTestsForServers.beforeClass()");
        apiTestUtil.createPlatformStructure();
        SPBPMTestUtil.createEnvironmentWithDefaultTenant();
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

    public static void afterAll() throws BonitaException {
        System.err.println("=================== BPMSPTestsForServers.afterClass()");
        SPBPMTestUtil.destroyPlatformAndTenants();
        apiTestUtil.deletePlatformStructure();
    }

}
