/**
 * Copyright (c) 2016 Linagora
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;

/**
 * A mapping transformation to apply on a message of a WSDL operation, extracted from WDSL according to SE Mapping
 * annotations
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface MappingMessage {

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
     * Transform an XML message
     * 
     * @param source
     *            The XML {@code Source} to transform
     * @param target
     *            The {@code Result} of transforming the given source.
     * @throws TransformException
     *             An error occurs during transformation
     */
    public void transform(final Source source, final Result target) throws TransformException;

    /**
     * Log the mapping message characteristics
     * 
     * @param logger
     * @param logLevel
     */
    public void log(final Logger logger, final Level logLevel);
}
