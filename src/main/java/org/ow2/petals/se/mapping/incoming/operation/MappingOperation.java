/**
 * Copyright (c) 2016-2017 Linagora
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
package org.ow2.petals.se.mapping.incoming.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation.MEPPatternConstants;
import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.jbidescriptor.generated.Consumes;
import org.ow2.petals.component.framework.listener.AbstractListener;
import org.ow2.petals.component.framework.util.NormalizedMessageUtil;
import org.ow2.petals.se.mapping.incoming.message.MappingInputMessage;
import org.ow2.petals.se.mapping.incoming.message.MappingOutputMessage;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.ow2.petals.se.mapping.incoming.operation.exception.InvalidAnnotationForOperationException;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.stream.EasyByteArrayOutputStream;
import com.ebmwebsourcing.easycommons.xml.SourceHelper;

/**
 * The XSL mapping operation extracted from WSDL according to SE Mapping annotations.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MappingOperation {

    /**
     * The mapping transformation to apply on the incoming message
     */
    private final MappingInputMessage inputMessageMapping;

    /**
     * The mapping transformation to apply on the outgoing message
     */
    private final MappingOutputMessage outputMessageMapping;

    /**
     * The WSDL operation containing the current annotations
     */
    private final QName wsdlOperation;

    /**
     * The service to invoke declared in the service unit
     */
    private final Consumes serviceProvider;

    /**
     * The operation of the @{@link #serviceProvider} to invoke, declared as annotation in the WSDL
     */
    private final QName serviceProviderOperation;

    private final Logger logger;

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL operation containing the current annotations
     * @param inputMessageMapping
     *            The mapping transformation to apply on the incoming message. Not {@code null}.
     * @param outputMessageMapping
     *            The mapping transformation to apply on the outgoing message. Can be {@code null}.
     * @param serviceProvider
     *            The service to invoke declared in the service unit. Not {@code null}.
     * @param serviceProviderOperation
     *            The service operation to invoke. Not {@code null}.
     * @param logger
     *            A logger
     */
    public MappingOperation(final QName wsdlOperationName, final MappingInputMessage inputMessageMapping,
            final MappingOutputMessage outputMessageMapping, final Consumes serviceProvider,
            final QName serviceProviderOperation, final Logger logger) {
        this.wsdlOperation = wsdlOperationName;
        this.inputMessageMapping = inputMessageMapping;
        this.outputMessageMapping = outputMessageMapping;
        this.serviceProvider = serviceProvider;
        this.logger = logger;
        this.serviceProviderOperation = serviceProviderOperation;
    }

    /**
     * <p>
     * Verify that annotation read from the WSDL are valid for the operation, otherwise the exception
     * {@link InvalidAnnotationForOperationException} is thrown.
     * </p>
     * <p>
     * During coherence checking, we can update not initialized field with the one available in SU JBI descriptor
     * extensions.
     * </p>
     * 
     * @throws InvalidAnnotationForOperationException
     *             The annotated operation is incoherent.
     */
    public void verifyAnnotationCoherence() throws InvalidAnnotationForOperationException {

        // NOP

    }

    /**
     * <p>
     * Execute the operation (sending request part):
     * <ol>
     * <li>transform the incoming request,</li>
     * <li>invoke a service using the transformed request,
     * <li>
     * <li>transform the service reply,</li>
     * <li>and return it to the caller.</li>
     * </ol>
     * </p>
     * 
     * @param exchange
     *            The exchange that contains the incoming request, and where the response will be put.
     * @param jbiMessageSender
     *            A JBI message sender
     * @param componentProperties
     *            Properties defined in the property file configured at component level
     * @return {@code true} if the given {@code exchange} must be terminated and sent back automatically by the CDK.
     *         {@code false} if the given {@code exchange} will be terminated manually
     */
    public boolean sendRequest(final Exchange exchange, final AbstractListener jbiMessageSender,
            final Properties componentProperties) {

        try {
            // Get the input message content
            final Document inputSource = exchange.getInMessageContentAsDocument();

            // Transform the incoming request
            final EasyByteArrayOutputStream ebaos = new EasyByteArrayOutputStream();
            final Result transformedInputResult = new StreamResult(ebaos);

            // TODO: Must be optimlized to avoid conversion Source <-> Document
            this.inputMessageMapping.transform(new DOMSource(inputSource), transformedInputResult, componentProperties);

            // Invoke the service - Create a new exchange
            final Exchange subExchange = jbiMessageSender.createConsumeExchange(this.serviceProvider,
                    MEPPatternConstants.fromURI(exchange.getPattern()));
            // We always use the operation from the service unit (the JBI Consumes defines the service used, not the
            // operation)
            subExchange.setOperation(this.serviceProviderOperation);

            // Set the XML payload
            // TODO: Must be optimized to avoid copy of byte array
            subExchange.setInMessageContent(new StreamSource(ebaos.toByteArrayInputStream()));
            // Copy attachments
            NormalizedMessageUtil.copyAttachments(exchange.getInMessage(), subExchange.getInMessage());

            // Invoke the service - Send request
            jbiMessageSender.sendAsync(subExchange,
                    new MappingOperationAsyncContext(this, exchange, subExchange, inputSource));

            return false;

        } catch (final MessagingException e) {
            // Technical error
            this.logger.log(Level.SEVERE, "Exchange " + exchange.getExchangeId() + " encountered a technical problem.",
                    e);
            exchange.setError(e);
            return true;
        }

    }

    /**
     * <p>
     * Execute the operation (processing response part):
     * <ol>
     * <li>transform the incoming request (business request),</li>
     * <li>invoke a service using the transformed request (technical request),
     * <li>
     * <li>transform the service reply (technical response),</li>
     * <li>and return it to the caller (business response).</li>
     * </ol>
     * </p>
     * 
     * @param technicalExchange
     *            The exchange used to invoke the service provider
     * @param context
     *            Asynchronous context of the technical service invocation
     * @param componentProperties
     *            Properties defined in the property file configured at component level
     */
    public void processResponse(final Exchange technicalExchange, final MappingOperationAsyncContext context,
            final Properties componentProperties) {

        final Exchange businessExchange = context.getInitialExchange();

        try {

            if (technicalExchange.getError() != null) {
                // Technical error received from technical service
                final Exception businessError = new MessagingException(
                        String.format(
                                "A technical error occurs at technical service level invoking the operation '%s' of the service '%s'.",
                                technicalExchange.getOperation(), technicalExchange.getService()),
                        technicalExchange.getError());
                this.logger.log(Level.SEVERE, businessError.getMessage(), technicalExchange.getError());
                businessExchange.setError(businessError);
            } else if (technicalExchange.isDoneStatus()) {
                // We receive a DONE status as response. It should be an exchange with pattern InOnly, RobustInOnly or
                // InOptionalOut. We return the same status.
                businessExchange.setDoneStatus();
            } else {
                // An OUT message or a fault was returned

                // Get the output message and output message content
                final NormalizedMessage technicalOutput;
                final Document technicalOutputDocument;
                if (technicalExchange.getFault() != null) {
                    this.logger.fine("Output extracted from fault");
                    technicalOutput = technicalExchange.getFault();
                    technicalOutputDocument = SourceHelper.toDocument(technicalOutput.getContent());
                } else {
                    this.logger.fine("Output extracted from OUT message");
                    technicalOutput = technicalExchange.getOutMessage();
                    technicalOutputDocument = technicalExchange.getOutMessageContentAsDocument(false);
                }

                // Transform the incoming response
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final Result transformedOutputResult = new StreamResult(baos);

                this.outputMessageMapping.transform(new DOMSource(technicalOutputDocument), transformedOutputResult,
                        context.getInputRequest(), componentProperties);

                final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                try {
                    // Set as XML payload of the normal OUT message or fault
                    // TODO: Must be optimized to avoid copy of byte array
                    if (this.outputMessageMapping.shouldReturnFault(technicalOutputDocument)) {
                        this.logger.fine("Output returned as fault");
                        final Fault businessFault = businessExchange.createFault();
                        businessFault.setContent(new StreamSource(bais));
                        businessExchange.setFault(businessFault);
                    } else {
                        this.logger.fine("Output returned as OUT message");
                        businessExchange.setOutMessageContent(new StreamSource(bais));
                        // Copy attachments
                        NormalizedMessageUtil.copyAttachments(technicalOutput, businessExchange.getOutMessage());
                    }

                } finally {
                    try {
                        bais.close();
                    } catch (final IOException e) {
                        this.logger.log(Level.WARNING, "An error occurs closing XML payload of sub-exchange", e);
                    }
                }
            }

        } catch (final MessagingException | InvalidAnnotationForMessageException | TransformerException e) {
            // Technical error
            final String errorMessage = "A technical problem occurs processing the response of exchange "
                    + technicalExchange.getExchangeId() + " to put into exchange" + businessExchange.getExchangeId()
                    + ".";
            this.logger.log(Level.SEVERE, errorMessage, e);
            businessExchange.setError(new MessagingException(errorMessage, e));
        }
    }

    /**
     * <p>
     * Execute the operation (processing response timout expiration part):
     * <ol>
     * <li>transform the incoming request,</li>
     * <li>invoke a service using the transformed request,
     * <li>
     * <li>transform the service reply,</li>
     * <li>and return it to the caller.</li>
     * </ol>
     * </p>
     * 
     * @param context
     *            Asynchronous context of the technical service invocation
     */
    public void processExpiredResponse(final MappingOperationAsyncContext context) {

        final String errorMessage = String.format("The timeout occurs invoking the operation '%s' of the service '%s'",
                context.getTechnicalOperationName().toString(), context.getTechnicalServiceName().toString());
        this.logger.log(Level.WARNING, errorMessage);
        context.getInitialExchange().setError(new MessagingException(errorMessage));
    }

    /**
     * @return The WSDL operation containing the current annotations
     */
    public QName getWsdlOperation() {
        return this.wsdlOperation;
    }

    /**
     * Log the mapping operation characteristics
     * 
     * @param logger
     * @param logLevel
     */
    public void log() {
        this.logger.config("mapping operation '" + this.wsdlOperation + "':");
        this.logger.config("  - service provider:");
        this.logger.config("    - interface name = " + this.serviceProvider.getInterfaceName().toString());
        this.logger.config("    - service name = " + this.serviceProvider.getServiceName().toString());
        this.logger.config("    - endpoint name = " + this.serviceProvider.getEndpointName());
        this.logger.config("    - operation name = " + this.serviceProviderOperation.toString());
        this.logger.config("    - timeout = " + this.serviceProvider.getTimeout());
        this.logger.config("  - input message:");
        this.logger.config("    - mapping message nature = " + this.inputMessageMapping.getClass().getName());
        this.inputMessageMapping.log();
        if (this.outputMessageMapping != null) {
            this.logger.config("  - output message:");
            this.logger.config("    - mapping message nature = " + this.outputMessageMapping.getClass().getName());
            this.outputMessageMapping.log();
            this.outputMessageMapping.getCondition().log();
        }
    }

}
