/**
 * Copyright (c) 2016-2018 Linagora
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
package org.ow2.petals.se.mapping;

import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.ow2.petals.component.framework.api.exception.PEtALSCDKException;
import org.ow2.petals.component.framework.jbidescriptor.generated.Consumes;
import org.ow2.petals.component.framework.jbidescriptor.generated.Jbi;
import org.ow2.petals.component.framework.jbidescriptor.generated.Provides;
import org.ow2.petals.component.framework.se.AbstractServiceEngine;
import org.ow2.petals.component.framework.se.ServiceEngineServiceUnitManager;
import org.ow2.petals.component.framework.su.ServiceUnitDataHandler;
import org.ow2.petals.component.framework.util.ServiceEndpointOperationKey;
import org.ow2.petals.se.mapping.incoming.AnnotatedWsdlParser;
import org.ow2.petals.se.mapping.incoming.exception.InvalidAnnotationException;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;
import org.w3c.dom.Document;

/**
 * @author Christophe DENEUX - Linagora
 */
public class MappingSuManager extends ServiceEngineServiceUnitManager {

    /**
     * Default constructor.
     * 
     * @param component
     *            the mapping component
     */
    public MappingSuManager(final AbstractServiceEngine component) {
        super(component);
    }

    @Override
    protected void doDeploy(final ServiceUnitDataHandler suDH) throws PEtALSCDKException {
        if (this.logger.isLoggable(Level.FINE)&& suDH != null) {
            this.logger.log(Level.FINE, "Deploying specific part of SU = {0} ...", suDH.getName() );
        }

        final Jbi jbiDescriptor = suDH.getDescriptor();

        // Check the JBI descriptor
        if (jbiDescriptor == null || jbiDescriptor.getServices() == null
                || jbiDescriptor.getServices().getProvides() == null
                || jbiDescriptor.getServices().getProvides().isEmpty()) {
            // TODO: This check should be done at CDK level: all SUs deployed on a SE must have at least one section
            // 'provides' ?
            throw new PEtALSCDKException("Invalid JBI descriptor: it does not contain a 'provides' section.");
        }

        // Check that only one Consumes section is declared in the SU
        if (jbiDescriptor.getServices().getConsumes().size() != 1) {
            throw new PEtALSCDKException(
                    "Invalid JBI descriptor: one and only one 'consumes' section must be defined.");
        }
        final Consumes serviceProvider = jbiDescriptor.getServices().getConsumes().get(0);
        if (serviceProvider.getOperation() != null && this.logger.isLoggable(Level.WARNING)) {
                this.logger.log(Level.WARNING,
                        "An operation '{0}' is defined into the 3PP service provider definition in the JBI descriptor. It won't be used, remove it !!",
                        serviceProvider.getOperation());
            }
        }

        // Check that there is only one Provides section in the SU
        if (jbiDescriptor.getServices().getProvides().size() != 1) {
            throw new PEtALSCDKException("Invalid JBI descriptor: it must not have more than one 'provides' section.");
        }

        // Get the provides
        final Provides provides = jbiDescriptor.getServices().getProvides().get(0);
        if (provides == null) {
            throw new PEtALSCDKException("Invalid JBI descriptor: the 'provides' section is invalid.");
        }

        // Create mapping operations
        final Document wsdl = suDH.getEndpointDescription(provides);
        final List<MappingOperation> mappingOperations = this.createMappingOperations(wsdl, suDH.getInstallRoot(),
                suDH.getName(), serviceProvider);

        // Enable mapping operations
        final String edptName = provides.getEndpointName();
        final QName serviceName = provides.getServiceName();
        for (final MappingOperation mappingOperation : mappingOperations) {
            // Store the AbstractSendService in the map with the corresponding end-point
            final ServiceEndpointOperationKey eptAndOperation = new ServiceEndpointOperationKey(serviceName, edptName,
                    mappingOperation.getWsdlOperation());
            this.getComponent().registerMappingService(eptAndOperation, mappingOperation);
        }

        this.getComponent().logEptOperationToMappingOperation();

        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.log(Level.FINE, "Specific part of SU = {0}) deployed.", suDH.getName() );
        }
    }

    @Override
    protected void doUndeploy(final ServiceUnitDataHandler suDH) throws PEtALSCDKException {
        this.logger.log(Level.FINE, "Undeploying specific part of SU = {0}) ...", suDH.getName());
        try {
            final String edptName = suDH.getDescriptor().getServices().getProvides().iterator().next()
                    .getEndpointName();
            // Remove the mapping operations in the map with the corresponding end-point
            this.getComponent().removeMappingOPerations(edptName);
        } finally {
            this.logger.log(Level.FINE, "Specific part of SU = {0}) undeployed", suDH.getName());
        }
    }

    @Override
    protected MappingSE getComponent() {
        return (MappingSE) super.getComponent();
    }

    /**
     * Create the mapping operations ({@link MappingOperation} reading annotations of the WSDL
     * 
     * @param wsdlDocument
     *            The WSDL to parse to create mapping operations
     * @param suRootPath
     *            The root directory of the service unit
     * @param suName
     *            Name of the current service unit
     * @param serviceProvider
     *            The service to invoke declared in the service unit
     * @return The list of {@link MappingOperation} created from the given WSDL. Not {@code null}.
     * @throws MappingDeclarationException
     *             An error was detected about annotations
     */
    private List<MappingOperation> createMappingOperations(final Document wsdlDocument, final String suRootPath,
            final String suName, final Consumes serviceProvider) {

        final AnnotatedWsdlParser annotatedWdslParser = new AnnotatedWsdlParser(this.logger);

        final List<MappingOperation> mappingOperations = annotatedWdslParser.parse(wsdlDocument, suRootPath, suName,
                serviceProvider);

        // Log all WSDL errors
        if (this.logger.isLoggable(Level.WARNING)) {
            for (final InvalidAnnotationException encounteredError : annotatedWdslParser.getEncounteredErrors()) {
                this.logger.warning(encounteredError.getMessage());
            }
        }

        return mappingOperations;

    }

}
