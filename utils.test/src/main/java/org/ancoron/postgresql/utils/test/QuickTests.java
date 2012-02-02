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
package org.ancoron.postgresql.utils.test;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ancoron
 */
public class QuickTests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // getting a BigInteger from some byte array...
        List<byte[]> arrays = new ArrayList<byte[]>();
        
        arrays.add(new byte[] {0, 0, 0, 127});
        arrays.add(new byte[] {(byte) 255, (byte) 127, (byte) 127, (byte) 0});
        arrays.add(InetAddress.getByName("255.255.255.0").getAddress());
        arrays.add(InetAddress.getByName("fe80::20e:cff:fe33:d204").getAddress());
        arrays.add(InetAddress.getByName("fe80::10e:cff:fe33:d204").getAddress());
        arrays.add(InetAddress.getByName("fe80::e:cff:fe33:d204").getAddress());
        arrays.add(InetAddress.getByName("fe80::40e:cff:fe33:d204").getAddress());
        arrays.add(InetAddress.getByName("fe80::40e:cff:feff:ffff").getAddress());
        arrays.add(InetAddress.getByName("255.255.255.255").getAddress());
        arrays.add(InetAddress.getByName("0.0.0.0").getAddress());
        

        for(byte[] mask : arrays) {
            BigInteger bi = new BigInteger(1, mask);
            System.out.println("IP (" + toString(mask) + ") = " + bi.toString(10) + " (MAC = " + toString(getMac(mask)) + ")");
            
            System.out.println("this = " + toString(mask) + ", previous = " + toString(previous(mask)) + ", next = " + toString(next(mask)));
        }

        int netmask = 64;
        byte[] addr = InetAddress.getByName("fe80::20e:cff:fe33:d204").getAddress();
        
        byte[] low = new byte[addr.length];
        int i = netmask / 8;
        int s = 8 - (netmask % 8);

        System.arraycopy(addr, 0, low, 0, i);

        if(s > 0 && s < 8) {
            byte b = (byte) addr[i];
            for(int j=0; j<s; j++) {
                b = (byte) (b & ~(1 << j));
            }
            low[i] = b;
        }

        System.out.println("LOW(s=" + s + "): " + toString(low) + " (" + InetAddress.getByAddress(low).toString() + ")");

        byte[] hi = new byte[addr.length];
        i = netmask / 8;
        s = 8 - (netmask % 8);

        System.arraycopy(addr, 0, hi, 0, i);

        if(s > 0 && s < 8) {
            byte b = (byte) addr[i];
            for(int j=0; j<s; j++) {
                b = (byte) (b | (1 << j));
            }
            hi[i] = b;
        }
        
        for(int j = ((s == 8) ? i : i+1); j < hi.length; j++) {
            hi[j] = (byte) 255;
        }

        System.out.println("HIGH(s=" + s + "): " + toString(hi) + " (" + InetAddress.getByAddress(hi).toString() + ")");
        
        netmask = 65;
        System.out.println("Netmask: " + toString(netmask(addr, netmask)));
        System.out.println("Hostmask: " + toString(hostmask(addr, netmask)));
        
        int n = (int) Math.pow(2, 30);
        
        int x = 1;

        while((1<<x) < n) {
            System.out.println("n = " + n + " <<< x = " + x + " (" + (1<<x) + ")");
            x++;
            if(x > 30) {
                break;
            }
        }
        
        System.out.println("n = " + n + " >>> x = " + x + " ((1<<" + x + ") = " + (1<<x) + ")");
    }

    private static byte[] previous(byte[] addr) {
        byte[] previous = new byte[addr.length];
        System.arraycopy(addr, 0, previous, 0, previous.length);
        int i = previous.length - 1;

        while(previous[i] == (byte) 0) {
            previous[i] = (byte) 0xFF;
            i = i - 1;
            if(i == 0) {
                // all bytes 0x00...
                return null;
            }
        }
        
        previous[i] = (byte) (previous[i] - 0x01 & 0xFF);
        
        return previous;
    }
    
    private static byte[] next(byte[] addr) {
        byte[] next = new byte[addr.length];
        System.arraycopy(addr, 0, next, 0, next.length);
        int i = next.length - 1;

        while(next[i] == (byte) 0xFF) {
            i = i - 1;
            if(i == 0) {
                // all bytes 0xFF...
                return null;
            }
        }

        next[i] = (byte) (next[i] + 0x01 & 0xFF);
        
        return next;
    }
    
    private static byte[] netmask(byte[] addr, int netmask) {
        byte[] mask = new byte[addr.length];
        int i = netmask / 8;
        int s = 8 - (netmask % 8);

        for(int j=0; j<i; j++) {
            mask[j] = (byte) 255;
        }

        if(s > 0 && s < 8) {
            byte b = (byte) 255;
            for(int j=0; j<s; j++) {
                b = (byte) (b & ~(1 << j));
            }
            mask[i] = b;
        }
        
        return mask;
    }
    
    private static byte[] hostmask(byte[] addr, int netmask) {
        byte[] mask = new byte[addr.length];
        int i = netmask / 8;
        int s = 8 - (netmask % 8);

        for(int j=i; j<mask.length; j++) {
            mask[j] = (byte) 255;
        }

        if(s > 0 && s < 8) {
            byte b = (byte) 0;
            for(int j=0; j<s; j++) {
                b = (byte) (b | (1 << j));
            }
            mask[i] = b;
        }
        
        return mask;
    }
    
    private static byte[] getMac(byte[] fullIpv6) {
        if(fullIpv6.length != 16) {
            return null;
        }
        
        if(fullIpv6[11] != (byte) 255 || fullIpv6[12] != (byte) 254) {
            return null;
        }
        
        byte[] mac = new byte[6];
        
        // swap 7 most significant bit...
        mac[0] = (byte) (fullIpv6[8] ^ 0x02);
        mac[1] = fullIpv6[9];
        mac[2] = fullIpv6[10];
        
        // last 3 bytes are unmodified...
        mac[3] = fullIpv6[13];
        mac[4] = fullIpv6[14];
        mac[5] = fullIpv6[15];
        
        
        return mac;
    }
    
    private static String toString(byte[] bytes) {
        if(bytes == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        String bs;

        for(byte b : bytes) {
            if(!first) {
                sb.append(",");
            }

            bs = Integer.toHexString(0xFF & b);
            if(bs.length() == 1) {
                bs = "0" + bs;
            }

            sb.append(bs);
            first = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
}
