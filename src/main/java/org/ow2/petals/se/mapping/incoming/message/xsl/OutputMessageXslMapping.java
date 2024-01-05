/**
 * Copyright (c) 2016-2024 Linagora
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

import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.ow2.petals.component.framework.api.util.Placeholders;
import org.ow2.petals.se.mapping.incoming.condition.MappingOutputCondition;
import org.ow2.petals.se.mapping.incoming.message.MappingOutputMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;
import org.w3c.dom.Document;

/**
 * The XSL mapping of an output message extracted from WSDL according to SE Mapping annotations. This mapping is used to
 * transform a message using a XSL transformation.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class OutputMessageXslMapping extends AbstractMessageXslMapping implements MappingOutputMessage {

    private static final String XSL_PARAM_OUTPUT_NAMESPACE = "http://petals.ow2.org/se/mapping/xsl/param/output/1.0";

    private static final QName XSL_PARAM_OUTPUT_INCOMING_REQUEST = new QName(XSL_PARAM_OUTPUT_NAMESPACE,
            "incoming-request");

    private static final String XSL_PARAM_OUTPUT_INCOMING_REQUEST_STR = XSL_PARAM_OUTPUT_INCOMING_REQUEST.toString();

    private final MappingOutputCondition outputCondition;

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL operation containing the current annotations
     * @param xslFileName
     *            The XSL style-sheet file name. Not {@code null} and not empty.
     * @param outputCondition
     *            The condition to define is a fault or a 'OUT' message must be returned
     * @param suRootPath
     *            The SU root path. Can not be {@code null}.
     * @param logErrorListener
     *            The SU XSL error listener, used to resolve and log line numbers when errors are found into the XSL
     *            style-sheet.
     * @param logger
     */
    public OutputMessageXslMapping(final QName wsdlOperationName, final String wsdlMessageName,
            final String xslFileName, final MappingOutputCondition outputCondition, final String suRootPath,
            final LogErrorListener logErrorListener, final Logger logger) {
        super(wsdlOperationName, wsdlMessageName, xslFileName, suRootPath, logErrorListener, logger);
        this.outputCondition = outputCondition;
    }

    @Override
    public boolean shouldReturnFault(final Document technicalDocument) throws InvalidAnnotationForMessageException {
        return this.outputCondition.shouldReturnFault(technicalDocument);
    }

    @Override
    public MappingOutputCondition getCondition() {
        return this.outputCondition;
    }

    @Override
    protected void setXslParameters(final Transformer transformer, final Document incomingSource,
            final Placeholders componentProperties) {
        super.setXslParameters(transformer, incomingSource, componentProperties);
        transformer.setParameter(XSL_PARAM_OUTPUT_INCOMING_REQUEST_STR, incomingSource);
    }

    @Override
    public void transform(final Source technicalResponse, final Result businessResponse, final Document businessRequest,
            final Placeholders componentProperties) throws TransformException {
        this.doTransform(technicalResponse, businessResponse, businessRequest, componentProperties);
    }
}
