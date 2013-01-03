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

import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * Description of ClientFlowExecutionRepositoryException.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class ClientFlowExecutionRepositoryException extends FlowExecutionRepositoryException {

    private static final long serialVersionUID = 2164175424974041060L;

    public ClientFlowExecutionRepositoryException(final String message) {
        super(message);
    }

    public ClientFlowExecutionRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
