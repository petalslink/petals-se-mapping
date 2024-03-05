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
package org.ow2.petals.se.mapping.incomming;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.ow2.petals.component.framework.junit.helpers.SimpleComponent;
import org.ow2.petals.component.framework.junit.impl.ConsumesServiceConfiguration;
import org.ow2.petals.component.framework.junit.impl.ProvidesServiceConfiguration;
import org.ow2.petals.component.framework.junit.impl.ServiceConfiguration;
import org.ow2.petals.component.framework.junit.rule.ComponentUnderTest;
import org.ow2.petals.component.framework.junit.rule.ParameterGenerator;
import org.ow2.petals.component.framework.junit.rule.ServiceConfigurationFactory;
import org.ow2.petals.junit.rules.log.handler.InMemoryLogHandler;
import org.ow2.petals.se.mapping.AbstractTest;

import com.ebmwebsourcing.easycommons.lang.UncheckedException;

/**
 * Abstract class for unit tests about request processing
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public abstract class AbstractComponentTest extends AbstractTest {

    protected static final long FACTURE_TIMEOUT = 4000;

    private static final String FACTURE_NAMESPACE = "http://petals.ow2.org/se/mapping/unit-test/facture";

    protected static final QName FACTURE_INTERFACE = new QName(FACTURE_NAMESPACE, "facture");

    protected static final QName FACTURE_SERVICE = new QName(FACTURE_NAMESPACE, "factureService");

    protected static final String FACTURE_ENDPOINT = "testEndpointName";

    protected static final QName OPERATION_ARCHIVER = new QName(FACTURE_NAMESPACE, "archiver");

    protected static final QName OPERATION_CONSULTER = new QName(FACTURE_NAMESPACE, "consulter");

    protected static final QName OPERATION_OUT2FAULT = new QName(FACTURE_NAMESPACE, "out2fault");

    protected static final QName OPERATION_FAULT2OUT = new QName(FACTURE_NAMESPACE, "fault2out");

    private static final String GED_NAMESPACE = "http://petals.ow2.org/se/mapping/unit-test/ged";

    protected static final QName GED_INTERFACE = new QName(GED_NAMESPACE, "document");

    protected static final QName GED_SERVICE = new QName(GED_NAMESPACE, "documentService");

    protected static final String GED_ENDPOINT = "gedEndpointName";

    protected static final QName GED_ARCHIVER_OPERATION = new QName(GED_NAMESPACE, "archiver");

    protected static final QName GED_CONSULTER_OPERATION = new QName(GED_NAMESPACE, "consulter");

    protected static final QName GED_SUPPRIMER_OPERATION = new QName(GED_NAMESPACE, "supprimer");

    protected static final long GED_TIMEOUT = 4000;

    protected static final String VALID_SU = "valid-su";

    protected static final String COMP_PROPERTY_VALUE_1 = "value-1";

    protected static final String COMP_PROPERTY_VALUE_2 = "value-2";

    protected static final InMemoryLogHandler IN_MEMORY_LOG_HANDLER = new InMemoryLogHandler();

    protected static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    protected static final ComponentUnderTest COMPONENT_UNDER_TEST = new ComponentUnderTest()
            .addLogHandler(IN_MEMORY_LOG_HANDLER.getHandler())
            .setParameter(new QName("http://petals.ow2.org/components/extensions/version-5", "properties-file"),
                    new ParameterGenerator() {

                        @Override
                        public String generate() throws Exception {
                            final URL componentPropertiesFile = Thread.currentThread().getContextClassLoader()
                                    .getResource("su/valid/componentProperties.properties");
                            assertNotNull("Component properties file is missing !", componentPropertiesFile);
                            return componentPropertiesFile.toString();
                        }

                    })
            .registerServiceToDeploy(VALID_SU, new ServiceConfigurationFactory() {
                @Override
                public ServiceConfiguration create() {

                    final URL wsdlUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/facture.wsdl");
                    assertNotNull("WSDl not found", wsdlUrl);
                    final ProvidesServiceConfiguration serviceConfiguration = new ProvidesServiceConfiguration(
                            FACTURE_INTERFACE, FACTURE_SERVICE, FACTURE_ENDPOINT, wsdlUrl);

                    final URL inputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/input-archiver.xsl");
                    assertNotNull("XSL 'input-archiver.xsl' not found", inputArchiverXslUrl);
                    serviceConfiguration.addResource(inputArchiverXslUrl);

                    final URL outputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/output-archiver.xsl");
                    assertNotNull("XSL 'output-archiver.xsl' not found", outputArchiverXslUrl);
                    serviceConfiguration.addResource(outputArchiverXslUrl);

                    final URL inputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/input-consulter.xsl");
                    assertNotNull("XSL 'input-consulter.xsl' not found", inputConsulterXslUrl);
                    serviceConfiguration.addResource(inputConsulterXslUrl);

                    final URL outputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/output-consulter.xsl");
                    assertNotNull("XSL 'output-consulter.xsl' not found", outputConsulterXslUrl);
                    serviceConfiguration.addResource(outputConsulterXslUrl);

                    final URL inputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/input-supprimer.xsl");
                    assertNotNull("XSL 'input-supprimer.xsl' not found", inputSupprimerXslUrl);
                    serviceConfiguration.addResource(inputSupprimerXslUrl);

                    final URL outputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/output-supprimer.xsl");
                    assertNotNull("XSL 'output-supprimer.xsl' not found", outputSupprimerXslUrl);
                    serviceConfiguration.addResource(outputSupprimerXslUrl);

                    final URL outputOut2FaultXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/output-out2fault.xsl");
                    assertNotNull("XSL 'output-out2fault.xsl' not found", outputOut2FaultXslUrl);
                    serviceConfiguration.addResource(outputOut2FaultXslUrl);

                    final URL outputFault2OutXslUrl = Thread.currentThread().getContextClassLoader()
                            .getResource("su/valid/output-fault2out.xsl");
                    assertNotNull("XSL 'output-fault2out.xsl' not found", outputFault2OutXslUrl);
                    serviceConfiguration.addResource(outputFault2OutXslUrl);

                    // Technical consumed service 'ged'
                    final ConsumesServiceConfiguration consumeServiceConfiguration = new ConsumesServiceConfiguration(
                            GED_INTERFACE, GED_SERVICE, GED_ENDPOINT);
                    consumeServiceConfiguration.setTimeout(FACTURE_TIMEOUT);
                    serviceConfiguration.addServiceConfigurationDependency(consumeServiceConfiguration);

                    return serviceConfiguration;
                }
            }).registerExternalServiceProvider(GED_ENDPOINT, GED_SERVICE, GED_INTERFACE);

    protected static Marshaller MARSHALLER;

    protected static Unmarshaller UNMARSHALLER;

    @ClassRule
    public static final TestRule chain = RuleChain.outerRule(TEMP_FOLDER).around(IN_MEMORY_LOG_HANDLER)
            .around(COMPONENT_UNDER_TEST);

    protected static final SimpleComponent COMPONENT = new SimpleComponent(COMPONENT_UNDER_TEST);

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

    /**
     * All log traces must be cleared before starting a unit test
     */
    @Before
    public void clearLogTraces() {
        IN_MEMORY_LOG_HANDLER.clear();
    }

    /**
     * Convert a JAXB element to bytes
     * 
     * @param jaxbElement
     *            The JAXB element to write as bytes
     */
    protected byte[] toByteArray(final Object jaxbElement) throws JAXBException, IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            MARSHALLER.marshal(jaxbElement, baos);
            return baos.toByteArray();
        } finally {
            baos.close();
        }
    }

}
