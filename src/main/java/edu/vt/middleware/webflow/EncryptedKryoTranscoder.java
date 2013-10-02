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
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.NoSuchPaddingException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;

/**
 * Provides symmetric block cipher encryption of objects using <a href="https://code.google.com/p/kryo/">Kryo</a>
 * serialization and Gzip compression.
 *
 * @author Marvin S. Addison
 */
public class EncryptedKryoTranscoder extends AbstractEncryptedTranscoder {

    private final Kryo kryo = new Kryo();

    @Override
    public void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
        super.init();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(
                ClientFlowExecutionRepository.SerializedFlowExecutionState.class,
                new FieldSerializer(kryo, ClientFlowExecutionRepository.SerializedFlowExecutionState.class));
        kryo.register(FlowExecutionImpl.class, new JavaSerializer());
    }


    @Override
    protected byte[] serialize(final Object o) throws IOException {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        Output out = null;
        try {
            if (this.compression) {
                out = new Output(new GZIPOutputStream(outBuffer));
            } else {
                out = new Output(outBuffer);
            }
            kryo.writeClassAndObject(out, o);
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
        Input in = null;
        try {
            if (this.compression) {
                in = new Input(new GZIPInputStream(inBuffer));
            } else {
                in = new Input(inBuffer);
            }
            return kryo.readClassAndObject(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
