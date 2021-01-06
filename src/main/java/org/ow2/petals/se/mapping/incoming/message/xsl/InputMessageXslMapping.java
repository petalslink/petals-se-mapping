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
package org.ow2.petals.se.mapping.incoming.message.xsl;

import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.ow2.petals.se.mapping.incoming.message.MappingInputMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;

/**
 * The XSL mapping of a message extracted from WSDL according to SE Mapping annotations. This mapping is used to
 * transform a message using a XSL transformation.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class InputMessageXslMapping extends AbstractMessageXslMapping implements MappingInputMessage {

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL operation containing the current annotations
     * @param xslFileName
     *            The XSL style-sheet file name. Not {@code null} and not empty.
     * @param suRootPath
     *            The SU root path. Can not be {@code null}.
     * @param logErrorListener
     *            The SU XSL error listener, used to resolve and log line numbers when errors are found into the XSL
     *            style-sheet.
     * @param logger
     */
    public InputMessageXslMapping(final QName wsdlOperationName, final String wsdlMessageName, final String xslFileName,
            final String suRootPath, final LogErrorListener logErrorListener, final Logger logger) {
        super(wsdlOperationName, wsdlMessageName, xslFileName, suRootPath, logErrorListener, logger);
    }

    @Override
    public void transform(final Source technicalResponse, final Result businessResponse,
            final Properties componentProperties) throws TransformException {
        this.doTransform(technicalResponse, businessResponse, null, componentProperties);
    }

}
