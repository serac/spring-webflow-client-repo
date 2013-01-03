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

/**
 * Handles transformation of an object into a byte array and vice versa.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public interface Transcoder {

    /**
     * Encodes an object into a stream of bytes.
     *
     * @param o Object to encode.
     *
     * @return Object encoded as a byte array.
     *
     * @throws IOException On encoding errors.
     */
    byte[] encode(Object o) throws IOException;


    /**
     * Decodes a stream of bytes produced by {@link #encode(Object)} back into the original object.
     *
     * @param encoded Encoded representation of an object.
     *
     * @return Object decoded from byte array.
     *
     * @throws IOException On decoding errors.
     */
    Object decode(byte[] encoded) throws IOException;
}
