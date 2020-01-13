/**
 * Copyright (c) 2016-2020 Linagora
 * 
 * This program/library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This program/library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program/library; If not, see http://www.gnu.org/licenses/
 * for the GNU Lesser General Public License version 2.1.
 */
package org.ow2.petals.se.mapping.incoming.condition;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.w3c.dom.Document;

/**
 * A mapping condition defining if an output message must be returned as fault or as 'OUT' message
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface MappingOutputCondition {

    /**
     * Compute if the returned output must be a fault or a normal OUT message.
     * 
     * @param technicalSource
     *            The XML {@code Document} from which the output nature (fault or normal OUT message) is computed
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during computation
     */
    public boolean shouldReturnFault(final Document technicalSource) throws InvalidAnnotationForMessageException;

    /**
     * Verify that annotation read from the WSDL are valid for the condition, otherwise the exception
     * {@link InvalidAnnotationForMessageException} is thrown.
     * 
     * @throws InvalidAnnotationForMessageException
     *             The annotated operation message is incoherent.
     */
    public void verifyAnnotationCoherence() throws InvalidAnnotationForMessageException;

    /**
     * @return The WSDL binding operation name containing the current annotations
     */
    public QName getWsdlOperationName();

    /**
     * @return The WSDL binding operation message name containing the current annotations
     */
    public String getWsdlMessageName();

    /**
     * Log the mapping output condition characteristics
     */
    public void log();
}
