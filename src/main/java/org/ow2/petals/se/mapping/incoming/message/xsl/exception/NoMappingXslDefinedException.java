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
package org.ow2.petals.se.mapping.incoming.message.xsl.exception;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;

/**
 * Several mapping annotations defining the use of a XSL style-sheet are declared on a WSDL binding operation message
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class NoMappingXslDefinedException extends InvalidAnnotationForMessageException {

    private static final long serialVersionUID = 6823581619015847993L;

    private static final String MESSAGE = "No XSL file given through the mapping annotation defining the use of a XSL style-sheet.";

    public NoMappingXslDefinedException(final QName wsdlOperation, final String messageName) {
        super(wsdlOperation, messageName, MESSAGE);
    }

}
