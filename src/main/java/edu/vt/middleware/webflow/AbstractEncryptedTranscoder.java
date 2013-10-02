/*
 * Virginia Tech licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.  You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.vt.middleware.webflow;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Abstract base class for all encoders that encrypt a serialized byte representation of an object.
 * The implementation assumes use of a block cipher such as AES for symmetric encryption operations.
 * <p>
 * Optional compression of the serialized byte stream before encryption is supported and enabled by default.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractEncryptedTranscoder implements Transcoder {
    /** Default cipher spec is AES in CBC mode with PKCS5 padding. */
    public static final String DEFAULT_CIPHER_SPEC = "AES/CBC/PKCS5Padding";

    /** Cipher initialization vector (block ciphers only). */
    protected IvParameterSpec iv;

    /** Cipher symmetric encryption key. */
    protected SecretKey key;

    /** Symmetric encryption cipher specification of form ALGORITHM/MODE/PADDING. */
    protected String cipherSpec = DEFAULT_CIPHER_SPEC;

    /** Flag to indicate whether to Gzip compression before encryption. */
    protected boolean compression = true;

    /** Bytes of symmetric encryption key. */
    private byte[] keyBytes;

    public void setCompression(final boolean compression) {
        this.compression = compression;
    }

    public void setIV(final String base64EncodedIV) {
        this.iv = new IvParameterSpec(Base64.decodeBase64(base64EncodedIV));
    }

    public void setKey(final String base64EncodedKey) {
        this.keyBytes = Base64.decodeBase64(base64EncodedKey);
    }

    public void setCipherSpec(final String specification) {
        this.cipherSpec = specification;
    }

    /**
     * Initializes the cipher based on the specification provided in {@link #setCipherSpec(String)}.
     * This method MUST be called prior to {@link #encode(Object)} or {@link #decode(byte[])}.
     *
     * @throws NoSuchAlgorithmException If the algorithm specified in {@link #setCipherSpec(String)} is not available.
     * @throws NoSuchPaddingException If the padding specified in {@link #setCipherSpec(String)} is not available.
     */
    public void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher.getInstance(this.cipherSpec);
        final String algorithm = this.cipherSpec.substring(0, this.cipherSpec.indexOf('/'));
        this.key = new SecretKeySpec(this.keyBytes, algorithm);
    }

    public byte[] encode(final Object o) throws IOException {
        if (o == null) {
            return new byte[0];
        }
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherSpec);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
            return cipher.doFinal(serialize(o));
        } catch (Exception e) {
            throw new IOException("Encryption error", e);
        }
    }

    public Object decode(byte[] encoded) throws IOException {
        final byte[] data;
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherSpec);
            cipher.init(Cipher.DECRYPT_MODE, this.key, this.iv);
            data = cipher.doFinal(encoded);
        } catch (Exception e) {
            throw new IOException("Decryption error", e);
        }
        if (data.length == 0) {
            return null;
        }
        return deserialize(data);
    }


    /**
     * Produces a serialized byte representation of the given object.
     *
     * @param o Object to serialize.
     *
     * @return Serialized bytes.
     *
     * @throws IOException On serialization errors.
     */
    protected abstract byte[] serialize(Object o) throws IOException;


    /**
     * Produces an object from its serialized byte representation. This is the complement to {@link #serialize(Object)}.
     *
     * @param bytes Serialized bytes.
     *
     * @return Object rehydrated from serialized form.
     *
     * @throws IOException On serialization errors.
     */
    protected abstract Object deserialize(byte[] bytes) throws IOException;
}
