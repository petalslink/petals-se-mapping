/**
 * Copyright (c) 2019-2021 Linagora
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
package org.ow2.petals.se.mapping.incomming;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.ow2.petals.se.mapping.AbstractTest;

import com.ebmwebsourcing.easycommons.lang.UncheckedException;

/**
 * Abstract class for unit tests about request processing
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public abstract class AbstractEnv extends AbstractTest {

    protected static final long FACTURE_TIMEOUT = 4000;

    private static final String FACTURE_NAMESPACE = "http://petals.ow2.org/se/mapping/unit-test/facture";

    protected static final QName FACTURE_INTERFACE = new QName(FACTURE_NAMESPACE, "facture");

    protected static final QName FACTURE_SERVICE = new QName(FACTURE_NAMESPACE, "factureService");

    protected static final String FACTURE_ENDPOINT = "testEndpointName";

    protected static final QName OPERATION_ARCHIVER = new QName(FACTURE_NAMESPACE, "archiver");

    protected static final QName OPERATION_CONSULTER = new QName(FACTURE_NAMESPACE, "consulter");

    protected static final QName OPERATION_SUPPRIMER = new QName(FACTURE_NAMESPACE, "supprimer");

    protected static final QName OPERATION_OUT2FAULT = new QName(FACTURE_NAMESPACE, "out2fault");

    protected static final QName OPERATION_FAULT2OUT = new QName(FACTURE_NAMESPACE, "fault2out");

    private static final String GED_NAMESPACE = "http://petals.ow2.org/se/mapping/unit-test/ged";

    protected static final QName GED_INTERFACE = new QName(GED_NAMESPACE, "document");

    protected static final QName GED_SERVICE = new QName(GED_NAMESPACE, "documentService");

    protected static final String GED_ENDPOINT = "gedEndpointName";

    protected static final QName GED_ARCHIVER_OPERATION = new QName(GED_NAMESPACE, "archiver");

    protected static final QName GED_CONSULTER_OPERATION = new QName(GED_NAMESPACE, "consulter");

    protected static final QName GED_SUPPRIMER_OPERATION = new QName(GED_NAMESPACE, "supprimer");

    protected static final String VALID_SU = "valid-su";

    protected static final String COMP_PROPERTY_VALUE_1 = "value-1";

    protected static final String COMP_PROPERTY_VALUE_2 = "value-2";

    protected static Marshaller MARSHALLER;

    protected static Unmarshaller UNMARSHALLER;

    static {
        try {
            final JAXBContext context = JAXBContext.newInstance(
                    org.ow2.petals.se.mapping.unit_test.facture.Archiver.class,
                    org.ow2.petals.se.mapping.unit_test.facture.Consulter.class,
                    org.ow2.petals.se.mapping.unit_test.facture.ConsulterResponse.class,
                    org.ow2.petals.se.mapping.unit_test.facture.Supprimer.class,
                    org.ow2.petals.se.mapping.unit_test.facture.SupprimerResponse.class,
                    org.ow2.petals.se.mapping.unit_test.facture.FactureInconnue.class,
                    org.ow2.petals.se.mapping.unit_test.facture.FactureExistante.class,
                    org.ow2.petals.se.mapping.unit_test.ged.Archiver.class,
                    org.ow2.petals.se.mapping.unit_test.ged.Consulter.class,
                    org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse.class,
                    org.ow2.petals.se.mapping.unit_test.ged.Supprimer.class,
                    org.ow2.petals.se.mapping.unit_test.ged.SupprimerResponse.class,
                    org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu.class,
                    org.ow2.petals.se.mapping.unit_test.ged.DocumentExistant.class);
            UNMARSHALLER = context.createUnmarshaller();
            MARSHALLER = context.createMarshaller();
            MARSHALLER.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (final JAXBException e) {
            throw new UncheckedException(e);
        }
    }

}
