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

/**
 * Provides symmetric block cipher encryption of objects using Java serialization and Gzip compression.
 *
 * @author Marvin S. Addison
 */
public class EncryptedTranscoder extends AbstractEncryptedTranscoder {

    @Override
    protected byte[] serialize(Object o) throws IOException {
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
        return outBuffer.toByteArray();
    }

    @Override
    protected Object deserialize(byte[] bytes) throws IOException {
        final ByteArrayInputStream inBuffer = new ByteArrayInputStream(bytes);
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
