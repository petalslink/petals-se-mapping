/**
 * Copyright (c) 2016-2021 Linagora
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

import java.util.List;
import java.util.logging.LogRecord;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.transform.Source;

import org.junit.Test;
import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation;
import org.ow2.petals.commons.log.FlowLogData;
import org.ow2.petals.commons.log.Level;
import org.ow2.petals.component.framework.api.Message.MEPConstants;
import org.ow2.petals.component.framework.junit.Message;
import org.ow2.petals.component.framework.junit.RequestMessage;
import org.ow2.petals.component.framework.junit.ResponseMessage;
import org.ow2.petals.component.framework.junit.StatusMessage;
import org.ow2.petals.component.framework.junit.helpers.ServiceProviderImplementation;
import org.ow2.petals.component.framework.junit.helpers.SimpleComponent;
import org.ow2.petals.component.framework.junit.impl.message.FaultToConsumerMessage;
import org.ow2.petals.component.framework.junit.impl.message.RequestToProviderMessage;
import org.ow2.petals.component.framework.junit.impl.message.ResponseToConsumerMessage;
import org.ow2.petals.component.framework.junit.impl.message.StatusToConsumerMessage;
import org.ow2.petals.se.mapping.unit_test.facture.Archiver;
import org.ow2.petals.se.mapping.unit_test.facture.Consulter;
import org.ow2.petals.se.mapping.unit_test.facture.ConsulterResponse;
import org.ow2.petals.se.mapping.unit_test.facture.FactureExistante;
import org.ow2.petals.se.mapping.unit_test.facture.FactureInconnue;
import org.ow2.petals.se.mapping.unit_test.facture.Supprimer;

import com.ebmwebsourcing.easycommons.xml.SourceHelper;

/**
 * Unit tests about request processing of mapping services, with a component configured with default values
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MappingServiceTest extends AbstractComponentTest {

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service
     * returns a normal response
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a nominal reply,</li>
     * <li>the response is correctly transformed,</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithOutMessageTransformedInOutMessage() throws Exception {

        final Consulter businessRequestBean = new Consulter();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_CONSULTER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Consulter);
                final org.ow2.petals.se.mapping.unit_test.ged.Consulter technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Consulter) technicalRequestObj;
                assertEquals(factureId + COMP_PROPERTY_VALUE_1, technicalRequestBean.getReference());

                final org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse technicalConsulterResponse = new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse();
                final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
                technicalConsulterResponse.setDocument(new DataHandler(ds));

                // We return the response
                return new ResponseToConsumerMessage(technicalRequest, technicalConsulterResponse, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status DONE on the message exchange
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.DONE, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest, technicalServiceMock);

        // Check the reply
        assertFalse(businessResponse.isFault());
        final Source fault = businessResponse.getFault();
        assertNull("Unexpected fault", (fault == null ? null : SourceHelper.toString(fault)));
        assertNotNull("No XML payload in response", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof ConsulterResponse);
        final ConsulterResponse businessResponseBean = (ConsulterResponse) businessResponseBeanObj;
        assertNotNull(businessResponseBean.getDocument());
        assertNotNull(businessResponse.getOutAttachmentNames());
        assertEquals(1, businessResponse.getOutAttachmentNames().size());

        // We check that the technical service has received the right status DONE
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service
     * returns a fault
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a fault,</li>
     * <li>the fault response is correctly transformed into a fault.</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithFaultTransformedInFault() throws Exception {

        final Consulter businessRequestBean = new Consulter();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_CONSULTER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Consulter);
                final org.ow2.petals.se.mapping.unit_test.ged.Consulter technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Consulter) technicalRequestObj;
                assertEquals(factureId + COMP_PROPERTY_VALUE_1, technicalRequestBean.getReference());

                final org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu technicalFault = new org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu();
                technicalFault.setReference(technicalRequestBean.getReference());

                // We return the response
                return new FaultToConsumerMessage(technicalRequest, technicalFault, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status DONE on the message exchange
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.DONE, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest, technicalServiceMock);

        // Check the reply
        assertTrue(businessResponse.isFault());
        final Source out = businessResponse.getOut();
        assertNull("Unexpected OUT message", (out == null ? null : SourceHelper.toString(out)));
        assertNotNull("No XML payload in fault", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof FactureInconnue);
        final FactureInconnue businessResponseBean = (FactureInconnue) businessResponseBeanObj;
        assertEquals(factureId + COMP_PROPERTY_VALUE_1 + COMP_PROPERTY_VALUE_2, businessResponseBean.getIdentifiant());

        // We check that the technical service has received the right status DONE
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service
     * returns a fault
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a fault,</li>
     * <li>the fault response is correctly transformed into an output message.</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithFaultTransformedInOutMessage() throws Exception {

        final Supprimer businessRequestBean = new Supprimer();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_FAULT2OUT, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_SUPPRIMER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Supprimer);
                final org.ow2.petals.se.mapping.unit_test.ged.Supprimer technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Supprimer) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                final org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu technicalFault = new org.ow2.petals.se.mapping.unit_test.ged.DocumentInconnu();
                technicalFault.setReference(technicalRequestBean.getReference());

                // We return the response
                return new FaultToConsumerMessage(technicalRequest, technicalFault, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status DONE on the message exchange
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.DONE, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest, technicalServiceMock);

        // Check the reply
        assertFalse(businessResponse.isFault());
        final Source fault = businessResponse.getFault();
        assertNull("Unexpected fault", (fault == null ? null : SourceHelper.toString(fault)));
        assertNotNull("No XML payload in response", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof Supprimer);
        final Supprimer businessResponseBean = (Supprimer) businessResponseBeanObj;
        assertEquals(factureId, businessResponseBean.getIdentifiant());

        // We check that the technical service has received the right status DONE
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_FAULT2OUT, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_SUPPRIMER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service
     * returns a normal response
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a nominal reply,</li>
     * <li>the response is correctly transformed into a fault,</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithOutMessageTransformedInFaultMessage() throws Exception {

        final Supprimer businessRequestBean = new Supprimer();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_OUT2FAULT, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_SUPPRIMER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Supprimer);
                final org.ow2.petals.se.mapping.unit_test.ged.Supprimer technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Supprimer) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                final org.ow2.petals.se.mapping.unit_test.ged.SupprimerResponse technicalConsulterResponse = new org.ow2.petals.se.mapping.unit_test.ged.SupprimerResponse();
                final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
                technicalConsulterResponse.setDocument(new DataHandler(ds));

                // We return the response
                return new ResponseToConsumerMessage(technicalRequest, technicalConsulterResponse, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status DONE on the message exchange
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.DONE, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest, technicalServiceMock);

        // Check the reply
        assertTrue(businessResponse.isFault());
        final Source out = businessResponse.getOut();
        assertNull("Unexpected OUT message", (out == null ? null : SourceHelper.toString(out)));
        assertNotNull("No XML payload in fault", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof FactureInconnue);
        final FactureInconnue businessResponseBean = (FactureInconnue) businessResponseBeanObj;
        assertEquals(factureId, businessResponseBean.getIdentifiant());

        // We check that the technical service has received the right status DONE
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_OUT2FAULT, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_SUPPRIMER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service
     * returns an error
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns an error (not a fault),</li>
     * <li>an error is returned.</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithErrorMessageTransformedInErrorMessage() throws Exception {

        final String myError = "My technical error";
        final Consulter businessRequestBean = new Consulter();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_CONSULTER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Consulter);
                final org.ow2.petals.se.mapping.unit_test.ged.Consulter technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Consulter) technicalRequestObj;
                assertEquals(factureId + COMP_PROPERTY_VALUE_1, technicalRequestBean.getReference());

                // We return an error
                return new StatusToConsumerMessage(technicalRequest, new Exception("My technical error"));
            }

            @Override
            public boolean statusExpected() {
                return false;
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock);

        // Check the reply
        assertNull(businessStatus.getOut());
        assertEquals(ExchangeStatus.ERROR, businessStatus.getStatus());
        assertEquals(myError, businessStatus.getError().getCause().getMessage());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-out request where the sub-service is
     * too long to reply.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called but a timeout expired,</li>
     * <li>an error is returned.</li>
     * </ul>
     */
    @Test
    public void validInOutRequest_WithTimeout() throws Exception {

        final Consulter businessRequestBean = new Consulter();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_OUT_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_CONSULTER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Consulter);
                final org.ow2.petals.se.mapping.unit_test.ged.Consulter technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Consulter) technicalRequestObj;
                assertEquals(factureId + COMP_PROPERTY_VALUE_1, technicalRequestBean.getReference());

                Thread.sleep(FACTURE_TIMEOUT + 2000);

                final org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse technicalConsulterResponse = new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse();
                final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
                technicalConsulterResponse.setDocument(new DataHandler(ds));

                // We return the response
                return new ResponseToConsumerMessage(technicalRequest, technicalConsulterResponse, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status ERROR on the message exchange because of timeout on consumer side
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ERROR, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request.
        // Caution to set the right timeout value
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock,
                SimpleComponent.DEFAULT_SEND_AND_RECEIVE_TIMEOUT, FACTURE_TIMEOUT + 2000);

        // Check the reply
        assertNull(businessStatus.getOut());
        assertEquals(ExchangeStatus.ERROR, businessStatus.getStatus());
        assertTrue(businessStatus.getError().getMessage().contains("timeout occurs"));
        assertTrue(businessStatus.getError().getMessage().contains(GED_CONSULTER_OPERATION.toString()));
        assertTrue(businessStatus.getError().getMessage().contains(GED_SERVICE.toString()));

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(3));

        // We check that the technical service has received the right status ERROR because of timeout on consumer side
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-only request where the sub-service
     * returns a status DONE.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a status DONE,</li>
     * <li>a status DONE is returned,</li>
     * </ul>
     */
    @Test
    public void validInOnlyRequest_WithStatusDone() throws Exception {

        final String factureId = "my-facture-id";

        final Archiver businessRequestBean = new Archiver();
        businessRequestBean.setIdentifiant(factureId);
        final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
        final DataHandler dh = new DataHandler(ds);
        businessRequestBean.setDocument(dh);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_ARCHIVER, AbsItfOperation.MEPPatternConstants.IN_ONLY.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_ONLY_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_ARCHIVER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Archiver);
                final org.ow2.petals.se.mapping.unit_test.ged.Archiver technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Archiver) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                // We return the status DONE
                return new StatusToConsumerMessage(technicalRequest, ExchangeStatus.DONE);
            }

            @Override
            public boolean statusExpected() {
                return false;
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock);

        // Check the reply
        assertEquals(ExchangeStatus.DONE, businessStatus.getStatus());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_ARCHIVER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid in-only request where the sub-service
     * returns a status ERROR.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a status ERROR,</li>
     * <li>a status ERROR is returned,</li>
     * </ul>
     */
    @Test
    public void validInOnlyRequest_WithStatusError() throws Exception {

        final String factureId = "my-facture-id";
        final String myErrorMsg = "My error message about facture '" + factureId + "'";

        final Archiver businessRequestBean = new Archiver();
        businessRequestBean.setIdentifiant(factureId);
        final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
        final DataHandler dh = new DataHandler(ds);
        businessRequestBean.setDocument(dh);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_ARCHIVER, AbsItfOperation.MEPPatternConstants.IN_ONLY.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.IN_ONLY_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_ARCHIVER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Archiver);
                final org.ow2.petals.se.mapping.unit_test.ged.Archiver technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Archiver) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                // We return the status DONE
                return new StatusToConsumerMessage(technicalRequest, new Exception(myErrorMsg));
            }

            @Override
            public boolean statusExpected() {
                return false;
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock);

        // Check the reply
        assertEquals(ExchangeStatus.ERROR, businessStatus.getStatus());
        assertEquals(myErrorMsg, businessStatus.getError().getCause().getMessage());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_ARCHIVER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid robust-in-only request where the
     * sub-service returns a status DONE.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a status DONE,</li>
     * <li>a status DONE is returned,</li>
     * </ul>
     */
    @Test
    public void validRobustInOnlyRequest_WithStatusDone() throws Exception {

        final String factureId = "my-facture-id";

        final Archiver businessRequestBean = new Archiver();
        businessRequestBean.setIdentifiant(factureId);
        final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
        final DataHandler dh = new DataHandler(ds);
        businessRequestBean.setDocument(dh);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_ARCHIVER, AbsItfOperation.MEPPatternConstants.ROBUST_IN_ONLY.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.ROBUST_IN_ONLY_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_ARCHIVER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Archiver);
                final org.ow2.petals.se.mapping.unit_test.ged.Archiver technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Archiver) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                // We return the status DONE
                return new StatusToConsumerMessage(technicalRequest, ExchangeStatus.DONE);
            }

            @Override
            public boolean statusExpected() {
                return false;
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock);

        // Check the reply
        assertEquals(ExchangeStatus.DONE, businessStatus.getStatus());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_ARCHIVER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid robust-in-only request where the
     * sub-service returns a status ERROR.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a status ERROR,</li>
     * <li>a status ERROR is returned,</li>
     * </ul>
     */
    @Test
    public void validRobustInOnlyRequest_WithStatusError() throws Exception {

        final String factureId = "my-facture-id";
        final String myErrorMsg = "My error message about facture '" + factureId + "'";

        final Archiver businessRequestBean = new Archiver();
        businessRequestBean.setIdentifiant(factureId);
        final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
        final DataHandler dh = new DataHandler(ds);
        businessRequestBean.setDocument(dh);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_ARCHIVER, AbsItfOperation.MEPPatternConstants.ROBUST_IN_ONLY.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.ROBUST_IN_ONLY_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_ARCHIVER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Archiver);
                final org.ow2.petals.se.mapping.unit_test.ged.Archiver technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Archiver) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                // We return the status DONE
                return new StatusToConsumerMessage(technicalRequest, new Exception(myErrorMsg));
            }

            @Override
            public boolean statusExpected() {
                return false;
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final StatusMessage businessStatus = COMPONENT.sendAndGetStatus(businessRequest, technicalServiceMock);

        // Check the reply
        assertEquals(ExchangeStatus.ERROR, businessStatus.getStatus());
        assertEquals(myErrorMsg, businessStatus.getError().getCause().getMessage());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_ARCHIVER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }

    /**
     * <p>
     * Check the message processing where the service is invoked with a valid robust-in-only request where the
     * sub-service returns a fault.
     * </p>
     * <p>
     * Expected results:
     * </p>
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a fault,</li>
     * <li>a fault transformed is returned,</li>
     * </ul>
     */
    @Test
    public void validRobustInOnlyRequest_WithFaultTransformedInFault() throws Exception {

        final String factureId = "my-facture-id";

        final Archiver businessRequestBean = new Archiver();
        businessRequestBean.setIdentifiant(factureId);
        final DataSource ds = new ByteArrayDataSource("My attached file content", "text/plain");
        final DataHandler dh = new DataHandler(ds);
        businessRequestBean.setDocument(dh);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_ARCHIVER, AbsItfOperation.MEPPatternConstants.ROBUST_IN_ONLY.value(), businessRequestBean,
                MARSHALLER);

        final ServiceProviderImplementation technicalServiceMock = new ServiceProviderImplementation() {
            private MessageExchange technicalMessageExchange;

            @Override
            public Message provides(final RequestMessage technicalRequest) throws Exception {

                this.technicalMessageExchange = technicalRequest.getMessageExchange();
                assertNotNull(this.technicalMessageExchange);
                assertEquals(ExchangeStatus.ACTIVE, this.technicalMessageExchange.getStatus());
                assertEquals(MEPConstants.ROBUST_IN_ONLY_PATTERN.value(), this.technicalMessageExchange.getPattern());
                assertEquals(Role.PROVIDER, technicalRequest.getMessageExchange().getRole());
                assertEquals(GED_INTERFACE, this.technicalMessageExchange.getInterfaceName());
                assertEquals(GED_SERVICE, this.technicalMessageExchange.getService());
                assertNotNull(this.technicalMessageExchange.getEndpoint());
                assertEquals(GED_ENDPOINT, this.technicalMessageExchange.getEndpoint().getEndpointName());
                assertEquals(GED_ARCHIVER_OPERATION, this.technicalMessageExchange.getOperation());
                final Object technicalRequestObj = UNMARSHALLER.unmarshal(technicalRequest.getPayload());
                assertTrue(technicalRequestObj instanceof org.ow2.petals.se.mapping.unit_test.ged.Archiver);
                final org.ow2.petals.se.mapping.unit_test.ged.Archiver technicalRequestBean = (org.ow2.petals.se.mapping.unit_test.ged.Archiver) technicalRequestObj;
                assertEquals(factureId, technicalRequestBean.getReference());

                final org.ow2.petals.se.mapping.unit_test.ged.DocumentExistant technicalFault = new org.ow2.petals.se.mapping.unit_test.ged.DocumentExistant();
                technicalFault.setReference(technicalRequestBean.getReference());

                // We return the response
                return new FaultToConsumerMessage(technicalRequest, technicalFault, MARSHALLER);
            }

            @Override
            public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                // Assert the status DONE on the message exchange
                assertNotNull(statusDoneMsg);
                // It's the same message exchange instance
                assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                assertEquals(ExchangeStatus.DONE, statusDoneMsg.getMessageExchange().getStatus());
            }

        };

        // We send the business request to the mapping service, and we check that the technical service receives the
        // right request
        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest, technicalServiceMock);

        // Check the reply
        assertTrue(businessResponse.isFault());
        final Source out = businessResponse.getOut();
        assertNull("Unexpected OUT message", (out == null ? null : SourceHelper.toString(out)));
        assertNotNull("No XML payload in fault", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof FactureExistante);
        final FactureExistante businessResponseBean = (FactureExistante) businessResponseBeanObj;
        assertEquals(factureId, businessResponseBean.getIdentifiant());

        // We check that the technical service has received the right status DONE
        COMPONENT.receiveStatusAsExternalProvider(technicalServiceMock);

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(4, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_ARCHIVER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderFailureLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderFailureLog(businessBeginFlowLogData, monitLogs_1.get(3));
    }
}
