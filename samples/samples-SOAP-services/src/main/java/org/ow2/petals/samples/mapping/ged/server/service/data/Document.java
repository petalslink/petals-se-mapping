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
package org.ow2.petals.samples.mapping.ged.server.service.data;

import java.util.List;

import javax.activation.DataHandler;

public class Document {

    private DataHandler file;

    private List<String> properties;

    public Document() {
        this.file = null;
        this.properties = null;
    }

    public Document(final DataHandler file, final List<String> properties) {
        this.file = file;
        this.properties = properties;
    }

    public DataHandler getFile() {
        return this.file;
    }

    public void setFile(final DataHandler file) {
        this.file = file;
    }

    public List<String> getProperties() {
        return this.properties;
    }

    public void setPrice(final List<String> properties) {
        this.properties = properties;
    }
}
