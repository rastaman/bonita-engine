/**
 * Copyright (C) 2014 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.work.TransactionServiceForTest;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceContextWorkTest {

    private static final long PROCESS_INSTANCE_ID = 2;

    private static final long ROOT_PROCESS_INSTANCE_ID = 3;

    @Mock
    private ProcessDefinitionContextWork wrappedWork;

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private SProcessInstance sProcessInstance;

    @Spy
    private TransactionServiceForTest transactionService;

    private ProcessInstanceContextWork txBonitawork;

    @Before
    public void before() throws Exception {
        doReturn(ROOT_PROCESS_INSTANCE_ID).when(sProcessInstance).getRootProcessInstanceId();
        doReturn(sProcessInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);

        doReturn(processInstanceService).when(tenantAccessor).getProcessInstanceService();
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(loggerService);
        when(tenantAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        when(tenantAccessor.getSessionService()).thenReturn(sessionService);
        when(tenantAccessor.getIncidentService()).thenReturn(incidentService);
        doReturn(transactionService).when(tenantAccessor).getTransactionService();

        txBonitawork = spy(new ProcessInstanceContextWork(wrappedWork, PROCESS_INSTANCE_ID));
        doReturn("The description").when(txBonitawork).getDescription();

        doReturn(false).when(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.TRACE);
    }

    @Test
    public void testWork() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void getDescription() {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", txBonitawork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitawork.getRecoveryProcedure());
    }

    @Test
    public void handleFailureWithProcessInstanceId() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + ROOT_PROCESS_INSTANCE_ID));
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void handleFailureWithProcessInstanceAndRootIds() throws Throwable {
        txBonitawork = spy(new ProcessInstanceContextWork(wrappedWork, PROCESS_INSTANCE_ID, 5));
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + 5));
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, txBonitawork.getTenantId());
    }

    @Test
    public void setTenantId() {
        txBonitawork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void getWrappedWork() {
        assertEquals(wrappedWork, txBonitawork.getWrappedWork());
    }

    @Test
    public void testToString() {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", txBonitawork.toString());
    }

}
