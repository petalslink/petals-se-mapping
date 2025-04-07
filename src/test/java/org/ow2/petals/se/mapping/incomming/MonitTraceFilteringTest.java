/**
 * Copyright (c) 2019-2025 Linagora
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

import javax.xml.namespace.QName;

import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation.MEPPatternConstants;
import org.ow2.petals.component.framework.junit.impl.ProvidesServiceConfiguration;
import org.ow2.petals.component.framework.junit.monitoring.business.filtering.AbstractMonitTraceFilteringTestForSimpleOrchestration;
import org.ow2.petals.component.framework.junit.monitoring.business.filtering.exception.ServiceProviderCfgCreationError;
import org.ow2.petals.se.mapping.unit_test.facture.Consulter;
import org.ow2.petals.se.mapping.unit_test.facture.Supprimer;

import jakarta.xml.bind.Marshaller;

/**
 * Unit tests about MONIT trace filtering.
 * 
 * @author Christophe DENEUX - Linagora
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
    protected QName getConsumedServiceOperation(final MEPPatternConstants mep) {
        if (mep == MEPPatternConstants.IN_OUT) {
            return AbstractEnv.GED_CONSULTER_OPERATION;
        } else {
            return AbstractEnv.GED_SUPPRIMER_OPERATION;
        }
    }

    @Override
    protected QName getInvokedServiceProviderOperation(final MEPPatternConstants mep) {
        if (mep == MEPPatternConstants.IN_OUT) {
            return AbstractEnv.OPERATION_CONSULTER;
        } else {
            return AbstractEnv.OPERATION_SUPPRIMER;
        }
    }

    @Override
    protected Marshaller getMarshaller() {
        return AbstractEnv.MARSHALLER;
    }

    @Override
    protected MEPPatternConstants[] getMepsSupported() {
        return new MEPPatternConstants[] { MEPPatternConstants.IN_ONLY, MEPPatternConstants.IN_OUT,
                MEPPatternConstants.ROBUST_IN_ONLY };
    }

    @Override
    protected Object createRequestPayloadToProvider(final MEPPatternConstants mep) {
        if (mep == MEPPatternConstants.IN_OUT) {
            return new Consulter();
        } else {
            return new Supprimer();
        }
    }

    @Override
    protected Object createResponsePayloadToProvider(final MEPPatternConstants mep, final boolean useAsFault) {
        if (mep == MEPPatternConstants.IN_OUT) {
            if (useAsFault) {
                return new org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu();
            } else {
                return new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse();
            }
        } else {
            // mep == RobustInOnly && useAsFault == true
            return new org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu();
        }
    }

    @Override
    protected ProvidesServiceConfiguration createServiceProvider(final int ruleIdx)
            throws ServiceProviderCfgCreationError {

        final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("su/valid/facture.wsdl");
        assertNotNull(wsdlUrl, "Rule #" + ruleIdx + ": WSDl not found");
        final ProvidesServiceConfiguration serviceProviderCfg = new ProvidesServiceConfiguration(
                AbstractEnv.FACTURE_INTERFACE, AbstractEnv.FACTURE_SERVICE, AbstractEnv.FACTURE_ENDPOINT, wsdlUrl);

        final URL inputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-archiver.xsl");
        assertNotNull(inputArchiverXslUrl, "Rule #" + ruleIdx + ": XSL 'input-archiver.xsl' not found");
        serviceProviderCfg.addResource(inputArchiverXslUrl);

        final URL outputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-archiver.xsl");
        assertNotNull(outputArchiverXslUrl, "Rule #" + ruleIdx + ": XSL 'output-archiver.xsl' not found");
        serviceProviderCfg.addResource(outputArchiverXslUrl);

        final URL inputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-consulter.xsl");
        assertNotNull(inputConsulterXslUrl, "Rule #" + ruleIdx + ": XSL 'input-consulter.xsl' not found");
        serviceProviderCfg.addResource(inputConsulterXslUrl);

        final URL outputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-consulter.xsl");
        assertNotNull(outputConsulterXslUrl, "Rule #" + ruleIdx + ": XSL 'output-consulter.xsl' not found");
        serviceProviderCfg.addResource(outputConsulterXslUrl);

        final URL inputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/input-supprimer.xsl");
        assertNotNull(inputSupprimerXslUrl, "Rule #" + ruleIdx + ": XSL 'input-supprimer.xsl' not found");
        serviceProviderCfg.addResource(inputSupprimerXslUrl);

        final URL outputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-supprimer.xsl");
        assertNotNull(outputSupprimerXslUrl, "Rule #" + ruleIdx + ": XSL 'output-supprimer.xsl' not found");
        serviceProviderCfg.addResource(outputSupprimerXslUrl);

        final URL outputOut2FaultXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-out2fault.xsl");
        assertNotNull(outputOut2FaultXslUrl, "Rule #" + ruleIdx + ": XSL 'output-out2fault.xsl' not found");
        serviceProviderCfg.addResource(outputOut2FaultXslUrl);

        final URL outputFault2OutXslUrl = Thread.currentThread().getContextClassLoader()
                .getResource("su/valid/output-fault2out.xsl");
        assertNotNull(outputFault2OutXslUrl, "Rule #" + ruleIdx + ": XSL 'output-fault2out.xsl' not found");
        serviceProviderCfg.addResource(outputFault2OutXslUrl);

        return serviceProviderCfg;
    }
}
