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

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static junit.framework.TestCase.assertEquals;

/**
 * Description of EncryptedTranscoderTest.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class EncryptedTranscoderTest {

    private EncryptedTranscoder transcoder;

    private Serializable encodable;

    public EncryptedTranscoderTest(
            final EncryptedTranscoder transcoder, final Serializable encodable) {
        this.transcoder = transcoder;
        this.encodable = encodable;
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final String iv64 = "0khXskIDcZ8=";
        final String iv128 = "Tt6uHLdUp2VlRu8zunYQUA==";
        final String key128 = "77LSKX7cjabjvDgjXyGWwA==";
        final String key256 = "4Io3Tji/578bgca6b140XQkA1A3ZKOQSjPYVOdDdY0Y=";

        // Test case #1
        // 128-bit AES with compression
        final EncryptedTranscoder transcoder1 = new EncryptedTranscoder();
        transcoder1.setIV(iv128);
        transcoder1.setCipherSpec("AES/CBC/PKCS5Padding");
        transcoder1.setKey(key128);
        transcoder1.setCompression(true);

        // Test case #2
        // 256-bit AES with compression
        // NOTE: This test requires use of JVM with unlimited strength JCE policy files due to 256-bit key size
        final EncryptedTranscoder transcoder2 = new EncryptedTranscoder();
        transcoder2.setIV(iv128);
        transcoder2.setCipherSpec("AES/CBC/PKCS5Padding");
        transcoder2.setKey(key256);
        transcoder2.setCompression(true);

        // Test case #3
        // 128-bit Blowfish in OFB mode without compression
        final EncryptedTranscoder transcoder3 = new EncryptedTranscoder();
        transcoder3.setIV(iv64);
        transcoder3.setCipherSpec("Blowfish/OFB/PKCS5Padding");
        transcoder3.setKey(key128);
        transcoder3.setCompression(false);

        return Arrays.asList(new Object[][] {
                { transcoder1, "Able was I ere I saw elba." },
                {
                        transcoder2,
                        "Four score and seven years ago our forefathers brought forth upon this continent a " +
                                "new nation conceived in liberty and dedicated to the proposition that all men " +
                                "are created equal.",
                },
                {
                        transcoder3,
                        new URL("https://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&" +
                                "q=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&aq=&" +
                                "sll=38.897678,-77.036517&sspn=0.00835,0.007939&vpsrc=6&t=w&" +
                                "g=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&ie=UTF8&hq=&" +
                                "hnear=1600+Pennsylvania+Ave+NW,+Washington,+District+of+Columbia,+20500&" +
                                "ll=38.898521,-77.036517&spn=0.00835,0.007939&z=17&iwloc=A") },
        });
    }

    @Test
    public void testEncodeDecode() throws Exception {
        this.transcoder.init();
        final byte[] encoded = this.transcoder.encode(this.encodable);
        assertEquals(this.encodable, this.transcoder.decode(encoded));
    }
}
