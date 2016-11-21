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
package org.ow2.petals.se.mapping.incoming;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.ow2.petals.component.framework.api.configuration.SuConfigurationParameters;
import org.ow2.petals.component.framework.jbidescriptor.generated.Consumes;
import org.ow2.petals.component.framework.util.XMLUtil;
import org.ow2.petals.se.mapping.incoming.exception.InvalidAnnotationException;
import org.ow2.petals.se.mapping.incoming.exception.NoMappingOperationException;
import org.ow2.petals.se.mapping.incoming.exception.NoWsdlBindingException;
import org.ow2.petals.se.mapping.incoming.message.MappingMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.xsl.LogErrorListener;
import org.ow2.petals.se.mapping.incoming.message.xsl.MessageXslMapping;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.MultipleMappingXslDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoMappingXslDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoTransformationDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;
import org.ow2.petals.se.mapping.incoming.operation.exception.InvalidAnnotationForOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.MultipleServiceProviderOperationDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.exception.NoInputOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.NoServiceProviderOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.SeveralInputOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.SeveralOutputOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The parser to read annotations included into the WSDL
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class AnnotatedWsdlParser {

    public static final String SCHEMA_WSDL = "http://schemas.xmlsoap.org/wsdl/";

    private static final String SCHEMA_MAPPING_ANNOTATIONS = "http://petals.ow2.org/se/mapping/annotations/1.0";

    /**
     * Local part of the annotation tag associated to the definition of the service provider operation to invoke
     */
    private static final String MAPPING_ANNOTATION_OPERATION = "service-provider-operation";

    /**
     * Local part of the annotation tag associated to the XSL style-sheet of a XSL transformation
     */
    private static final String XSL = "xsl";

    private final Logger logger;

    /**
     * List of errors encountered during a parsing. All errors are reseted when the parsing starts
     */
    private final List<InvalidAnnotationException> encounteredErrors = new ArrayList<>();

    public AnnotatedWsdlParser(final Logger logger) {
        this.logger = logger;
    }

    /**
     * <p>
     * Parse the annotated WSDL.
     * </p>
     * <p>
     * If an annotated operation is invalid because of error using annotations, the operation is skipped.
     * </p>
     * 
     * @param annotatedWsdl
     *            The WSDL to parse containing BC Mail annotations
     * @param extensions
     *            BC Mail extensions of the JBI descriptor of the current provider. Can be {@code null} if the WSDL is
     *            correctly annotated and it is not needed to extract fields from the SU JBI descriptor.
     * @param suRootPath
     *            The root directory of the service unit
     * @param suName
     *            Name of the current service unit
     * @param serviceProvider
     *            The service to invoke declared in the service unit
     * @return The mapping operation of the WSDL.
     */
    public List<MappingOperation> parse(final Document annotatedWsdl, final SuConfigurationParameters extensions,
            final String suRootPath, final String suName, final Consumes serviceProvider) {

        this.encounteredErrors.clear();

        final LogErrorListener xslLogErrorListener = new LogErrorListener(this.logger, suName);

        final List<MappingOperation> mappingOperations = new ArrayList<>();

        annotatedWsdl.getDocumentElement().normalize();
        final String targetNamespace = annotatedWsdl.getDocumentElement().getAttribute("targetNamespace");

        // Get the node "wsdl:binding"
        final NodeList bindings = annotatedWsdl.getElementsByTagNameNS(SCHEMA_WSDL, "binding");
        if (bindings.getLength() != 0) {
            final Node binding = bindings.item(0);
            // Get the list of nodes "wsdl:operation"
            final NodeList wsdlOperations = ((Element) binding).getElementsByTagNameNS(SCHEMA_WSDL, "operation");
            for (int j = 0; j < wsdlOperations.getLength(); j++) {
                final Node wsdlOperation = wsdlOperations.item(j);
                final QName wsdlOperationName = new QName(targetNamespace,
                        ((Element) wsdlOperation).getAttribute("name"));

                try {

                    // Get the node "mapping:service-provider-operation"
                    final NodeList serviceProviderOperationNodes = ((Element) wsdlOperation)
                            .getElementsByTagNameNS(SCHEMA_MAPPING_ANNOTATIONS, MAPPING_ANNOTATION_OPERATION);
                    final QName serviceProviderOperation;
                    if (serviceProviderOperationNodes.getLength() > 1) {
                        throw new MultipleServiceProviderOperationDefinedException(wsdlOperationName);
                    } else if (serviceProviderOperationNodes.getLength() == 0) {
                        throw new NoServiceProviderOperationException(wsdlOperationName);
                    } else {
                        serviceProviderOperation = XMLUtil
                                .getElementValueAsQName(serviceProviderOperationNodes.item(0));
                        if (serviceProviderOperation == null) {
                            throw new NoServiceProviderOperationException(wsdlOperationName);
                        }
                    }

                    // Get the list of nodes "wsdl:input"
                    final NodeList wsdlInputs = ((Element) wsdlOperation).getElementsByTagNameNS(SCHEMA_WSDL, "input");
                    MappingMessage inputMessageMapping = null;
                    if (wsdlInputs.getLength() > 1) {
                        // An operation can not have more than one input message
                        throw new SeveralInputOperationException(wsdlOperationName);
                    } else if (wsdlInputs.getLength() == 1) {
                        inputMessageMapping = this.parseMessage(wsdlOperationName, wsdlInputs.item(0), extensions,
                                suRootPath, xslLogErrorListener);
                    } else {
                        // An operation required one input message
                        throw new NoInputOperationException(wsdlOperationName);
                    }

                    // Get the list of nodes "wsdl:output"
                    final NodeList wsdlOutputs = ((Element) wsdlOperation).getElementsByTagNameNS(SCHEMA_WSDL,
                            "output");
                    MappingMessage outputMessageMapping = null;
                    if (wsdlOutputs.getLength() > 1) {
                        // An operation can not have more than one output message
                        throw new SeveralOutputOperationException(wsdlOperationName);
                    } else if (wsdlOutputs.getLength() == 1) {
                        outputMessageMapping = this.parseMessage(wsdlOperationName, wsdlOutputs.item(0), extensions,
                                suRootPath, xslLogErrorListener);
                    }

                    // Get the list of nodes "wsdl:fault"
                    final NodeList wsdlFaults = ((Element) wsdlOperation).getElementsByTagNameNS(SCHEMA_WSDL, "fault");
                    final List<MappingMessage> faultMessageMappings = new ArrayList<>();
                    for (int i = 0; i < wsdlFaults.getLength(); i++) {
                        final MappingMessage annotatedFaultMsg = this.parseMessage(wsdlOperationName,
                                wsdlFaults.item(i), extensions, suRootPath, xslLogErrorListener);
                        if (annotatedFaultMsg != null) {
                            faultMessageMappings.add(annotatedFaultMsg);
                        }
                    }

                    mappingOperations
                            .add(new MappingOperation(wsdlOperationName, inputMessageMapping, outputMessageMapping,
                                    faultMessageMappings, serviceProvider, serviceProviderOperation, this.logger));

                } catch (final InvalidAnnotationForOperationException e) {
                    this.encounteredErrors.add(e);
                }
            }
        } else {
            this.encounteredErrors.add(new NoWsdlBindingException());
        }

        if (mappingOperations.isEmpty()) {
            this.encounteredErrors.add(new NoMappingOperationException());
        }

        return mappingOperations;

    }

    /**
     * Parse a WSDL binding operation message (input, output or fault)
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessage
     *            The binding operation message to parse
     * @param extensions
     *            BC Mail extensions of the JBI descriptor of the current provider. Not {@code null}
     * @param suRootPath
     *            The root directory of the service unit
     * @param xslLogErrorListener
     *            The SU XSL error listener
     * @return The annotated operation associated to the WSDL binding operation.
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding operation message
     */
    private MappingMessage parseMessage(final QName wsdlOperationName, final Node wsdlMessage,
            final SuConfigurationParameters extensions, final String suRootPath,
            final LogErrorListener xslLogErrorListener) throws InvalidAnnotationForMessageException {

        // The WSDL message name is extracted from the node. We use the value of the attribute 'name' if exists,
        // otherwise the XML tag name is used.
        final String wsdlMessageName;
        final String wsdlMessageNameByAttr = ((Element) wsdlMessage).getAttribute("name");
        if (wsdlMessageNameByAttr == null || wsdlMessageNameByAttr.isEmpty()) {
            wsdlMessageName = wsdlMessage.getLocalName();
        } else {
            wsdlMessageName = wsdlMessageNameByAttr;
        }

        // Get the node "mapping:xsl"
        final NodeList mappingXslNodes = ((Element) wsdlMessage).getElementsByTagNameNS(SCHEMA_MAPPING_ANNOTATIONS,
                XSL);
        final String xslFile;
        if (mappingXslNodes.getLength() > 1) {
            throw new MultipleMappingXslDefinedException(wsdlOperationName, wsdlMessageName);
        } else if (mappingXslNodes.getLength() == 0) {
            xslFile = null;
        } else {
            xslFile = mappingXslNodes.item(0).getTextContent();
        }

        if (xslFile != null) {
            if (xslFile.isEmpty()) {
                throw new NoMappingXslDefinedException(wsdlOperationName, wsdlMessageName);
            }

            // Create the annotated operation from annotations read into the WSDL
            final MappingMessage annotatedOperation = new MessageXslMapping(wsdlOperationName, wsdlMessageName, xslFile,
                    suRootPath, extensions, xslLogErrorListener);

            // Check the coherence of the annotated operation (ie. coherence of annotations of the operation
            // against content of the JBI descriptor)
            annotatedOperation.verifyAnnotationCoherence();

            return annotatedOperation;
        } else {
            // No annotation exist in the WSDL
            throw new NoTransformationDefinedException(wsdlOperationName, wsdlMessageName);
        }
    }

    /**
     * Get the errors encountered during the previous parsing. Not thread-safe with the parsing itself.
     * 
     * @return The errors encountered during the previous parsing
     */
    public List<InvalidAnnotationException> getEncounteredErrors() {
        return this.encounteredErrors;
    }

}
