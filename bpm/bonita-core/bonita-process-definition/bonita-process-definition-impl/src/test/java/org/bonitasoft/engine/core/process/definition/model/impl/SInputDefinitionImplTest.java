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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Test
    public void constructor_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_description_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION, true);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should be multiple").isTrue();

    }

    @Test
    public void constructor_with_name_and_description() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.getName()).as("should get name").isEqualTo(NAME);
        assertThat(sInputDefinitionImpl.getDescription()).as("should get name").isEqualTo(DESCRIPTION);

    }

}
