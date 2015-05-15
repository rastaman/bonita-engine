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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

/**
 * @author Matthieu Chaffotte
 */
public class CatchSignalEventTriggerDefinitionImpl extends SignalEventTriggerDefinitionImpl implements CatchSignalEventTriggerDefinition {

    private static final long serialVersionUID = -933991836111559400L;

    public CatchSignalEventTriggerDefinitionImpl(final String signalName) {
        super(signalName);
    }

    @Override
    public <T> T accept(ModelFinderVisitor<T> visitor, long modelId) {
        // nothing to do here:
        return null;
    }
}
