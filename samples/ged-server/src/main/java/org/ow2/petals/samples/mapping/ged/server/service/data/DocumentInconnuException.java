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
package org.ow2.petals.samples.mapping.ged.server.service.data;

import javax.xml.ws.WebFault;

@WebFault(name = "documentInconnu", targetNamespace = "http://service.server.ged.mapping.samples.petals.ow2.org/")
public class DocumentInconnuException extends Exception {

    private static final long serialVersionUID = 5867587992536186478L;

    private static final String MESSAGE_PATTERN = "Unknown document '%s'";

    private final String reference;

    public DocumentInconnuException(final String reference) {
        super(String.format(MESSAGE_PATTERN, reference));
        this.reference = reference;
    }

    public String getReference() {
        return this.reference;
    }
}
