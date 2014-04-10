/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.operation;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public enum OperatorType {

    // assign new value to process / task variable:
    ASSIGNMENT,

    JAVA_METHOD,

    XPATH_UPDATE_QUERY,

    DOCUMENT_CREATE_UPDATE,

    STRING_INDEX,

    CREATE_BUSINESS_DATA,

    // update a Business Data attribute by calling setter method on it:
    BUSINESS_DATA_JAVA_SETTER;
}