/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.event.LocalInterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.LocalTimerEventIT;
import org.bonitasoft.engine.job.JobExecutionIT;
import org.bonitasoft.engine.theme.ThemeIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        JobExecutionIT.class,
        BPMLocalIT.class,
        ConnectorExecutionsLocalIT.class,
        ProcessWithExpressionLocalIT.class,
        ProcessArchiveIT.class,
        LocalTimerEventIT.class,
        LocalInterruptingTimerBoundaryEventIT.class,
        DataInstanceIntegrationLocalIT.class,
        GatewayExecutionLocalIT.class,
        ThemeIT.class,
        PermissionAPIIT.class
})
@RunWith(Suite.class)
public class BPMLocalSuiteTests {

}
