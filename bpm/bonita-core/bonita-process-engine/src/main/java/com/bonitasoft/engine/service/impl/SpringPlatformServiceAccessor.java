/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SpringPlatformServiceAccessor extends org.bonitasoft.engine.service.impl.SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private SearchPlatformEntitiesDescriptor searchPlatformEntitiesDescriptor;

    @Override
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        if (searchPlatformEntitiesDescriptor == null) {
            searchPlatformEntitiesDescriptor = SpringPlatformFileSystemBeanAccessor.getService(SearchPlatformEntitiesDescriptor.class);
        }
        return searchPlatformEntitiesDescriptor;
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        final TenantServiceAccessor instance = TenantServiceSingleton.getInstance(tenantId);
        return instance;
    }

}
