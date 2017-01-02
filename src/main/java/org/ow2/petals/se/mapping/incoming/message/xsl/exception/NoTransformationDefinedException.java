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
package org.ow2.petals.se.mapping.incoming.message.xsl.exception;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;

/**
 * No transformation defined through WSDL annotations
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class NoTransformationDefinedException extends InvalidAnnotationForMessageException {

    private static final long serialVersionUID = 1558905311983860667L;

    private static final String MESSAGE = "No transformation defined through WSDL annotations";

    public NoTransformationDefinedException(final QName wsdlOperation, final String messageName) {
        super(wsdlOperation, messageName, MESSAGE);
    }

}
