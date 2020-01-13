/**
 * Copyright (c) 2016-2020 Linagora
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
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import org.ow2.petals.samples.mapping.ged.server.service.data.DocumentInconnuException;

@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public interface GedService {

    @WebMethod
    public void stocker(@WebParam(name = "reference") final String reference,
            @WebParam(name = "type") final String type, @WebParam(name = "file") DataHandler file);

    @WebMethod
    @WebResult(name = "file")
    public DataHandler consulter(@WebParam(name = "reference") final String reference) throws DocumentInconnuException;

}
