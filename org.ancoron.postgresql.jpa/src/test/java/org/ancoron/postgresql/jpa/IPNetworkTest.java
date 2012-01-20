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
public class IPNetworkTest {

    private static final Logger log;

    static {
        log = Logger.getLogger(IPNetworkTest.class.getName());
    }

    @Test
    public void testInitializerPGcidr() throws Exception {
        IPNetwork net = new IPNetwork();

        Assert.assertEquals("Empty IPNetwork must not have a network address", null, net.getNetmaskAddress());
    }

    @Test
    public void testInitializerString() throws Exception {
        try {
            IPNetwork net = new IPNetwork("");

            Assert.fail("An empty String shouldn't be allowed");
        } catch (IllegalArgumentException x) {
            // expected
        }
    }

    @Test
    public void testNetworkDataIPv4() throws Exception {
        assertIPNetwork("192.168.167/24",
                24,
                "192.168.167.255",
                "255.255.255.0",
                "0.0.0.255",
                "192.168.167.1",
                "192.168.167.254");

        
        assertIPNetwork("10.0.0.0/8",
                8,
                "10.255.255.255",
                "255.0.0.0",
                "0.255.255.255",
                "10.0.0.1",
                "10.255.255.254");

        
        assertIPNetwork("123.45.64.0/19",
                19,
                "123.45.95.255",
                "255.255.224.0",
                "0.0.31.255",
                "123.45.64.1",
                "123.45.95.254");
    }

    @Test
    public void testNetworkDataIPv6() throws Exception {
        assertIPNetwork("fe80::/64",
                64,
                null,
                "ffff:ffff:ffff:ffff::",
                "::ffff:ffff:ffff:ffff",
                "fe80::",
                "fe80::ffff:ffff:ffff:ffff");

        assertIPNetwork("2001:4a2b::1f00/120",
                120,
                null,
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00",
                "::ff",
                "2001:4a2b::1f00",
                "2001:4a2b::1fff");

        assertIPNetwork("2002::123.45.67.64/122",
                122,
                null,
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffc0",
                "::3f",
                "2002::7b2d:4340",
                "2002::7b2d:437f");
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

    protected IPNetwork assertIPNetwork(String spec, int netmask, String broadcast,
            String netAddr, String hostAddr, String low, String high)
            throws UnknownHostException {
        InetAddress inet = InetAddress.getByName(spec.split("/")[0]);
        
        return assertIPNetwork(
                new IPNetwork(spec),
                netmask,
                inet.getAddress(),
                broadcast != null ? InetAddress.getByName(broadcast).getAddress() : null,
                netAddr != null ? InetAddress.getByName(netAddr).getAddress() : null,
                hostAddr != null ? InetAddress.getByName(hostAddr).getAddress() : null,
                new IPTarget(low),
                new IPTarget(high));
    }

    protected IPNetwork assertIPNetwork(IPNetwork net, int netmask,
            byte[] bytes, byte[] broadcast, byte[] netAddr, byte[] hostmask,
            IPTarget low, IPTarget high)
            throws UnknownHostException, ArrayComparisonFailure {
        if(bytes.length == 16) {
            Assert.assertArrayEquals("Got a broadcast address for an IPv6 IPNetwork", broadcast, net.getBroadcastAddress());
        } else {
            Assert.assertArrayEquals("Unexpected IPNetwork IPv4 broadcast address", broadcast, net.getBroadcastAddress());
        }

        Assert.assertEquals("Unexpected IPNetwork netmask", netmask, net.getNetmask());
        Assert.assertArrayEquals("Unexpected IPNetwork network address", netAddr, net.getNetmaskAddress());
        Assert.assertArrayEquals("Unexpected IPNetwork hostmask address", hostmask, net.getWildcard());

        Assert.assertEquals("Unexpected IPNetwork low target", low, net.getLowestTarget());
        Assert.assertEquals("Unexpected IPNetwork high target", high, net.getHighestTarget());
        
        return net;
    }
}
