/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.flownode;

import java.util.List;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface FlowNodeDefinition extends BaseElement, DescriptionElement {

    /**
     * Gets the outgoing transitions of the activity.
     *
     * @return the outgoing transitions of the activity
     */
    List<TransitionDefinition> getOutgoingTransitions();

    TransitionDefinition getDefaultTransition();

    /**
     * Gets the incoming transitions of the activity.
     *
     * @return the incoming transitions of the activity
     */
    List<TransitionDefinition> getIncomingTransitions();

    List<ConnectorDefinition> getConnectors();

    void addConnector(ConnectorDefinition connectorDefinition);

    Expression getDisplayDescription();

    Expression getDisplayName();

    Expression getDisplayDescriptionAfterCompletion();

}
