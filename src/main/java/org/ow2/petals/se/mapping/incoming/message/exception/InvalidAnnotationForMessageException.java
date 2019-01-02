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
package org.ow2.petals.se.mapping.incoming.message.exception;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.operation.exception.InvalidAnnotationForOperationException;

/**
 * A WSDL annotation is invalid for a operation message
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public abstract class InvalidAnnotationForMessageException extends InvalidAnnotationForOperationException {

    private static final long serialVersionUID = 4807726306653521594L;

    private static final String MESSAGE_PATTERN = "A WSDL annotation is invalid for a message '%s'";

    /**
     * Name of the WSDL binding operation message for which an error occurs
     */
    private final String wsdlMessageName;

    public InvalidAnnotationForMessageException(final QName wsdlOperation, final String wsdlMessageName,
            final String message) {
        super(wsdlOperation, String.format(MESSAGE_PATTERN, wsdlMessageName, message));
        this.wsdlMessageName = wsdlMessageName;
    }

    public InvalidAnnotationForMessageException(final QName wsdlOperation, final String wsdlMessageName,
            final String message, final Throwable cause) {
        super(wsdlOperation, String.format(MESSAGE_PATTERN, wsdlMessageName, message), cause);
        this.wsdlMessageName = wsdlMessageName;
    }

    /**
     * @return The name of the WSDL binding operation message for which an error occurs
     */
    public String getWsdlMessageName() {
        return this.wsdlMessageName;
    }
}
