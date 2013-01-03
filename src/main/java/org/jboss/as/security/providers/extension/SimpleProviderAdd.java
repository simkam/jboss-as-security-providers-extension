/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.security.providers.extension;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * A handler for adding a simple Java Security Provider.
 * 
 * @author Josef Cacek
 */
class SimpleProviderAdd extends AbstractAddStepHandler {

    public static final SimpleProviderAdd INSTANCE = new SimpleProviderAdd();

    // Constructors ----------------------------------------------------------

    /**
     * Private ctor.
     */
    private SimpleProviderAdd() {
    }

    // Public methods --------------------------------------------------------

    /**
     * Model population.
     * 
     * @param operation
     * @param model
     * @throws OperationFailedException
     * @see org.jboss.as.controller.AbstractAddStepHandler#populateModel(org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode)
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    }

    /**
     * Creates and registers {@link SimpleProviderService} instance with the given configuration.
     * 
     * @param context
     * @param operation
     * @param model
     * @param verificationHandler
     * @param newControllers
     * @throws OperationFailedException
     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext,
     *      org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler,
     *      java.util.List)
     */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        String providerClassName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement()
                .getValue();
        SimpleProviderService service = new SimpleProviderService(providerClassName);
        ServiceName name = SimpleProviderService.createServiceName(providerClassName);
        ServiceController<SimpleProviderService> controller = context.getServiceTarget().addService(name, service)
                .addListener(verificationHandler).setInitialMode(Mode.ACTIVE).install();
        newControllers.add(controller);
    }
}