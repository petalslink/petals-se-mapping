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
package org.ow2.petals.samples.mapping;

import static org.ow2.petals.se.mapping.junit.Assert.assertWsdlCompliance;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

/**
 * Unit test of the compliance of the service WSDL
 * 
 * @author Christophe DENEUX - Linagora
 *
 */
public class WsdlComplianceTest {

    private static final String TARGET_NAMESPACE = "http://facture.mapping.samples.petals.ow2.org/";

    @Test
    public void validate() throws Exception {
        assertWsdlCompliance(
                new QName[] { new QName(TARGET_NAMESPACE, "stocker"), new QName(TARGET_NAMESPACE, "consulter") });
    }

}
