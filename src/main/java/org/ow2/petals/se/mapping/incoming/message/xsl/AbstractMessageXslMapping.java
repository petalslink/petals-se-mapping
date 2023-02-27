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
package org.ow2.petals.se.mapping.incoming.message.xsl;

import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.ow2.petals.component.framework.api.util.Placeholders;
import org.ow2.petals.se.mapping.incoming.message.AbsMappingMessage;
import org.ow2.petals.se.mapping.incoming.message.AbstractMappingMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.InvalidXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnbuildableXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnexistingXslMappingException;
import org.w3c.dom.Document;

/**
 * The XSL mapping of a message extracted from WSDL according to SE Mapping annotations. This mapping is used to
 * transform a message using a XSL transformation.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public abstract class AbstractMessageXslMapping extends AbstractMappingMessage implements AbsMappingMessage {

    private static final String XSL_PARAM_PROPERTIES_NAMESPACE = "http://petals.ow2.org/se/mapping/xsl/param/1.0";

    /**
     * The SU root path
     */
    private final String suRootPath;

    /**
     * The XSL style-sheet file name
     */
    private String xslFileName;

    /**
     * The SU XSL error listener, used to resolve and log line numbers when errors are found into the XSL style-sheet.
     */
    private final LogErrorListener logErrorListener;

    /**
     * The XSL style-sheet compiled.
     */
    private Templates xsl = null;

    private final Logger logger;

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
    public AbstractMessageXslMapping(final QName wsdlOperationName, final String wsdlMessageName,
            final String xslFileName, final String suRootPath, final LogErrorListener logErrorListener,
            final Logger logger) {
        super(wsdlOperationName, wsdlMessageName);
        this.xslFileName = xslFileName;
        this.suRootPath = suRootPath;
        this.logErrorListener = logErrorListener;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * 
     * During coherence checking, we can update not initialized field with the one available in SU JBI descriptor
     * extensions
     */
    @Override
    public void verifyAnnotationCoherence() throws InvalidAnnotationForMessageException {

        // The XSL style-sheet file must exist
        final File xslFile = new File(this.suRootPath, this.xslFileName);
        if (!xslFile.exists()) {
            throw new UnexistingXslMappingException(this.wsdlOperationName, this.wsdlMessageName, xslFile);
        }

        // The XSL style-sheet file must be a regular file
        if (!xslFile.isFile()) {
            throw new InvalidXslFileMappingException(this.wsdlOperationName, this.wsdlMessageName, xslFile);
        }

        // The XSL style-sheet file must be a buildable XSL
        final Source xslSource = new StreamSource(xslFile);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setErrorListener(this.logErrorListener);
        try {
            this.xsl = transformerFactory.newTemplates(xslSource);
        } catch (final TransformerConfigurationException e) {
            throw new UnbuildableXslFileMappingException(this.wsdlOperationName, this.wsdlMessageName, xslFile, e);
        }

    }

    @Override
    public void log() {
        this.logger.config("    - xsl file = " + new File(this.suRootPath, this.xslFileName).getAbsolutePath());
    }

    /**
     * <p>
     * Set XSL global parameters.
     * </p>
     * <p>
     * This implementation add all properties of the component property file.
     * </p>
     * 
     * @param transformer
     *            The {@link Transformer} for which parameters must be set. Not {@code null}
     * @param incomingSource
     *            The initial incoming request
     * @param componentProperties
     *            Placeholders defined in the property file configured at component level
     */
    protected void setXslParameters(final Transformer transformer, final Document incomingSource,
            final Placeholders componentProperties) {

        if (componentProperties != null) {
            for (final Entry<Object, Object> property : componentProperties.toProperties().entrySet()) {
                transformer.setParameter(
                        new QName(XSL_PARAM_PROPERTIES_NAMESPACE, property.getKey().toString()).toString(),
                        property.getValue().toString());
            }
        }
    }

    /**
     * Transform an XML message
     * 
     * @param source
     *            The XML {@code Source} to transform
     * @param target
     *            The {@code Result} transformed.
     * @param businessRequest
     *            The XML {@code Document} associated to the incoming request
     * @param componentProperties
     *            Placeholders defined in the property file configured at component level
     * @throws TransformException
     *             An error occurs during transformation
     */
    protected void doTransform(final Source source, final Result target, final Document businessRequest,
            final Placeholders componentProperties) throws TransformException {
        try {
            final Transformer transformer = this.xsl.newTransformer();

            this.setXslParameters(transformer, businessRequest, componentProperties);

            transformer.transform(source, target);
        } catch (final TransformerException e) {
            throw new TransformException(e);
        }
    }
}
