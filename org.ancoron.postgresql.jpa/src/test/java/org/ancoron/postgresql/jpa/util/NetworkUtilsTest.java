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
package org.ancoron.postgresql.jpa.util;

import java.util.Arrays;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ancoron
 */
public class NetworkUtilsTest {
    
    @Test
    public void testSplitSimpleIPv4() {
        assertSplit(null, null);

        assertSplit("192.168.107.0/24", new String[] {
            "192.168.107.0/25",
            "192.168.107.128/25"
        });
    }

    @Test
    public void testSplitSimpleIPv6() {
        assertSplit("fe80::/64", new String[] {
            "fe80::/65",
            "fe80::8000:0000:0000:0000/65"
        });
    }
    
    @Test
    public void testSplitBasicIPv4() {
        assertSplit("192.168.107.0/24", 2, new String[] {
            "192.168.107.0/25",
            "192.168.107.128/25"
        });
    }

    @Test
    public void testSplitBasicIPv6() {
        assertSplit("fe80::/64", 4, new String[] {
            "fe80::/66",
            "fe80::4000:0000:0000:0000/66",
            "fe80::8000:0000:0000:0000/66",
            "fe80::C000:0000:0000:0000/66"
        });
    }

    @Test
    public void testSplitIPv4() {
        assertSplit("10.0.0.0/8", 16, new String[] {
            "10.0.0.0/12",
            "10.16.0.0/12",
            "10.32.0.0/12",
            "10.48.0.0/12",
            "10.64.0.0/12",
            "10.80.0.0/12",
            "10.96.0.0/12",
            "10.112.0.0/12",
            "10.128.0.0/12",
            "10.144.0.0/12",
            "10.160.0.0/12",
            "10.176.0.0/12",
            "10.192.0.0/12",
            "10.208.0.0/12",
            "10.224.0.0/12",
            "10.240.0.0/12"
        });
    }

    @Test
    public void testSplitIPv6() {
        assertSplit("2002::123.45.67.64/122", 8, new String[] {
            "2002::123.45.67.64/125",
            "2002::123.45.67.72/125",
            "2002::123.45.67.80/125",
            "2002::123.45.67.88/125",
            "2002::123.45.67.96/125",
            "2002::123.45.67.104/125",
            "2002::123.45.67.112/125",
            "2002::123.45.67.120/125"
        });
    }

    @Test
    public void testSplitInvalid() {
        assertSplitInvalid("192.168.107.0/24", 1, "value is lower than");
        assertSplitInvalid("192.168.107.0/24", Integer.MAX_VALUE, "value is higher than");
        assertSplitInvalid("192.168.107.0/24", 9, "value is not a valid power of 2");
        assertSplitInvalid("192.168.107.0/24", 512, "not enough free bits in netmask");
    }

    private void assertSplitResult(String[] res, IPNetwork[] splitted, int count) {
        if(res == null) {
            Assert.assertNull("Unexpectedly got sub-networks", splitted);
        } else {
            Assert.assertEquals("Invalid count of splitted networks", count, splitted.length);

            for(int i=0; i<count; i++) {
                Assert.assertEquals("Invalid network #" + (i + 1), new IPNetwork(res[i]).getValue(), splitted[i].getValue());
            }
        }
    }

    private void assertSplit(String network, String[] res) {
        IPNetwork net = network != null ? new IPNetwork(network) :null;
        
        IPNetwork[] splitted = NetworkUtils.split(net);
        int count = 2;
        assertSplitResult(res, splitted, count);
    }

    private void assertSplit(String network, int count, String[] res) {
        IPNetwork net = network != null ? new IPNetwork(network) :null;
        
        IPNetwork[] splitted = NetworkUtils.splitByCount(net, count);
        assertSplitResult(res, splitted, count);
    }

    private void assertSplitInvalid(String network, int count, String exceptionContains) {
        IPNetwork net = new IPNetwork(network);
        
        try {
            IPNetwork[] splitted = NetworkUtils.splitByCount(net, count);
            
            Assert.fail("Unexpectedly splitted the network: " + Arrays.deepToString(splitted));
        } catch(Exception x) {
            Assert.assertTrue("Unexpected exception message '" + x.getMessage()
                    + "' (does not contain '" + exceptionContains + "')",
                    x.getMessage().contains(exceptionContains));
        }
    }

    private void assertMergeResult(String res, IPNetwork merged) {
        if(res == null) {
            Assert.assertNull("Unexpectedly got a super-network", merged);
        } else {
            Assert.assertEquals("Invalid merged network", new IPNetwork(res).getValue(), merged.getValue());
        }
    }

    private void assertMerge(String[] networks, String res) {
        IPNetwork[] sub = null;
        if(networks != null) {
            sub = new IPNetwork[networks.length];
            for(int i=0; i<networks.length; i++) {
                sub[i] = new IPNetwork(networks[i]);
            }
        }

        IPNetwork merged = NetworkUtils.merge(sub);

        assertMergeResult(res, merged);
    }
    
    @Test
    public void testMergeIPv6() {
        assertMerge(null, null);

        assertMerge(new String[] {
            "fe80::/66",
            "fe80::4000:0000:0000:0000/66",
            "fe80::8000:0000:0000:0000/66",
            "fe80::C000:0000:0000:0000/66"
        }, "fe80::/64");
        
        assertMerge(new String[] {
            "2002::123.45.67.64/125",
            "2002::123.45.67.72/125",
            "2002::123.45.67.80/125",
            "2002::123.45.67.88/125",
            "2002::123.45.67.96/125",
            "2002::123.45.67.104/125",
            "2002::123.45.67.112/125",
            "2002::123.45.67.120/125"
        }, "2002::123.45.67.64/122");
    }
    
    @Test
    public void testMergeIPv4() {
        assertMerge(null, null);

        assertMerge(new String[] {
            "192.168.107.0/25",
            "192.168.107.128/25"
        }, "192.168.107.0/24");
        
        assertMerge(new String[] {
            "10.0.0.0/12",
            "10.16.0.0/12",
            "10.32.0.0/12",
            "10.48.0.0/12",
            "10.64.0.0/12",
            "10.80.0.0/12",
            "10.96.0.0/12",
            "10.112.0.0/12",
            "10.128.0.0/12",
            "10.144.0.0/12",
            "10.160.0.0/12",
            "10.176.0.0/12",
            "10.192.0.0/12",
            "10.208.0.0/12",
            "10.224.0.0/12",
            "10.240.0.0/12"
        }, "10.0.0.0/8");
    }
}
