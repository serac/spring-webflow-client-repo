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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Encodes an object by encrypting its serialized byte stream. The implementation assumes use of a block
 * cipher such as AES for symmetric encryption operations.
 * <p>
 * Optional gzip compression of the serialized byte stream before encryption is supported and enabled by default.
 *
 * @author Marvin S. Addison
 */
public class EncryptedTranscoder implements Transcoder {

    /** Default cipher spec is AES in CBC mode with PKCS5 padding. */
    public static final String DEFAULT_CIPHER_SPEC = "AES/CBC/PKCS5Padding";

    private IvParameterSpec iv;

    private byte[] keyBytes;

    private SecretKey key;

    /** Symmetric encryption cipher specification of form ALGORITHM/MODE/PADDING. */
    private String cipherSpec = DEFAULT_CIPHER_SPEC;

    /** Flag to indicate whether to Gzip compression before encryption. */
    private boolean compression = true;


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
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            if (this.compression) {
                out = new ObjectOutputStream(new GZIPOutputStream(outBuffer));
            } else {
                out = new ObjectOutputStream(outBuffer);
            }
            out.writeObject(o);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherSpec);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
            return cipher.doFinal(outBuffer.toByteArray());
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
        final ByteArrayInputStream inBuffer = new ByteArrayInputStream(data);
        ObjectInputStream in = null;
        try {
            if (this.compression) {
                in = new ObjectInputStream(new GZIPInputStream(inBuffer));
            } else {
                in = new ObjectInputStream(inBuffer);
            }
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization error", e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
