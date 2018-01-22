/**
 * Copyright (c) 2016-2018 Linagora
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
package org.ow2.petals.se.mapping.incoming.message;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;

/**
 * An abstract mapping transformation to apply on message
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface AbsMappingMessage {

    /**
     * Verify that annotation read from the WSDL are valid for the message, otherwise the exception
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
     * Log the mapping message characteristics
     */
    public void log();
}
