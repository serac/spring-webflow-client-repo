/*
  $Id: $

  Copyright (C) 2013 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package edu.vt.middleware.webflow;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * Description of ClientFlowExecutionRepository.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class ClientFlowExecutionRepository implements FlowExecutionRepository, FlowExecutionKeyFactory {

    /** Client flow storage has not backing store independent from the flow key, so no locking is required. */
    private static final FlowExecutionLock NOOP_LOCK = new FlowExecutionLock() {
        public void lock() {}

        public void unlock() {}
    };

    private final FlowExecutionFactory flowExecutionFactory;

    private final FlowDefinitionLocator flowDefinitionLocator;

    private final Transcoder transcoder;

    public ClientFlowExecutionRepository(
            final FlowExecutionFactory flowExecutionFactory,
            final FlowDefinitionLocator flowDefinitionLocator,
            final Transcoder transcoder) {
        Assert.notNull(transcoder, "FlowExecutionFactory cannot be null");
        Assert.notNull(transcoder, "FlowDefinitionLocator cannot be null");
        Assert.notNull(transcoder, "Transcoder cannot be null");
        this.flowExecutionFactory = flowExecutionFactory;
        this.flowDefinitionLocator = flowDefinitionLocator;
        this.transcoder = transcoder;
    }

    public FlowExecutionKey parseFlowExecutionKey(final String encodedKey) throws FlowExecutionRepositoryException {
        return ClientFlowExecutionKey.parse(encodedKey);
    }

    public FlowExecutionLock getLock(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        return NOOP_LOCK;
    }

    public FlowExecution getFlowExecution(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        if (!(key instanceof ClientFlowExecutionKey)) {
            throw new IllegalArgumentException(
                    "Expected instance of ClientFlowExecutionKey but got " + key.getClass().getName());
        }
        final byte[] encoded = ((ClientFlowExecutionKey) key).getData();
        try {
            final SerializedFlowExecutionState state = (SerializedFlowExecutionState) this.transcoder.decode(encoded);
            final FlowDefinition flow = this.flowDefinitionLocator.getFlowDefinition(state.getFlowId());
            return this.flowExecutionFactory.restoreFlowExecution(
                    state.getExecution(), flow, key, state.getConversationScope(), this.flowDefinitionLocator);
        } catch (IOException e) {
            throw new ClientFlowExecutionRepositoryException("Error decoding flow execution", e);
        }
    }

    public void putFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {}

    public void removeFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {}

    public FlowExecutionKey getKey(final FlowExecution execution) {
        try {
            return new ClientFlowExecutionKey(this.transcoder.encode(new SerializedFlowExecutionState(execution)));
        } catch (IOException e) {
            throw new ClientFlowExecutionRepositoryException("Error encoding flow execution", e);
        }
    }

    public void updateFlowExecutionSnapshot(final FlowExecution execution) {}

    public void removeFlowExecutionSnapshot(final FlowExecution execution) {}

    public void removeAllFlowExecutionSnapshots(final FlowExecution execution) {}


    static class SerializedFlowExecutionState implements Serializable {
        private static final long serialVersionUID = -4020991769174829876L;

        private final String flowId;

        private final MutableAttributeMap conversationScope;

        private final FlowExecution execution;

        public SerializedFlowExecutionState(final FlowExecution execution) {
            this.execution = execution;
            this.flowId = execution.getDefinition().getId();
            this.conversationScope = execution.getConversationScope();
        }

        public String getFlowId() {
            return flowId;
        }

        public MutableAttributeMap getConversationScope() {
            return conversationScope;
        }

        public FlowExecution getExecution() {
            return execution;
        }
    }
}
