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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;

/**
 * Spring Webflow execution key that contains the serialized flow execution state as part of the identifier.
 * Keys produced by this class have the form KEY_BASE64 where KEY is a globally unique identifier and BASE64
 * is the base-64 encoded bytes of a serialized object output stream.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class ClientFlowExecutionKey extends FlowExecutionKey {

    public static final String KEY_FORMAT = "<uuid>_<base64-encoded-flow-state>";

    private static final long serialVersionUID = 1499481653992008590L;

    private static final int HASH_SEED = 31;

    private static final int HASH_FACTOR = 99;

    private UUID key;

    private byte[] data;


    public ClientFlowExecutionKey(final byte[] data) {
        this(UUID.randomUUID(), data);
    }

    public ClientFlowExecutionKey(final UUID key, final byte[] data) {
        Assert.notNull(key, "Flow execution key cannot be null.");
        this.key = key;
        this.data = data;
    }

    public UUID getKey() {
        return this.key;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof ClientFlowExecutionKey)) {
            return false;
        }
        final ClientFlowExecutionKey other = (ClientFlowExecutionKey) o;
        return this.key.equals(other.key) && Arrays.equals(this.data, other.data);
    }

    @Override
    public int hashCode() {
        int hash = HASH_SEED;
        hash += HASH_FACTOR * this.key.hashCode();
        for (int i = 0; i < this.data.length; i++) {
            hash += HASH_FACTOR * this.data[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        return this.key + "_" + Base64.encodeBase64String(this.data);
    }

    public static ClientFlowExecutionKey parse(final String key) throws BadlyFormattedFlowExecutionKeyException {
        final String[] tokens = key.split("_");
        if (tokens.length != 2) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(tokens[0]);
        } catch (Exception e) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        final byte[] decoded;
        try {
            decoded = Base64.decodeBase64(tokens[1]);
        } catch (Exception e) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        return new ClientFlowExecutionKey(uuid, decoded);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(toString());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        final ClientFlowExecutionKey temp = parse(in.readUTF());
        this.key = temp.key;
        this.data = temp.data;
    }
}
