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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockExternalContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Description of ClientFlowExecutionRepositoryTest.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/webflow-config-context.xml")
public class ClientFlowExecutionRepositoryTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testLaunchAndResumeFlow() throws Exception {
        final FlowExecutor executor = context.getBean(FlowExecutor.class);
        final FlowExecutionResult launchResult = executor.launchExecution(
                "test", new LocalAttributeMap(), new MockExternalContext());
        assertNotNull(launchResult.getPausedKey());
        try {
            final ClientFlowExecutionKey key = ClientFlowExecutionKey.parse(launchResult.getPausedKey());
            assertEquals(key.toString(), launchResult.getPausedKey());
        } catch (BadlyFormattedFlowExecutionKeyException e) {
            fail("Error parsing flow execution key: " + e.getMessage());
        }
        final MockExternalContext context = new MockExternalContext();
        context.setEventId("submit");
        context.getRequestMap().put("vegan", "0");
        final FlowExecutionResult resumeResult = executor.resumeExecution(launchResult.getPausedKey(), context);
        assertNotNull(resumeResult.getOutcome());
        assertEquals("lasagnaDinner", resumeResult.getOutcome().getId());
    }
}
