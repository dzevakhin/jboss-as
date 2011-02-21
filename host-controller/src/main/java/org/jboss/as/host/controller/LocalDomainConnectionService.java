/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.host.controller;

import org.jboss.as.controller.OperationResult;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

import java.util.concurrent.CancellationException;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.domain.controller.DomainController;
import org.jboss.as.domain.controller.FileRepository;
import org.jboss.as.domain.controller.HostControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Emanuel Muckenhuber
 */
class LocalDomainConnectionService implements DomainControllerConnection, Service<DomainControllerConnection> {

    final InjectedValue<DomainController> domainController = new InjectedValue<DomainController>();
    private String name;

    /** {@inheritDoc} */
    @Override
    public synchronized ModelNode register(final HostController hostController) {
        assert hostController != null : "null HC";
        final DomainController domainController = this.domainController.getValue();
        this.name = hostController.getName();
        final HostControllerClient client = new LocalHostControllerClient(hostController);
        domainController.addClient(client);
        return domainController.getDomainModel();
    }


    /** {@inheritDoc} */
    @Override
    public ModelNode getProfileOperations(String profileName) {
        assert profileName != null : "Null profile name";
        final DomainController domainController = this.domainController.getValue();
        ModelNode operation = new ModelNode();

        operation.get(OP).set(DESCRIBE);
        operation.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement(PROFILE, profileName)).toModelNode());

        try {
            return domainController.execute(operation).require(RESULT);
        } catch (CancellationException e) {
            // AutoGenerated
            throw new RuntimeException(e);
        } catch (OperationFailedException e) {
            // AutoGenerated
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void unregister() {
        final DomainController domainController = this.domainController.getValue();
        domainController.removeClient(name);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized FileRepository getRemoteFileRepository() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void start(StartContext context) throws StartException {
        this.domainController.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void stop(StopContext context) {
        //
    }

    /** {@inheritDoc} */
    @Override
    public synchronized DomainControllerConnection getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    InjectedValue<DomainController> getDomainController() {
        return domainController;
    }

    static class LocalHostControllerClient implements HostControllerClient {
        private final HostController controller;

        private final PathAddress proxyNodeAddress;
        LocalHostControllerClient(final HostController controller) {
            this.controller = controller;
            this.proxyNodeAddress = PathAddress.pathAddress(PathElement.pathElement(HOST, getId()));
        }

        /** {@inheritDoc} */
        @Override
        public OperationResult execute(ModelNode operation, ResultHandler handler) {
            return this.controller.execute(operation, handler);
        }

        /** {@inheritDoc} */
        @Override
        public ModelNode execute(ModelNode operation) throws CancellationException, OperationFailedException {
            return this.controller.execute(operation);
        }

        /** {@inheritDoc} */
        @Override
        public String getId() {
            return controller.getName();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public PathAddress getProxyNodeAddress() {
            return proxyNodeAddress;
        }
    }
}