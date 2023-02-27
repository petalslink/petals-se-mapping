/**
 * Copyright (c) 2016-2023 Linagora
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
package org.ow2.petals.se.mapping.incoming.condition.exception;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;

/**
 * Several input XSLs are declared on a WSDL operation
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MultipleOutputConditionDefinedException extends InvalidAnnotationForMessageException {

    private static final long serialVersionUID = 2146748396977435879L;

    private static final String MESSAGE = "Several SE Mapping annotations defining an output condition are declared";

    public MultipleOutputConditionDefinedException(final QName wsdlOperation, final String wsdlMessageName) {
        super(wsdlOperation, wsdlMessageName, MESSAGE);
    }

}
