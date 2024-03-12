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
package org.ow2.petals.se.mapping.incoming.message;

import javax.xml.namespace.QName;

/**
 * An abstract mapping message extracted from WDSL
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public abstract class AbstractMappingMessage implements AbsMappingMessage {

    /**
     * The WSDL binding operation name containing the current annotations
     */
    protected final QName wsdlOperationName;

    /**
     * The WSDL binding operation message name containing the current annotations
     */
    protected final String wsdlMessageName;

    /**
     * Create an annotated operation.
     * 
     * @param wsdlOperationName
     *            The WSDL binding operation name containing the current annotations
     * @param wsdlMessageName
     *            The WSDL binding operation message name containing the current annotations
     */
    protected AbstractMappingMessage(final QName wsdlOperationName, final String wsdlMessageName) {
        super();
        this.wsdlOperationName = wsdlOperationName;
        this.wsdlMessageName = wsdlMessageName;
    }

    @Override
    public QName getWsdlOperationName() {
        return this.wsdlOperationName;
    }

    @Override
    public String getWsdlMessageName() {
        return this.wsdlMessageName;
    }
}
