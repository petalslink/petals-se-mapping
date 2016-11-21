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
package org.ow2.petals.se.mapping.incoming.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation.MEPPatternConstants;
import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.jbidescriptor.generated.Consumes;
import org.ow2.petals.component.framework.listener.AbstractListener;
import org.ow2.petals.component.framework.util.NormalizedMessageUtil;
import org.ow2.petals.se.mapping.incoming.message.MappingMessage;
import org.ow2.petals.se.mapping.incoming.operation.exception.InvalidAnnotationForOperationException;

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
    private final MappingMessage inputMessageMapping;

    /**
     * The mapping transformation to apply on the outgoing message
     */
    private final MappingMessage outputMessageMapping;

    /**
     * The mapping transformations to apply on outgoing faults
     */
    private final List<MappingMessage> faultMessageMappings;

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
     * @param faultMessageMappings
     *            The mapping transformations to apply on outgoing faults. Not {@code null}.
     * @param serviceProvider
     *            The service to invoke declared in the service unit. Not {@code null}.
     * @param serviceProviderOperation
     *            The service operation to invoke. Not {@code null}.
     * @param logger
     *            A logger
     */
    public MappingOperation(final QName wsdlOperationName, final MappingMessage inputMessageMapping,
            final MappingMessage outputMessageMapping, final List<MappingMessage> faultMessageMappings,
            final Consumes serviceProvider, final QName serviceProviderOperation, final Logger logger) {
        this.wsdlOperation = wsdlOperationName;
        this.inputMessageMapping = inputMessageMapping;
        this.outputMessageMapping = outputMessageMapping;
        this.faultMessageMappings = faultMessageMappings;
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
     * @return {@code true} if the given {@code exchange} must be terminated and sent back automatically by the CDK.
     *         {@code false} if the given {@code exchange} will be terminated manually
     */
    public boolean sendRequest(final Exchange exchange, final AbstractListener jbiMessageSender) {

        try {
            // Get the input message content
            final Source inputSource = exchange.getInMessageContentAsSource();

            // Transform the incoming request
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Result transformedInputResult = new StreamResult(baos);

            this.inputMessageMapping.transform(inputSource, transformedInputResult);
            
            // Invoke the service - Create a new exchange
            final Exchange subExchange = jbiMessageSender.createConsumeExchange(this.serviceProvider,
                    MEPPatternConstants.fromURI(exchange.getPattern()));
            // we always use the operation from the service unit (the JBI Consumes defines the service used, not the
            // operation)
            subExchange.setOperation(this.serviceProviderOperation);
            
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            try {
                // Set the XML payload
                // TODO: Must be optimized to avoid copy of byte array
                subExchange.setInMessageContent(new StreamSource(bais));
                // Copy attachments
                NormalizedMessageUtil.copyAttachments(exchange.getInMessage(), subExchange.getInMessage());

                // Invoke the service - Send request
                jbiMessageSender.sendAsync(subExchange,
                        new MappingOperationAsyncContext(this, exchange));

                return false;
            } finally {
                try {
                    bais.close();
                } catch (final IOException e) {
                    this.logger.log(Level.WARNING, "An error occurs closing XML payload of sub-exchange", e);
                }
            }

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
     * <li>transform the incoming request,</li>
     * <li>invoke a service using the transformed request,
     * <li>
     * <li>transform the service reply,</li>
     * <li>and return it to the caller.</li>
     * </ol>
     * </p>
     * 
     * @param subExchange
     *            The exchange used to invoke the service provider
     * @param initialExchange
     *            The exchange that contains the initial incoming request, and where the response will be put.
     */
    public void processResponse(final Exchange subExchange, final Exchange initialExchange) {

        try {

            if (subExchange.getError() != null) {
                // TODO: Process the error
            } else if (subExchange.getFault() != null) {
                // TODO: Process the fault
            } else {
                // A response was returned

                // Get the output message content
                final Source outputSource = subExchange.getOutMessageContentAsSource();

                // Transform the incoming response
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final Result transformedOutputResult = new StreamResult(baos);

                this.outputMessageMapping.transform(outputSource, transformedOutputResult);

                final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                try {
                    // Set the XML payload
                    // TODO: Must be optimized to avoid copy of byte array
                    initialExchange.setOutMessageContent(new StreamSource(bais));
                    // Copy attachments
                    NormalizedMessageUtil.copyAttachments(subExchange.getInMessage(), initialExchange.getInMessage());

                } finally {
                    try {
                        bais.close();
                    } catch (final IOException e) {
                        this.logger.log(Level.WARNING, "An error occurs closing XML payload of sub-exchange", e);
                    }
                }
            }

        } catch (final MessagingException e) {
            // Technical error
            this.logger.log(Level.SEVERE, "A technical problem occurs processing the response of exchange "
                    + subExchange.getExchangeId() + " to put into exchange" + initialExchange.getExchangeId() + ".", e);
            initialExchange.setError(e);
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
     * @param subExchange
     *            The exchange used to invoke the service provider
     * @param initialExchange
     *            The exchange that contains the initial incoming request, and where the response will be put.
     */
    public void processExpiredResponse(final Exchange subExchange, final Exchange initialExchange) {

        final String errorMessage = String.format("The timeout occurs invoking the operation '%s' of the service '%s'",
                subExchange.getOperation().toString(), subExchange.getService().toString());
        this.logger.log(Level.WARNING, errorMessage);
        initialExchange.setError(new MessagingException(errorMessage));
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
    public void log(final Logger logger, final Level logLevel) {

        if (logger.isLoggable(logLevel)) {
            logger.log(logLevel, "mapping operation '" + this.wsdlOperation + "':");
            logger.log(logLevel, "  - service provider:");
            logger.log(logLevel, "    - interface name = " + this.serviceProvider.getInterfaceName().toString());
            logger.log(logLevel, "    - service name = " + this.serviceProvider.getServiceName().toString());
            logger.log(logLevel, "    - endpoint name = " + this.serviceProvider.getEndpointName());
            logger.log(logLevel, "    - operation name = " + this.serviceProviderOperation.toString());
            logger.log(logLevel, "    - timeout = " + this.serviceProvider.getTimeout());
            logger.log(logLevel, "  - input message:");
            logger.log(logLevel, "    - mapping message nature = " + this.inputMessageMapping.getClass().getName());
            this.inputMessageMapping.log(logger, logLevel);
            if (this.outputMessageMapping != null) {
                logger.log(logLevel, "  - output message:");
                logger.log(logLevel,
                        "    - mapping message nature = " + this.outputMessageMapping.getClass().getName());
                this.outputMessageMapping.log(logger, logLevel);
            }
            for (final MappingMessage faultMessageMapping : this.faultMessageMappings) {
                logger.log(logLevel, "  - fault message: " + faultMessageMapping.getWsdlMessageName());
                logger.log(logLevel, "    - mapping message nature = " + faultMessageMapping.getClass().getName());
                faultMessageMapping.log(logger, logLevel);
            }
        }
    }

}
