/**
 * Copyright (c) 2016-2019 Linagora
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.LogRecord;

import javax.jbi.messaging.ExchangeStatus;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation;
import org.ow2.petals.commons.log.FlowLogData;
import org.ow2.petals.commons.log.Level;
import org.ow2.petals.component.framework.junit.Message;
import org.ow2.petals.component.framework.junit.RequestMessage;
import org.ow2.petals.component.framework.junit.StatusMessage;
import org.ow2.petals.component.framework.junit.helpers.MessageChecks;
import org.ow2.petals.component.framework.junit.helpers.ServiceProviderImplementation;
import org.ow2.petals.component.framework.junit.helpers.SimpleComponent;
import org.ow2.petals.component.framework.junit.impl.ConsumesServiceConfiguration;
import org.ow2.petals.component.framework.junit.impl.ProvidesServiceConfiguration;
import org.ow2.petals.component.framework.junit.impl.ServiceConfiguration;
import org.ow2.petals.component.framework.junit.impl.message.RequestToProviderMessage;
import org.ow2.petals.component.framework.junit.impl.message.ResponseToConsumerMessage;
import org.ow2.petals.component.framework.junit.rule.ComponentUnderTest;
import org.ow2.petals.component.framework.junit.rule.ServiceConfigurationFactory;
import org.ow2.petals.component.framework.util.ExchangeUtil;
import org.ow2.petals.junit.rules.log.handler.InMemoryLogHandler;
import org.ow2.petals.se.mapping.unit_test.facture.Consulter;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Unit tests about MONIT trace filtering.
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public class MonitTraceFilteringTest extends AbstractEnv {

    private static final InMemoryLogHandler IN_MEMORY_LOG_HANDLER = new InMemoryLogHandler();

    private static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    private static final ComponentUnderTest COMPONENT_UNDER_TEST = new ComponentUnderTest()
            .addLogHandler(IN_MEMORY_LOG_HANDLER.getHandler())
            .registerExternalServiceProvider(GED_ENDPOINT, GED_SERVICE, GED_INTERFACE);

    private static final SimpleComponent COMPONENT = new SimpleComponent(COMPONENT_UNDER_TEST);

    @ClassRule
    public static final TestRule chain = RuleChain.outerRule(TEMP_FOLDER).around(IN_MEMORY_LOG_HANDLER)
            .around(COMPONENT_UNDER_TEST);

    /**
     * All log traces must be cleared before starting a unit test
     */
    @Before
    public void clearLogTraces() {
        IN_MEMORY_LOG_HANDLER.clear();
    }

    /**
     * <p>
     * Check the MONIT trace filtering.
     * </p>
     * <p>
     * Note: According to the Petals CDK JUnit, the flow tracing configuration of the <b>service provider consumed</b>
     * depends on the received exchange. If it contains the property
     * {@value org.ow2.petals.component.framework.api.Message#FLOW_TRACING_ACTIVATION_MSGEX_PROP}, its value is used,
     * otherwise the flow tracing is enabled.
     * </p>
     */
    @Test
    public void monitTracesFiltering() throws Exception {

        final URL monitTraceFilteringRulesURL = Thread.currentThread().getContextClassLoader()
                .getResource("monitTraceFilteringRules.csv");
        assertNotNull(monitTraceFilteringRulesURL);
        final List<MonitTraceFilteringRule> monitTraceFilteringRules = this
                .readMonitTraceFileteringRules(new File(monitTraceFilteringRulesURL.toURI()));
        int ruleCpt = 0;
        for (final MonitTraceFilteringRule rule : monitTraceFilteringRules) {
            this.executeRule(rule, ++ruleCpt);
        }
    }

    /**
     * Read the given MONIT flow tracing rule file.
     * 
     * @param monitTraceFilteringRulesFile
     *            Name of file containing MONIT flow tracing rules
     * @return All MONIT flow tracing rules defined
     * @throws IOException
     *             An error occurs reading the file
     */
    private List<MonitTraceFilteringRule> readMonitTraceFileteringRules(final File monitTraceFilteringRulesFile)
            throws IOException {

        final CellProcessor[] processors = new CellProcessor[] {
                // component configuration - flow tracing activation
                new StrNotNullOrEmpty(
                        new IsIncludedIn(new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString(), "-" })),
                // component configuration - flow tracing propagation
                new StrNotNullOrEmpty(
                        new IsIncludedIn(new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString(), "-" })),
                // service provider configuration - flow tracing activation
                new StrNotNullOrEmpty(
                        new IsIncludedIn(new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString(), "-" })),
                // service consumer configuration - flow tracing propagation
                new StrNotNullOrEmpty(
                        new IsIncludedIn(new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString(), "-" })),
                // exchange configuration - flow tracing activation
                new StrNotNullOrEmpty(
                        new IsIncludedIn(new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString(), "-" })),
                // expected results - MONIT traces logged for service provider
                new StrNotNullOrEmpty(new IsIncludedIn(new String[] { "Yes", "No" })),
                // expected results - MONIT traces logged for service provider consumed
                new StrNotNullOrEmpty(new IsIncludedIn(new String[] { "Yes", "No" })),
                // expected results - Flow tracing activation state in outgoing exchange
                new StrNotNullOrEmpty(new IsIncludedIn(new String[] { "Enabled", "Disabled", "Empty" })) };

        final List<MonitTraceFilteringRule> monitTraceFilteringRules = new ArrayList<>();
        try (final ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(monitTraceFilteringRulesFile),
                new CsvPreference.Builder('"', ',', "\r\n").skipComments(new CommentStartsWith("#")).build())) {

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);

            monitTraceFilteringRules.clear();
            MonitTraceFilteringRule rule;
            while ((rule = beanReader.read(MonitTraceFilteringRule.class, header, processors)) != null) {
                monitTraceFilteringRules.add(rule);
            }
        }
        return monitTraceFilteringRules;
    }

    /**
     * Execute a flow tracing filtering rule
     * 
     * @param compEnableFlowTracing
     *            Enable/Disable flow tracing activation at component configuration level,
     * @param compEnableFlowTracingPropagation
     *            Enable/Disable flow tracing propagation at component configuration level,
     * @param provEnableFlowTracing
     *            Enable/Disable flow tracing activation at service provider definition level,
     * @param consEnableFlowTracingPropagation
     *            Enable/Disable flow tracing propagation at service consumer definition level,
     * @param msgEnableFlowTracing
     *            Enable/Disable flow tracing activation at incoming exchange,
     * @param expectedFlowTracingActivationState
     *            Expected activation state in the outgoing exchange
     * @param isProvMonitTraceLogged
     *            Is the MONIT traces of the service provider logged ?
     * @param isConsMonitTraceLogged
     *            Is the MONIT traces of the service provider consumed logged ?
     */
    private void executeRule(final MonitTraceFilteringRule rule, final int ruleIdx) throws Exception {
        final Optional<Boolean> compEnableFlowTracing = parseAsOptional(rule.compEnableFlowTracing);
        final Optional<Boolean> compEnableFlowTracingPropagation = parseAsOptional(
                rule.compEnableFlowTracingPropagation);
        final Optional<Boolean> provEnableFlowTracing = parseAsOptional(rule.provEnableFlowTracing);
        final Optional<Boolean> consEnableFlowTracingPropagation = parseAsOptional(
                rule.consEnableFlowTracingPropagation);
        final Optional<Boolean> msgEnableFlowTracing = parseAsOptional(rule.msgEnableFlowTracing);
        final Optional<Boolean> expectedFlowTracingActivationState = parseExpectedResultAsOptional(
                rule.expectedFlowTracingActivationState);
        final boolean isProvMonitTraceLogged = parseExpectedResultAsBool(rule.isProvMonitTraceLogged);
        final boolean isConsMonitTraceLogged = parseExpectedResultAsBool(rule.isConsMonitTraceLogged);

        IN_MEMORY_LOG_HANDLER.clear();

        if (compEnableFlowTracing.isPresent()) {
            COMPONENT_UNDER_TEST.setRuntimeParameter("activateFlowTracing",
                    Boolean.toString(compEnableFlowTracing.get()));
        } else {
            // Flow tracing activation is set with its default value
            COMPONENT_UNDER_TEST.setRuntimeParameter("activateFlowTracing", Boolean.TRUE.toString());
        }
        if (compEnableFlowTracingPropagation.isPresent()) {
            COMPONENT_UNDER_TEST.setRuntimeParameter("propagateFlowTracingActivation",
                    Boolean.toString(compEnableFlowTracingPropagation.get()));
        } else {
            // Flow tracing propagation is set with its default value
            COMPONENT_UNDER_TEST.setRuntimeParameter("propagateFlowTracingActivation", Boolean.TRUE.toString());
        }

        COMPONENT_UNDER_TEST.deployService(VALID_SU, new ServiceConfigurationFactory() {
            @Override
            public ServiceConfiguration create() {

                final URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("su/valid/facture.wsdl");
                assertNotNull("Rule #" + ruleIdx + ": WSDl not found", wsdlUrl);
                final ProvidesServiceConfiguration serviceConfiguration = new ProvidesServiceConfiguration(
                        FACTURE_INTERFACE, FACTURE_SERVICE, FACTURE_ENDPOINT, wsdlUrl);
                if (provEnableFlowTracing.isPresent()) {
                    serviceConfiguration.setParameter(
                            new QName("http://petals.ow2.org/components/extensions/version-5", "activate-flow-tracing"),
                            Boolean.toString(provEnableFlowTracing.get()));
                }

                final URL inputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/input-archiver.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'input-archiver.xsl' not found", inputArchiverXslUrl);
                serviceConfiguration.addResource(inputArchiverXslUrl);

                final URL outputArchiverXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/output-archiver.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'output-archiver.xsl' not found", outputArchiverXslUrl);
                serviceConfiguration.addResource(outputArchiverXslUrl);

                final URL inputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/input-consulter.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'input-consulter.xsl' not found", inputConsulterXslUrl);
                serviceConfiguration.addResource(inputConsulterXslUrl);

                final URL outputConsulterXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/output-consulter.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'output-consulter.xsl' not found", outputConsulterXslUrl);
                serviceConfiguration.addResource(outputConsulterXslUrl);

                final URL inputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/input-supprimer.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'input-supprimer.xsl' not found", inputSupprimerXslUrl);
                serviceConfiguration.addResource(inputSupprimerXslUrl);

                final URL outputSupprimerXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/output-supprimer.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'output-supprimer.xsl' not found", outputSupprimerXslUrl);
                serviceConfiguration.addResource(outputSupprimerXslUrl);

                final URL outputOut2FaultXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/output-out2fault.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'output-out2fault.xsl' not found", outputOut2FaultXslUrl);
                serviceConfiguration.addResource(outputOut2FaultXslUrl);

                final URL outputFault2OutXslUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("su/valid/output-fault2out.xsl");
                assertNotNull("Rule #" + ruleIdx + ": XSL 'output-fault2out.xsl' not found", outputFault2OutXslUrl);
                serviceConfiguration.addResource(outputFault2OutXslUrl);

                // Technical consumed service 'ged'
                final ConsumesServiceConfiguration consumeServiceConfiguration = new ConsumesServiceConfiguration(
                        GED_INTERFACE, GED_SERVICE, GED_ENDPOINT);
                if (consEnableFlowTracingPropagation.isPresent()) {
                    consumeServiceConfiguration.setParameter(
                            new QName("http://petals.ow2.org/components/extensions/version-5",
                                    "propagate-flow-tracing-activation"),
                            Boolean.toString(consEnableFlowTracingPropagation.get()));
                }
                serviceConfiguration.addServiceConfigurationDependency(consumeServiceConfiguration);

                return serviceConfiguration;
            }
        });

        try {
            // Send the valid request
            final Properties requestProps = new Properties();
            if (msgEnableFlowTracing.isPresent()) {
                requestProps.put(org.ow2.petals.component.framework.api.Message.FLOW_TRACING_ACTIVATION_MSGEX_PROP,
                        msgEnableFlowTracing.get());
            }
            final RequestToProviderMessage businessRequest = new RequestToProviderMessage(COMPONENT_UNDER_TEST,
                    VALID_SU, OPERATION_CONSULTER, AbsItfOperation.MEPPatternConstants.IN_OUT.value(), new Consulter(),
                    MARSHALLER, requestProps);

            // We send the business request to the mapping service, wait the response and return status
            COMPONENT.sendAndCheckResponseAndSendStatus(businessRequest, new ServiceProviderImplementation() {
                @Override
                public Message provides(final RequestMessage technicalRequest) throws Exception {

                    assertEquals("Rule #" + ruleIdx + ": ", expectedFlowTracingActivationState,
                            ExchangeUtil.isFlowTracingActivated(technicalRequest.getMessageExchange()));

                    // We return the response
                    return new ResponseToConsumerMessage(technicalRequest,
                            new org.ow2.petals.se.mapping.unit_test.ged.ConsulterResponse(), MARSHALLER);
                }

                @Override
                public void handleStatus(final StatusMessage statusDoneMsg) throws Exception {
                    // Assertions about status are skipped because it's not the goal of this unit test.
                }
            }, new MessageChecks() {
                @Override
                public void checks(Message message) throws Exception {
                    // No check about response is done here because it's not the goal of this unit test.
                }
            }, ExchangeStatus.DONE);

            // Check MONIT traces
            final List<LogRecord> monitLogs = IN_MEMORY_LOG_HANDLER.getAllRecords(Level.MONIT);
            if (isProvMonitTraceLogged) {
                assertTrue("Rule #" + ruleIdx + ": ", monitLogs.size() >= 2);
                final FlowLogData businessBeginFlowLogData = assertMonitProviderBeginLog("Rule #" + ruleIdx + ": ",
                        FACTURE_INTERFACE, FACTURE_SERVICE, FACTURE_ENDPOINT, OPERATION_CONSULTER, monitLogs.get(0));
                if (isConsMonitTraceLogged) {
                    assertEquals("Rule #" + ruleIdx + ": ", 4, monitLogs.size());
                    final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog("Rule #" + ruleIdx + ": ",
                            businessBeginFlowLogData, GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION,
                            monitLogs.get(1));
                    assertMonitProviderEndLog("Rule #" + ruleIdx + ": ", technicalBeginFlowLogData, monitLogs.get(2));
                    assertMonitProviderEndLog("Rule #" + ruleIdx + ": ", businessBeginFlowLogData, monitLogs.get(3));
                } else {
                    assertEquals("Rule #" + ruleIdx + ": ", 2, monitLogs.size());
                    assertMonitProviderEndLog("Rule #" + ruleIdx + ": ", businessBeginFlowLogData, monitLogs.get(1));
                }
            } else if (isConsMonitTraceLogged) {
                assert !isProvMonitTraceLogged;

                assertEquals("Rule #" + ruleIdx + ": ", 2, monitLogs.size());
                final FlowLogData technicalBeginFlowLogData = assertMonitProviderBeginLog("Rule #" + ruleIdx + ": ",
                        GED_INTERFACE, GED_SERVICE, GED_ENDPOINT, GED_CONSULTER_OPERATION, monitLogs.get(0));
                assertMonitProviderEndLog("Rule #" + ruleIdx + ": ", technicalBeginFlowLogData, monitLogs.get(1));
            } else {
                assertEquals("Rule #" + ruleIdx + ": ", 0, monitLogs.size());
            }

        } finally {
            COMPONENT_UNDER_TEST.undeployService(VALID_SU);
        }
    }

    private static Optional<Boolean> parseAsOptional(final String value) {
        if (value.equals("true")) {
            return Optional.of(Boolean.TRUE);
        } else if (value.equals("false")) {
            return Optional.of(Boolean.FALSE);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> parseExpectedResultAsOptional(final String value) {
        if (value.equals("Enabled")) {
            return Optional.of(Boolean.TRUE);
        } else if (value.equals("Disabled")) {
            return Optional.of(Boolean.FALSE);
        } else {
            return Optional.empty();
        }
    }

    private static boolean parseExpectedResultAsBool(final String value) {
        if (value.equals("Yes")) {
            return true;
        } else {
            return false;
        }
    }
}
