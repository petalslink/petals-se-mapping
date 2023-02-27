/**
 * Copyright (c) 2016-2023 Linagora
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
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;

/**
 * A mapping transformation to apply on an input message of a WSDL operation, extracted from WDSL according to SE
 * Mapping annotations
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface MappingInputMessage extends AbsMappingMessage {

    /**
     * Transform an XML message
     * 
     * @param businessRequest
     *            The XML {@code Source} associated to the incoming request to transform
     * @param technicalRequest
     *            The {@code Result} associated to the incoming request transformed.
     * @param componentProperties
     *            Placeholders defined in the property file configured at component level
     * @throws TransformException
     *             An error occurs during transformation
     */
    public void transform(final Source businessRequest, final Result technicalRequest,
            final Placeholders componentProperties) throws TransformException;
}
