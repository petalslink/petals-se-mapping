/**
 * Copyright (c) 2016-2025 Linagora
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

import java.io.File;

import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.incoming.message.exception.InvalidAnnotationForMessageException;

/**
 * The XSL style-sheet file declared on a WSDL binding operation message is an invalid file.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class InvalidXslFileMappingException extends InvalidAnnotationForMessageException {

    private static final long serialVersionUID = 3418313515942792117L;

    private static final String MESSAGE_PATTERN = "The XSL style-sheet file is an invalid file: %s";

    public InvalidXslFileMappingException(final QName wsdlOperation, final String messageName, final File xslFile) {
        super(wsdlOperation, messageName, String.format(MESSAGE_PATTERN, xslFile.getAbsolutePath()));
    }

}
