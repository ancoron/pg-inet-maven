/*
 * Copyright 2012 ancoron.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ancoron.postgresql.jpa;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import org.junit.Test;
import org.junit.Assert;
import org.junit.internal.ArrayComparisonFailure;

/**
 *
 * @author ancoron
 */
public class IPTargetTest {

    private static final Logger log;

    static {
        log = Logger.getLogger(IPTargetTest.class.getName());
    }

    @Test
    public void testInitializerPGinet() throws Exception {
        IPTarget ip = new IPTarget();

        Assert.assertEquals("Empty IPTarget must not have a host", null, ip.getHost());
    }

    @Test
    public void testInitializerString() throws Exception {
        try {
            IPTarget ip = new IPTarget("");

            Assert.fail("An empty String shouldn't be allowed");
        } catch (IllegalArgumentException x) {
            // expected
        }

        assertIPTarget("192.168.167.167", 32, null, null);
        assertIPTarget("10.0.0.1", 32, null, null);
        assertIPTarget("fe80::20e:cff:fe33:d204", 128, bytes("00:0e:0c:33:d2:04"), InetAddress.getByName("ff02::1:ff33:d204"));
        assertIPTarget("::", 128, null, null);
        assertIPTarget("::1", 128, null, null);
    }

    protected byte[] bytes(String spec) {
        spec = spec.replaceAll("[:\\.\\-]", "");
        int len = spec.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(spec.charAt(i), 16) << 4)
                    + Character.digit(spec.charAt(i + 1), 16));
        }

        return data;
    }

    protected void assertIPTarget(String spec, int netmask, byte[] mac, InetAddress unimc)
            throws UnknownHostException {
        InetAddress inet = InetAddress.getByName(spec);
        assertIPTarget(new IPTarget(spec), inet, netmask, inet.getAddress(), spec, mac, unimc);
    }

    protected void assertIPTarget(IPTarget ip, InetAddress inet, int netmask,
            byte[] bytes, String str, byte[] mac, InetAddress unimc)
            throws UnknownHostException, ArrayComparisonFailure {
        Assert.assertEquals("Unexpected IPTarget host", inet, ip.getHost());
        Assert.assertEquals("Unexpected IPTarget netmask", netmask, ip.getNetmask());
        Assert.assertArrayEquals("Unexpected IPTarget addr", bytes, ip.getAddr());

        if(bytes.length == 16) {
            Assert.assertArrayEquals("Unexpected IPTarget MAC address", mac, ip.getMAC());
            Assert.assertEquals("Unexpected IPTarget unicast prefix-based multicast address", unimc, ip.getUnicastPrefixMCAddress());
        } else {
            Assert.assertArrayEquals("Got a MAC address from an IPv4 address", null, ip.getMAC());
            Assert.assertEquals("Got a unicast prefix-based multicast address from an IPv4 address", null, ip.getUnicastPrefixMCAddress());
        }
    }
}
