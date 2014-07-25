package com.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SApplication;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLogService;

    @Mock
    private SApplication application;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Before
    public void setUp() throws Exception {
        when(queriableLogService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(true);
        given(application.getId()).willReturn(10L);
    }

    @Test
    public void createApplication_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                .setObject(application).done();
        final InsertRecord record = new InsertRecord(application);

        //when
        final SApplication createdApplication = applicationService.createApplication(application);

        //then
        assertThat(createdApplication).isEqualTo(application);
        verify(recorder, times(1)).recordInsert(record, insertEvent);
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplication_should_throw_SObjectCreationException_when_record_insert_throws_Exception() throws Exception {
        //given
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        //when
        applicationService.createApplication(application);

        //then exception
    }

    @Test
    public void createApplication_should_throw_SObjectAlreadyExistsException_when_an_application_with_the_same_name_already_exists() throws Exception {
        //given
        final String name = "app";
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByName", Collections.<String, Object> singletonMap("name",
                name), SApplication.class))).willReturn(application);

        final SApplication newApp = mock(SApplication.class);
        given(newApp.getName()).willReturn(name);

        //when
        try {
            applicationService.createApplication(newApp);
            fail("Exception expected");
        } catch (final SObjectAlreadyExistsException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("An application already exists with name '" + name + "'.");
            verify(recorder, never()).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        }

    }

    @Test
    public void getApplication_should_return_result_of_persitence_service_selectById() throws Exception {
        //given
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, 10L))).willReturn(application);

        //when
        final SApplication retrievedApp = applicationService.getApplication(10L);

        //then
        assertThat(retrievedApp).isEqualTo(application);
    }

    @Test
    public void getApplication_should_throw_SObjectNotFoundException_when_persitence_service_returns_null() throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId))).willReturn(null);

        //when
        try {
            applicationService.getApplication(applicationId);
            fail("Exception expected");
        } catch (final SObjectNotFoundException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("No application found with id '" + applicationId + "'.");
        }

    }

    @Test
    public void deleteApplication_should_call_record_delete() throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId))).willReturn(application);

        //when
        applicationService.deleteApplication(applicationId);

        //then
        final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION)
                .setObject(application).done();
        verify(recorder, times(1)).recordDelete(new DeleteRecord(application), event);
    }

}
