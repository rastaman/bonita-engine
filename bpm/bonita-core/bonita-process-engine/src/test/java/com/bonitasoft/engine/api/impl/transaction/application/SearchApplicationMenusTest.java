/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.convertor.ApplicationMenuConvertor;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;

@RunWith(MockitoJUnitRunner.class)
public class SearchApplicationMenusTest {

    private static final int START_INDEX = 0;

    private static final int MAX_RESULTS = 10;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationMenuConvertor convertor;

    @InjectMocks
    private SearchApplicationMenus search;

    @Test
    public void executeCount_should_return_result_of_applicationService_getNumberOfApplicationMenus() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, MAX_RESULTS);
        given(applicationService.getNumberOfApplicationMenus(options)).willReturn(5L);

        //when
        final long count = search.executeCount(options);

        //then
        assertThat(count).isEqualTo(5);
    }

    @Test(expected = SBonitaReadException.class)
    public void executeCount_should_throw_SearchException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, MAX_RESULTS);
        given(applicationService.getNumberOfApplicationMenus(options)).willThrow(new SBonitaReadException(""));

        //when
        search.executeCount(options);

        //then exception
    }

    @Test
    public void executeSearch_should_return_result_of_applicationService_searchApplicationMenus() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, MAX_RESULTS);
        final List<SApplicationMenu> menus = new ArrayList<SApplicationMenu>(1);
        menus.add(mock(SApplicationMenu.class));
        given(applicationService.searchApplicationMenus(options)).willReturn(menus);

        //when
        final List<SApplicationMenu> retrievedMenus = search.executeSearch(options);

        //then
        assertThat(retrievedMenus).isEqualTo(menus);
    }

    @Test
    public void convertToClientObjects_should_return_result_of_convertor() throws Exception {
        //given
        final List<SApplicationMenu> sMenus = Arrays.asList(mock(SApplicationMenu.class));
        final List<ApplicationMenu> menus = Arrays.asList(mock(ApplicationMenu.class));
        given(convertor.toApplicationMenu(sMenus)).willReturn(menus);

        //when
        final List<ApplicationMenu> clientObjects = search.convertToClientObjects(sMenus);

        //then
        assertThat(clientObjects).isNotNull();
        assertThat(clientObjects).isEqualTo(menus);
    }

}