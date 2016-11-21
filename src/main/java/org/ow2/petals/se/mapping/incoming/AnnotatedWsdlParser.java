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
import org.ow2.petals.se.mapping.incoming.condition.MappingOutputCondition;
import org.ow2.petals.se.mapping.incoming.condition.bool.BooleanOutputCondition;
import org.ow2.petals.se.mapping.incoming.condition.exception.MultipleOutputConditionDefinedException;
import org.ow2.petals.se.mapping.incoming.condition.xpath.XPathOutputCondition;
import org.ow2.petals.se.mapping.incoming.condition.xpath.exception.EmptyXPathExpressionException;
import org.ow2.petals.se.mapping.incoming.exception.InvalidAnnotationException;
import org.ow2.petals.se.mapping.incoming.exception.NoMappingOperationException;
import org.ow2.petals.se.mapping.incoming.exception.NoWsdlBindingException;
import org.ow2.petals.se.mapping.incoming.message.MappingInputMessage;
import org.ow2.petals.se.mapping.incoming.message.MappingOutputMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.message.exception.NoOutputConditionDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.InputMessageXslMapping;
import org.ow2.petals.se.mapping.incoming.message.xsl.LogErrorListener;
import org.ow2.petals.se.mapping.incoming.message.xsl.OutputMessageXslMapping;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.EmptyMappingXslDefinedException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoInputXslException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoOutputXslException;
import org.ow2.petals.se.mapping.incoming.message.xsl.exception.NoTransformationDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;
import org.ow2.petals.se.mapping.incoming.operation.exception.InvalidAnnotationForOperationException;
import org.ow2.petals.se.mapping.incoming.operation.exception.MultipleInputTransfoDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.exception.MultipleOutputTransfoDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.exception.MultipleServiceProviderOperationDefinedException;
import org.ow2.petals.se.mapping.incoming.operation.exception.NoServiceProviderOperationException;
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
     * Local part of the annotation tag associated to the definition of the transformation to apply on request
     */
    private static final String MAPPING_ANNOTATION_INPUT_TRANSFO = "input-transformation";

    /**
     * Local part of the annotation tag associated to the definition of the transformation to apply on response
     */
    private static final String MAPPING_ANNOTATION_OUTPUT_TRANSFO = "output-transformation";

    /**
     * Local part of the annotation tag associated to the definition of the condition to return a fault or an OUT
     * message
     */
    private static final String MAPPING_ANNOTATION_OUTPUT_CONDITION = "should-return-fault";

    /**
     * Local part of the attribute of {@link #MAPPING_ANNOTATION_INPUT_TRANSFO} or
     * {@link #MAPPING_ANNOTATION_OUTPUT_TRANSFO} containing the filename of the XSL defining the transformation to do
     */
    private static final String MAPPING_ANNOTATION_XSL = "xsl";

    /**
     * Local part of the attribute of {@link #MAPPING_ANNOTATION_OUTPUT_CONDITION} defining that the expression defining
     * if a fault or OUT message must be returned is expressed using XPath
     */
    private static final String MAPPING_ANNOTATION_EXPR_XPATH = "as-xpath-expr";

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
        final NodeList wsdlBindings = annotatedWsdl.getElementsByTagNameNS(SCHEMA_WSDL, "binding");
        if (wsdlBindings.getLength() != 0) {
            final Node wsdlBinding = wsdlBindings.item(0);
            // Get the list of nodes "wsdl:operation"
            final NodeList wsdlOperations = ((Element) wsdlBinding).getElementsByTagNameNS(SCHEMA_WSDL, "operation");
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

                    // Get the node "mapping:input-transformation"
                    final NodeList inputTransfoNodes = ((Element) wsdlOperation)
                            .getElementsByTagNameNS(SCHEMA_MAPPING_ANNOTATIONS, MAPPING_ANNOTATION_INPUT_TRANSFO);
                    final MappingInputMessage inputMessageMapping;
                    if (inputTransfoNodes.getLength() > 1) {
                        throw new MultipleInputTransfoDefinedException(wsdlOperationName);
                    } else if (inputTransfoNodes.getLength() == 0) {
                        throw new NoTransformationDefinedException(wsdlOperationName, MAPPING_ANNOTATION_INPUT_TRANSFO);
                    } else {
                        inputMessageMapping = this.parseInputMessage(wsdlOperationName, inputTransfoNodes.item(0),
                                extensions, suRootPath, xslLogErrorListener);
                    }

                    // Get the node "mapping:output-transformation"
                    final NodeList outputTransfoNodes = ((Element) wsdlOperation)
                            .getElementsByTagNameNS(SCHEMA_MAPPING_ANNOTATIONS, MAPPING_ANNOTATION_OUTPUT_TRANSFO);
                    final MappingOutputMessage outputMessageMapping;
                    if (outputTransfoNodes.getLength() > 1) {
                        throw new MultipleOutputTransfoDefinedException(wsdlOperationName);
                    } else if (outputTransfoNodes.getLength() == 0) {
                        // If no output and no fault is defined at port-type, it's not an error
                        // TODO: Check against the port-type instead of binding operation
                        if ((((Element) wsdlOperation).getElementsByTagNameNS(SCHEMA_WSDL, "output").getLength()
                                + ((Element) wsdlOperation).getElementsByTagNameNS(SCHEMA_WSDL, "fault")
                                        .getLength()) > 0) {
                            throw new NoOutputXslException(wsdlOperationName);
                        } else {
                            outputMessageMapping = null;
                        }
                    } else {
                        outputMessageMapping = this.parseOutputMessage(wsdlOperationName, outputTransfoNodes.item(0),
                                extensions, suRootPath, xslLogErrorListener);
                    }

                    mappingOperations.add(new MappingOperation(wsdlOperationName, inputMessageMapping,
                            outputMessageMapping, serviceProvider, serviceProviderOperation, this.logger));

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
     * Parse a WSDL binding annotation associated to an input message transformation
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessageAnnotation
     *            The WSDL binding annotation to parse
     * @param extensions
     *            BC Mail extensions of the JBI descriptor of the current provider. Not {@code null}
     * @param suRootPath
     *            The root directory of the service unit
     * @param xslLogErrorListener
     *            The SU XSL error listener
     * @return The annotated message transformation associated to the WSDL binding annotation.
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding annotation
     */
    private MappingInputMessage parseInputMessage(final QName wsdlOperationName, final Node wsdlMessageAnnotation,
            final SuConfigurationParameters extensions, final String suRootPath,
            final LogErrorListener xslLogErrorListener) throws InvalidAnnotationForMessageException {

        // Get the attribute "xsl" of the transformation node
        final String xslFileStr = AnnotatedWsdlParser.retrieveXslValue(wsdlOperationName, wsdlMessageAnnotation);

        if (xslFileStr != null) {
            // Create the annotated operation from annotations read into the WSDL
            final MappingInputMessage annotatedMappingMessage = new InputMessageXslMapping(wsdlOperationName,
                    wsdlMessageAnnotation.getLocalName(), xslFileStr, suRootPath, extensions, xslLogErrorListener,
                    this.logger);

            // Check the coherence of the annotated operation (ie. coherence of annotations of the operation
            // against content of the JBI descriptor)
            annotatedMappingMessage.verifyAnnotationCoherence();

            return annotatedMappingMessage;
        } else {
            // No annotation exist in the WSDL
            throw new NoInputXslException(wsdlOperationName, wsdlMessageAnnotation.getLocalName());
        }
    }

    /**
     * Parse a WSDL binding annotation associated to an output message transformation
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessageAnnotation
     *            The WSDL binding annotation to parse
     * @param extensions
     *            BC Mail extensions of the JBI descriptor of the current provider. Not {@code null}
     * @param suRootPath
     *            The root directory of the service unit
     * @param xslLogErrorListener
     *            The SU XSL error listener
     * @return The annotated message transformation associated to the WSDL binding annotation.
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding annotation
     */
    private MappingOutputMessage parseOutputMessage(final QName wsdlOperationName, final Node wsdlMessageAnnotation,
            final SuConfigurationParameters extensions, final String suRootPath,
            final LogErrorListener xslLogErrorListener) throws InvalidAnnotationForMessageException {

        // Get the attribute "xsl" of the transformation node to know if an XSL transformation must be applied
        final String xslFileStr = AnnotatedWsdlParser.retrieveXslValue(wsdlOperationName, wsdlMessageAnnotation);

        // Get the node "mapping:should-return-fault"
        final NodeList outputConditionNodes = ((Element) wsdlMessageAnnotation)
                .getElementsByTagNameNS(SCHEMA_MAPPING_ANNOTATIONS, MAPPING_ANNOTATION_OUTPUT_CONDITION);
        final MappingOutputCondition outputCondition;
        if (outputConditionNodes.getLength() > 1) {
            throw new MultipleOutputConditionDefinedException(wsdlOperationName, wsdlMessageAnnotation.getNodeName());
        } else if (outputConditionNodes.getLength() == 0) {
            outputCondition = new BooleanOutputCondition(wsdlOperationName, wsdlMessageAnnotation.getNodeName(),
                    this.logger);
        } else {
            outputCondition = this.parseOutputCondition(wsdlOperationName, outputConditionNodes.item(0), extensions);
        }

        if (xslFileStr != null) {
            // Create the annotated operation from annotations read into the WSDL
            final MappingOutputMessage annotatedMappingMessage = new OutputMessageXslMapping(wsdlOperationName,
                    wsdlMessageAnnotation.getLocalName(), xslFileStr, outputCondition, suRootPath, extensions,
                    xslLogErrorListener, this.logger);

            // Check the coherence of the annotated operation (ie. coherence of annotations of the operation
            // against content of the JBI descriptor)
            annotatedMappingMessage.verifyAnnotationCoherence();

            return annotatedMappingMessage;
        } else {
            // No annotation exist in the WSDL
            throw new NoTransformationDefinedException(wsdlOperationName, wsdlMessageAnnotation.getLocalName());
        }
    }

    /**
     * Parse a WSDL binding annotation associated to an output message condition
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessageAnnotation
     *            The WSDL binding annotation to parse
     * @param extensions
     *            BC Mail extensions of the JBI descriptor of the current provider. Not {@code null}
     * @return The annotated message condition associated to the WSDL binding annotation.
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding annotation
     */
    private MappingOutputCondition parseOutputCondition(final QName wsdlOperationName, final Node wsdlMessageAnnotation,
            final SuConfigurationParameters extensions) throws InvalidAnnotationForMessageException {

        // Get the attribute "as-xpath-expr" of the condition node to know if an XPath condition must be applied
        final Boolean xpathCond = AnnotatedWsdlParser.retrieveXpathExprFlag(wsdlOperationName, wsdlMessageAnnotation,
                wsdlMessageAnnotation.getLocalName());

        if (xpathCond != null && xpathCond.booleanValue()) {

            final String xpathExprStr = wsdlMessageAnnotation.getTextContent().trim();
            if (xpathExprStr == null || xpathExprStr.trim().isEmpty()) {
                throw new EmptyXPathExpressionException(wsdlOperationName, wsdlMessageAnnotation.getNodeName());
            } else {
                // Create the annotated output condition from annotations read into the WSDL
                final MappingOutputCondition outputCondition = new XPathOutputCondition(wsdlOperationName,
                        wsdlMessageAnnotation.getLocalName(), xpathExprStr, extensions, this.logger);

                // Check the coherence of the annotated operation (ie. coherence of annotations of the operation
                // against content of the JBI descriptor)
                outputCondition.verifyAnnotationCoherence();

                return outputCondition;
            }
        } else {
            // No annotation exist in the WSDL
            throw new NoOutputConditionDefinedException(wsdlOperationName, wsdlMessageAnnotation.getLocalName());
        }
    }

    /**
     * Retrieve the XSL filename of a message transformation using an XSL
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessageAnnotation
     *            The WSDL binding annotation to parse
     * @return An XSL file name
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding annotation
     */
    private static final String retrieveXslValue(final QName wsdlOperationName, final Node wsdlMessageAnnotation)
            throws InvalidAnnotationForMessageException {

        // Note: if the attribute 'xsl' is defined several times, an error is generating during the XML parsing of the
        // WSDL. SO it is not needed to manage this case here.
        final String xslFileStr;
        if (!((Element) wsdlMessageAnnotation).hasAttribute(MAPPING_ANNOTATION_XSL)) {
            xslFileStr = null;
        } else {
            final String xslFileAttrStr = ((Element) wsdlMessageAnnotation).getAttribute(MAPPING_ANNOTATION_XSL);
            if (xslFileAttrStr.isEmpty()) {
                throw new EmptyMappingXslDefinedException(wsdlOperationName, wsdlMessageAnnotation.getLocalName());
            } else {
                xslFileStr = xslFileAttrStr;
            }
        }

        return xslFileStr;
    }

    /**
     * Retrieve the XPath flag defining an output condition given as an XPath expressionL
     * 
     * @param wsdlOperationName
     *            The binding operation name of the binding operation message to parse
     * @param wsdlMessageConditionAnnotation
     *            The WSDL binding annotation node associated to the condition to parse
     * @return An XSL file name
     * @throws InvalidAnnotationForMessageException
     *             An error occurs during the parsing of the binding annotation
     */
    private static final Boolean retrieveXpathExprFlag(final QName wsdlOperationName,
            final Node wsdlMessageConditionAnnotation, final String wsdlMessageName)
            throws InvalidAnnotationForMessageException {

        // Note: if the attribute 'as-xpath-expr' is defined several times, an error is generating during the XML
        // parsing of the
        // WSDL. SO it is not needed to manage this case here.
        final Boolean xpathExprFlag;
        if (!((Element) wsdlMessageConditionAnnotation).hasAttribute(MAPPING_ANNOTATION_EXPR_XPATH)) {
            xpathExprFlag = null;
        } else {
            xpathExprFlag = Boolean.parseBoolean(
                    ((Element) wsdlMessageConditionAnnotation).getAttribute(MAPPING_ANNOTATION_EXPR_XPATH));
        }

        return xpathExprFlag;
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
