/**
 * Copyright (c) 2016-2021 Linagora
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

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.ow2.petals.component.framework.api.util.Placeholders;
import org.ow2.petals.se.mapping.incoming.condition.MappingOutputCondition;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;
import org.w3c.dom.Document;

/**
 * A mapping transformation to apply on an output message of a WSDL operation, extracted from WDSL according to SE
 * Mapping annotations
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface MappingOutputMessage extends AbsMappingMessage {

    /**
     * Transform an XML message
     * 
     * @param technicalResponse
     *            The XML {@code Source} associated to the invoked service response to transform
     * @param businessResponse
     *            The {@code Result} associated to the invoked service response transformed.
     * @param businessRequest
     *            The XML {@code Document} associated to the incoming request
     * @param componentProperties
     *            Placeholders defined in the property file configured at component level
     * @throws TransformException
     *             An error occurs during transformation
     */
    public void transform(final Source technicalResponse, final Result businessResponse, final Document businessRequest,
            final Placeholders componentProperties) throws TransformException;

    /**
     * Compute if the returned output must be a fault or a normal OUT message.
     * 
     * @param technicalDocument
     *            The XML {@code Document} from which the output nature (fault or normal OUT message) is computed
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during computation
     */
    public boolean shouldReturnFault(final Document technicalDocument) throws InvalidAnnotationForMessageException;

    /**
     * @return The {@link MappingOutputCondition} associated to this output message
     */
    public MappingOutputCondition getCondition();
}
