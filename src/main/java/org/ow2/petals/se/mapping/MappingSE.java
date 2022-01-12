/**
 * Copyright (c) 2016-2022 Linagora
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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.ow2.petals.component.framework.se.AbstractServiceEngine;
import org.ow2.petals.component.framework.se.ServiceEngineServiceUnitManager;
import org.ow2.petals.component.framework.util.ServiceEndpointOperationKey;
import org.ow2.petals.se.mapping.incoming.operation.MappingOperation;

/**
 * The component class of the Mapping Service Engine.
 * 
 * @author Christophe DENEUX - Linagora
 */
public class MappingSE extends AbstractServiceEngine {

    /**
     * A map used to get the mapping operations associated with (end-point name + operation)
     */
    private final Map<ServiceEndpointOperationKey, MappingOperation> mappingOperations = new ConcurrentHashMap<>();

    /**
     * Register a mapping operation.
     * 
     * @param eptAndOperation
     *            the end-point Name and operation Name
     * @param mappingOperation
     *            the mapping operation implementation
     */
    public void registerMappingService(final ServiceEndpointOperationKey eptAndOperation,
            final MappingOperation mappingOperation) {
        this.mappingOperations.put(eptAndOperation, mappingOperation);
    }

    /**
     * Retrieve the mapping operation associated to the given {@code ServiceEndpointOperationKey}.
     * 
     * @param eptAndOperation
     *            the end-point name and operation name
     * @return the mapping operation associated with this end-point name and operation name
     */
    public MappingOperation getMappingOperation(final ServiceEndpointOperationKey eptAndOperation) {
        return this.mappingOperations.get(eptAndOperation);
    }

    /**
     * Remove all mapping operations associated to the given end-point.
     * 
     * @param eptName
     *            the end-point name
     */
    public void removeMappingOPerations(final String eptName) {

        final Iterator<Entry<ServiceEndpointOperationKey, MappingOperation>> itEptOperationToMappingOperation = this.mappingOperations
                .entrySet().iterator();
        while (itEptOperationToMappingOperation.hasNext()) {
            final Entry<ServiceEndpointOperationKey, MappingOperation> entry = itEptOperationToMappingOperation.next();
            if (entry.getKey().getEndpointName().equals(eptName)) {
                itEptOperationToMappingOperation.remove();
            }
        }
    }

    public void logEptOperationToMappingOperation() {
        if (this.getLogger().isLoggable(Level.CONFIG)) {
            for (final Map.Entry<ServiceEndpointOperationKey, MappingOperation> entry : this.mappingOperations
                    .entrySet()) {
                final ServiceEndpointOperationKey key = entry.getKey();
                this.getLogger().config("*** Endpoint Operation ");
                this.getLogger().config(key.toString());
                this.getLogger().config("------------------------------------------------------ ");
                entry.getValue().log();
                this.getLogger().config("******************* ");
            }
        }
    }

    @Override
    protected ServiceEngineServiceUnitManager createServiceUnitManager() {
        return new MappingSuManager(this);
    }
}
