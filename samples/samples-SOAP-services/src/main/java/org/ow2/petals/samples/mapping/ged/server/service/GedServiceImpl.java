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
package org.ow2.petals.samples.mapping.ged.server.service;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.ow2.petals.samples.mapping.ged.server.service.data.DocumentInconnuException;

@MTOM(enabled = true, threshold = 2048)
@WebService(endpointInterface = "org.ow2.petals.samples.mapping.ged.server.service.GedService", serviceName = "GedService", portName = "GedServicePort")
public class GedServiceImpl implements GedService {

    @Override
    public void stocker(final String reference, final String type, final DataHandler file) {
        System.out.println(
                String.format("Storing as document '%S' ref '%s' as '%s'%n", type, reference, file.getContentType()));
    }

    @Override
    public DataHandler consulter(final String reference) throws DocumentInconnuException {
        if ("INCONNU".equals(reference)) {
            System.out.println(String.format("Unknown document '%s'", reference));
            throw new DocumentInconnuException(reference);
        } else {
            System.out.println(String.format("Getting document '%s'", reference));
            return null;
        }
    }
}
