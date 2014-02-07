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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.cryptacular.bean.CipherBean;

/**
 * Encodes an object by encrypting its serialized byte stream. Details of encryption are handled by an instance of
 * {@link CipherBean}.
 * <p>
 * Optional gzip compression of the serialized byte stream before encryption is supported and enabled by default.
 *
 * @author Marvin S. Addison
 */
public class EncryptedTranscoder implements Transcoder {

    /** Handles encryption/decryption details. */
    private CipherBean cipherBean;

    /** Flag to indicate whether to Gzip compression before encryption. */
    private boolean compression = true;


    public void setCompression(final boolean compression) {
        this.compression = compression;
    }

    public void setCipherBean(final CipherBean cipherBean) {
        this.cipherBean = cipherBean;
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
            return cipherBean.encrypt(outBuffer.toByteArray());
        } catch (Exception e) {
            throw new IOException("Encryption error", e);
        }
    }

    public Object decode(final byte[] encoded) throws IOException {
        final byte[] data;
        try {
            data = cipherBean.decrypt(encoded);
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
