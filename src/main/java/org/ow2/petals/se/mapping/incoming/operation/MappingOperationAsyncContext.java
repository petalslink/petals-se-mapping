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

import org.ow2.petals.component.framework.api.message.Exchange;
import org.ow2.petals.component.framework.process.async.AsyncContext;

public class MappingOperationAsyncContext extends AsyncContext {

    /**
     * The current mapping operation
     */
    final MappingOperation mappingOperation;

    /**
     * The initial exchange
     */
    final Exchange initialExchange;

    public MappingOperationAsyncContext(final MappingOperation mappingOperation, final Exchange initialExchange) {
        this.mappingOperation = mappingOperation;
        this.initialExchange = initialExchange;
    }

    public MappingOperation getMappingOperation() {
        return this.mappingOperation;
    }

    public Exchange getInitialExchange() {
        return this.initialExchange;
    }

}
