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
package org.ow2.petals.se.mapping.incoming.message.xsl;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.ow2.petals.component.framework.api.configuration.SuConfigurationParameters;
import org.ow2.petals.se.mapping.incoming.message.AbstractMappingMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.TransformException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.InvalidXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnbuildableXslFileMappingException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.UnexistingXslMappingException;

/**
 * The XSL mapping of a message extracted from WSDL according to SE Mapping annotations. This mapping is used to
 * transform a message using a XSL transformation.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MessageXslMapping extends AbstractMappingMessage {

    /**
     * The SU root path
     */
    private final String suRootPath;

    /**
     * The XSL style-sheet file name
     */
    private String xslFileName;

    /**
     * SE Mapping extensions of the JBI descriptor of the current provider in which this operation takes place.
     */
    private final SuConfigurationParameters extensions;

    /**
     * The SU XSL error listener, used to resolve and log line numbers when errors are found into the XSL style-sheet.
     */
    private final LogErrorListener logErrorListener;

    /**
     * The XSL style-sheet compiled.
     */
    private Templates xsl = null;

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL operation containing the current annotations
     * @param xslFileName
     *            The XSL style-sheet file name. Not {@code null} and not empty.
     * @param suRootPath
     *            The SU root path. Can not be {@code null}.
     * @param extensions
     *            SE Mapping extensions of the JBI descriptor of the current provider in which this operation takes
     *            place. Not {@code null}.
     * @param logErrorListener
     *            The SU XSL error listener, used to resolve and log line numbers when errors are found into the XSL
     *            style-sheet.
     */
    public MessageXslMapping(final QName wsdlOperationName, final String wsdlMessageName, final String xslFileName,
            final String suRootPath,
            final SuConfigurationParameters extensions, final LogErrorListener logErrorListener) {
        super(wsdlOperationName, wsdlMessageName);
        this.xslFileName = xslFileName;
        this.extensions = extensions;
        this.suRootPath = suRootPath;
        this.logErrorListener = logErrorListener;
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

    /**
     * @return The XSL style-sheet compiled.
     */
    public Templates getXsl() {
        return this.xsl;
    }

    @Override
    public void transform(final Source source, final Result target) throws TransformException {
        try {
            this.xsl.newTransformer().transform(source, target);
        } catch (final TransformerException e) {
            throw new TransformException(e);
        }
    }

    @Override
    public void log(final Logger logger, final Level logLevel) {
        logger.log(logLevel, "    - xsl file = " + new File(this.suRootPath, this.xslFileName).getAbsolutePath());
    }

}
