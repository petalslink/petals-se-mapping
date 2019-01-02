/**
 * Copyright (c) 2016-2019 Linagora
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

import javax.xml.namespace.QName;

import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.process.async.AsyncContext;
import org.w3c.dom.Document;

public class MappingOperationAsyncContext extends AsyncContext {

    /**
     * The current mapping operation
     */
    private final MappingOperation mappingOperation;

    /**
     * The initial exchange
     */
    private final Exchange initialExchange;

    private final QName technicalServiceName;

    private final QName technicalOperationName;

    /**
     * The initial incoming request
     */
    private final Document inputRequest;

    public MappingOperationAsyncContext(final MappingOperation mappingOperation, final Exchange businessExchange,
            final Exchange technicalExchange, final Document inputRequest) {
        this.mappingOperation = mappingOperation;
        this.initialExchange = businessExchange;
        this.technicalServiceName = technicalExchange.getService();
        this.technicalOperationName = technicalExchange.getOperation();
        this.inputRequest = inputRequest;
    }

    public MappingOperation getMappingOperation() {
        return this.mappingOperation;
    }

    public Exchange getInitialExchange() {
        return this.initialExchange;
    }

    public QName getTechnicalServiceName() {
        return this.technicalServiceName;
    }

    public QName getTechnicalOperationName() {
        return this.technicalOperationName;
    }

    public Document getInputRequest() {
        return this.inputRequest;
    }

}
