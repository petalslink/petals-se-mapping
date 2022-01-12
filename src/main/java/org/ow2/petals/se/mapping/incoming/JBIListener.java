/**
 * Copyright (c) 2016-2022 Linagora
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.listener.AbstractJBIListener;
import org.ow2.petals.component.framework.process.async.AsyncContext;
import org.ow2.petals.component.framework.util.ServiceEndpointOperationKey;
import org.ow2.petals.se.mapping.MappingSE;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperationAsyncContext;

/**
 * Listens to messages incoming from inside Petals.
 * <p>
 * This class is in charge of processing messages coming from a Petals consumer. These messages can be requests (in
 * messages) or acknowledgments (ACK).
 * </p>
 * 
 * @author Christophe DENEUX - Linagora
 */
public class JBIListener extends AbstractJBIListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onJBIMessage(final Exchange exchange) {

        final Logger logger = this.getLogger();
        logger.fine("Start JBIListener.onJBIMessage()");
        try {
            boolean isExchangeClosedByCDK = true;

            if (exchange.isActiveStatus()) {

                // Provider role
                if (exchange.isProviderRole()) {
                    final String eptName = exchange.getEndpointName();
                    final QName service = exchange.getService();
                    final QName interfaceName = exchange.getInterfaceName();
                    final QName operation = exchange.getOperation();

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(exchange.getExchangeId() + " was received and is started to be processed.");
                        logger.fine("InterfaceName = " + interfaceName);
                        logger.fine("Service       = " + service);
                        logger.fine("EndpointName  = " + eptName);
                        logger.fine("OperationName = " + operation);
                        logger.fine("Pattern " + exchange.getPattern());
                    }

                    // TODO Validate Message

                    final ServiceEndpointOperationKey eptAndOperation = new ServiceEndpointOperationKey(service,
                            eptName, operation);

                    try {
                        // Get the mapping operation to execute from the registered operation
                        final MappingOperation mappingOperation = ((MappingSE) this.getComponent())
                                .getMappingOperation(eptAndOperation);
                        if (mappingOperation == null) {
                            // TODO: Create a unit test
                            throw new MessagingException("No mapping operation found matching the exchange");
                        }

                        isExchangeClosedByCDK = mappingOperation.sendRequest(exchange, this,
                                this.getComponent().getPlaceHolders());

                    } catch (final MessagingException e) {
                        logger.log(Level.SEVERE, "Exchange " + exchange.getExchangeId() + " encountered a problem.", e);
                        exchange.setError(e);
                    }
                } else {
                    // TODO: to do
                }
            } else if (exchange.isErrorStatus()) {
                logger.warning("Exchange " + exchange.getExchangeId() + " received with a status 'ERROR'. Skipped !");
            }

            // True to let the CDK close the exchange.
            // False to explicitly return the exchange.
            return isExchangeClosedByCDK;
        } finally {
            logger.fine("End JBIListener.onJBIMessage()");
        }
    }

    @Override
    public boolean onAsyncJBIMessage(final Exchange asyncExchange, final AsyncContext asyncContext) {

        if (asyncContext instanceof MappingOperationAsyncContext) {
            final MappingOperationAsyncContext mappingOperationAsyncContext = (MappingOperationAsyncContext) asyncContext;
            final Exchange initialExchange = mappingOperationAsyncContext.getInitialExchange();
            mappingOperationAsyncContext.getMappingOperation().processResponse(asyncExchange,
                    mappingOperationAsyncContext, this.getComponent().getPlaceHolders());

            try {
                this.send(initialExchange);
            } catch (final MessagingException e) {
                final String errorMessage = "A technical problem occurs processing the response of exchange "
                        + asyncExchange.getExchangeId() + " to put into exchange" + initialExchange.getExchangeId()
                        + ".";
                this.getLogger().log(Level.SEVERE, errorMessage, e);
                asyncExchange.setError(new MessagingException(errorMessage, e));
            }

        } else {
            this.getLogger().warning("Unexpected asynchronous context received: " + asyncContext.getClass().getName());
        }

        return true;
    }

    @Override
    public void onExpiredAsyncJBIMessage(final Exchange asyncExchange, final AsyncContext asyncContext) {

        if (asyncContext instanceof MappingOperationAsyncContext) {
            final MappingOperationAsyncContext mappingOperationAsyncContext = (MappingOperationAsyncContext) asyncContext;

            mappingOperationAsyncContext.getMappingOperation().processExpiredResponse(mappingOperationAsyncContext);

            try {
                this.send(mappingOperationAsyncContext.getInitialExchange());
            } catch (final MessagingException e) {
                this.getLogger().log(Level.WARNING,
                        String.format(
                                "A technical error occurs returning the exchange '%s' containing the time-out error.",
                                mappingOperationAsyncContext.getInitialExchange().getExchangeId()),
                        e);
            }

        } else {
            this.getLogger()
                    .warning("Unexpected expired asynchronous context received: " + asyncContext.getClass().getName());
        }
    }
}
