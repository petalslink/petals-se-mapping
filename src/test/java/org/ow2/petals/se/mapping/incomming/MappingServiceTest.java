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
package org.ow2.petals.se.mapping.incomming;

import java.util.List;
import java.util.logging.LogRecord;

import javax.activation.DataHandler;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
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
import org.ow2.petals.component.framework.junit.impl.message.RequestToProviderMessage;
import org.ow2.petals.component.framework.junit.impl.message.ResponseToConsumerMessage;
import org.ow2.petals.se.mapping.unit_test.facture.Consulter;
import org.ow2.petals.se.mapping.unit_test.facture.ConsulterResponse;

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
     * Check the message processing where the service is invoked with a valid request
     * </p>
     * <p>
     * Expected results:
     * <ul>
     * <li>the request is correctly transformed,</li>
     * <li>the sub-service is correctly called and returns a nominal reply,</li>
     * <li>the response is correctly transformed,</li>
     * </ul>
     * </p>
     */
    @Test
    public void validRequest() throws Exception {

        final Consulter businessRequestBean = new Consulter();
        final String factureId = "my-facture-id";
        businessRequestBean.setIdentifiant(factureId);

        // Send the valid request
        final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST, VALID_SU,
                OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(),
                this.toByteArray(businessRequestBean));

        final ResponseMessage businessResponse = COMPONENT.sendAndGetResponse(businessRequest,
                new ServiceProviderImplementation() {
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
                        assertEquals(factureId, technicalRequestBean.getReference());

                        final org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse technicalConsulterResponse = new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse();
                        technicalConsulterResponse
                                .setDocument(new DataHandler("My attached file content", "text/plain"));

                        // We return the response
                        return new ResponseToConsumerMessage(technicalRequest,
                                new String(MappingServiceTest.this.toByteArray(technicalConsulterResponse)));
                    }

                    @Override
                    public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                        // Assert the status DONE on the message exchange
                        assertNotNull(statusDoneMsg);
                        // It's the same message exchange instance
                        assertSame(statusDoneMsg.getMessageExchange(), this.technicalMessageExchange);
                        assertEquals(statusDoneMsg.getMessageExchange().getStatus(), ExchangeStatus.DONE);
                    }

                });

        // Check the reply
        assertNotNull(businessResponse);
        final Source fault = businessResponse.getFault();
        assertNull("Unexpected fault", (fault == null ? null : SourceHelper.toString(fault)));
        assertNotNull("No XML payload in response", businessResponse.getPayload());
        final Object businessResponseBeanObj = UNMARSHALLER.unmarshal(businessResponse.getPayload());
        assertTrue(businessResponseBeanObj instanceof ConsulterResponse);
        final ConsulterResponse businessResponseBean = (ConsulterResponse) businessResponseBeanObj;
        assertNotNull(businessResponseBean.getDocument());

        // Check MONIT traces
        final List<LogRecord> monitLogs_1 = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
        assertEquals(2, monitLogs_1.size());
        final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog(FACTURE_INTERFACE, FACTURE_SERVICE,
                FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs_1.get(0));
        final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog(businessBeginFlowLogData,
                GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_ARCHIVER_OPERATION, monitLogs_1.get(1));
        assertMonitProviderEndLog(technicalBeginFlowLogData, monitLogs_1.get(2));
        assertMonitProviderEndLog(businessBeginFlowLogData, monitLogs_1.get(3));

        COMPONENT.sendDoneStatus(businessResponse);
    }
}
