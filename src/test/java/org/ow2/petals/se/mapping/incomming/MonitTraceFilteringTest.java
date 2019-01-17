/**
 * Copyright (c) 2019 Linagora
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

import java.net.URL;

import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.ow2.petals.component.framework.junit.impl.ProvidesServiceConfiguration;
import org.ow2.petals.component.framework.junit.monitoring.business.filtering.AbstractMonitTraceFilteringTestForSimpleOrchestration;
import org.ow2.petals.component.framework.junit.monitoring.business.filtering.exception.ServiceProviderCfgCreationError;
import org.ow2.petals.se.mapping.unit_test.facture.Consulter;

/**
 * Unit tests about MONIT trace filtering.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MonitTraceFilteringTest extends AbstractMonitTraceFilteringTestForSimpleOrchestration {

    @Override
    protected String getConsumedServiceEndpoint() {
        return AbstractEnv.GED_ENDPOINT;
    }

    @Override
    protected QName getConsumedServiceName() {
        return AbstractEnv.GED_SERVICE;
    }

    @Override
    protected QName getConsumedServiceInterface() {
        return AbstractEnv.GED_INTERFACE;
    }

    @Override
    protected QName getConsumedServiceOperation() {
        return AbstractEnv.GED_CONSULTER_OPERATION;
    }

    @Override
    protected QName getInvokedServiceProviderOperation() {
        return AbstractEnv.OPERATION_CONSULTER;
    }

    @Override
    protected Marshaller getMarshaller() {
        return AbstractEnv.MARSHALLER;
    }

    @Override
    protected Object createRequestPayloadToProvider() {
        return new Consulter();
    }

    @Override
    protected Object createResponsePayloadToProvider() {
        return new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse();
    }

    @Override
    protected ProvidesServiceConfiguration createServiceProvider(final int ruleIdx)
            throws ServiceProviderCfgCreationError {

        final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("su/valid/facture.wsdl");
        assertNotNull("Rule #" + ruleIdx + ": WSDl not found", wsdlUrl);
        final ProvidesServiceConfiguration serviceProviderCfg = new ProvidesServiceConfiguration(
                AbstractEnv.FACTURE_INTERFACE, AbstractEnv.FACTURE_SERVICE, AbstractEnv.FACTURE_ENDPOINT, wsdlUrl);

        final URL inputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-archiver.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'input-archiver.xsl' not found", inputArchiverXslUrl);
        serviceProviderCfg.addResource(inputArchiverXslUrl);

        final URL outputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-archiver.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'output-archiver.xsl' not found", outputArchiverXslUrl);
        serviceProviderCfg.addResource(outputArchiverXslUrl);

        final URL inputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-consulter.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'input-consulter.xsl' not found", inputConsulterXslUrl);
        serviceProviderCfg.addResource(inputConsulterXslUrl);

        final URL outputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-consulter.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'output-consulter.xsl' not found", outputConsulterXslUrl);
        serviceProviderCfg.addResource(outputConsulterXslUrl);

        final URL inputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-supprimer.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'input-supprimer.xsl' not found", inputSupprimerXslUrl);
        serviceProviderCfg.addResource(inputSupprimerXslUrl);

        final URL outputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-supprimer.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'output-supprimer.xsl' not found", outputSupprimerXslUrl);
        serviceProviderCfg.addResource(outputSupprimerXslUrl);

        final URL outputOut2FaultXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-out2fault.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'output-out2fault.xsl' not found", outputOut2FaultXslUrl);
        serviceProviderCfg.addResource(outputOut2FaultXslUrl);

        final URL outputFault2OutXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-fault2out.xsl");
        assertNotNull("Rule #" + ruleIdx + ": XSL 'output-fault2out.xsl' not found", outputFault2OutXslUrl);
        serviceProviderCfg.addResource(outputFault2OutXslUrl);

        return serviceProviderCfg;
    }
}
