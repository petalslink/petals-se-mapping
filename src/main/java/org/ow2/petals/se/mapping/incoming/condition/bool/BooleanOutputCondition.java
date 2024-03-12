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
package org.ow2.petals.se.mapping.incoming.condition.bool;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.condition.AbstractMappingCondition;
import org.ow2.petals.se.mapping.incoming.condition.MappingOutputCondition;
import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;
import org.w3c.dom.Document;

/**
 * A {@link MappingOutputCondition} to return always a fault or an 'OUT' message according to a boolean value
 * 
 * @author Christophe DENEUX - Linagora
 *
 */
public class BooleanOutputCondition extends AbstractMappingCondition implements MappingOutputCondition {

    /**
     * If {@code true}, a fault will be always returned. If {@code false}, an 'OUT' message will be always returned.
     */
    private final boolean shouldReturnFault;

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL binding operation name containing the current annotations
     * @param wsdlMessageName
     *            The WSDL binding operation message name containing the current annotations
     * @param logger
     */
    public BooleanOutputCondition(final QName wsdlOperationName, final String wsdlMessageName, final Logger logger) {
        super(wsdlOperationName, wsdlMessageName, logger);
        this.shouldReturnFault = false;
    }

    /**
     * 
     * @param wsdlOperationName
     *            The WSDL binding operation name containing the current annotations
     * @param wsdlMessageName
     *            The WSDL binding operation message name containing the current annotations
     * @param shouldReturnFault
     * @param logger
     */
    public BooleanOutputCondition(final QName wsdlOperationName, final String wsdlMessageName,
            final boolean shouldReturnFault, final Logger logger) {
        super(wsdlOperationName, wsdlMessageName, logger);
        this.shouldReturnFault = shouldReturnFault;
    }

    @Override
    public boolean shouldReturnFault(final Document technicalDocument) throws InvalidAnnotationForMessageException {
        return this.shouldReturnFault;
    }

    @Override
    public void verifyAnnotationCoherence() throws InvalidAnnotationForMessageException {
        // NOP
    }

    @Override
    public void log() {
        super.log();
        this.logger.config("      - return only fault = " + this.shouldReturnFault);
    }

}
