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

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Arrays;
import org.postgresql.net.PGinet;

/**
 *
 * @author ancoron
 */
public class IPTarget extends PGinet implements Serializable, Cloneable, Comparable<IPTarget> {

    private InetAddress host;
    
    private boolean v6 = false;
    
    public IPTarget() {
        super();
    }
    
    public IPTarget(PGinet ip) {
        this();
        
        if(ip != null) {
            try {
                setValue(ip.getValue());
            } catch (SQLException ex) {
                throw new IllegalArgumentException(
                        "Unable to create IPTarget instance from given PGinet "
                        + ip.toString(), ex);
            }
            
            init();
        }
    }
    
    public IPTarget(byte[] address) {
        this();
        
        if(address != null) {
            try {
                host = InetAddress.getByAddress(address);

                // use verifying methods to initialize...
                setValue(host.toString().split("/")[1]);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Unable to create IPTarget instance from given byte array",
                        ex);
            }
            
            init();
        }
    }
    
    public IPTarget(String ip) {
        this();

        if(ip != null) {
            try {
                setValue(ip);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to construct IPTarget from value '" + ip + "'", ex);
            }

            init();
        }
    }
    
    public final void init() {
        if(addr != null) {
            v6 = addr.length == 16;
            try {
                host = InetAddress.getByAddress(addr);
            } catch (UnknownHostException ex) {
                throw new IllegalArgumentException(
                        "Unable to initialize IPTarget instance from given byte array",
                        ex);
            }
        } else {
            // reset everything...
            host = null;
        }
    }

    public InetAddress getHost() {
        return host;
    }

    public byte[] getAddr() {
        return addr;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public boolean isV6() {
        return v6;
    }

    public void setV6(boolean v6) {
        this.v6 = v6;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IPTarget other = (IPTarget) obj;

        if (this.netmask != other.netmask) {
            return false;
        }

        if (!Arrays.equals(this.addr, other.addr)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Arrays.hashCode(this.addr);
        hash = 17 * hash + this.netmask;
        return hash;
    }

    @Override
    public int compareTo(IPTarget o) {
        if(o == null) {
            return 1;
        }
        
        if(this.equals(o)) {
            return 0;
        }
        
        BigInteger a = new BigInteger(1, this.addr);
        BigInteger b = new BigInteger(1, o.addr);

        return a.compareTo(b);
    }

    /**
     * Get the MAC address of an IPv6 address.
     * 
     * <p>
     * This method can be used to easily retrieve the MAC address that has been
     * used to construct the interface identifier part of a uni-/any-cast IPv6
     * address.
     * </p>
     * 
     * <p>
     * This method simply returns <tt>null</tt> in any other case.
     * </p>
     * 
     * <p>
     * Note: not all IPv6 uni-/any-cast addresses encode the MAC address inside
     * themselves. In addition even if an IPv6 address seems to encode the MAC
     * address by evaluating hinting values it may just be an accident. So this
     * method does not guarantee that
     * <ol type="a">
     * <li>a MAC address is returned at all.</li>
     * <li>the returned MAC address is valid.</li>
     * <li>the returned MAC address really specifies an existing interface.</li>
     * </ol>
     * </p>
     * 
     * @return The MAC address of the IPv6 address or <tt>null</tt>
     */
    public byte[] getMAC() {
        if(addr.length != 16) {
            return null;
        }
        
        // check for multicast...
        if(addr[0] == (byte) 255) {
            return null;
        }
        
        // check for inserted "FFFE" value...
        if(addr[11] != (byte) 255 || addr[12] != (byte) 254) {
            return null;
        }
        
        byte[] mac = new byte[6];
        
        // swap the 7th most significant bit...
        mac[0] = (byte) (addr[8] ^ 0x02);
        mac[1] = addr[9];
        mac[2] = addr[10];
        
        // last 3 bytes are unmodified...
        mac[3] = addr[13];
        mac[4] = addr[14];
        mac[5] = addr[15];
        
        return mac;
    }
    
    /**
     * Gets the unicast prefix-based multicast address of an IPv6 address.
     * 
     * <p>
     * This multicast address is specified by RFC-3306 and also known as the 
     * "Solicited-node multicast address".
     * </p>
     * 
     * <p>
     * Note: this only works if the underlying IPv6 specifies a single target.
     * </p>
     * 
     * 
     * @return The multicast address or <tt>null</tt> if not an IPv6 address
     * 
     * @throws UnknownHostException This should never happen
     */
    public InetAddress getUnicastPrefixMCAddress() throws UnknownHostException {
        byte[] snmc = new byte[16];
        
        if(addr.length != 16) {
            return null;
        }
        
        // check for multicast...
        if(addr[0] == (byte) 255) {
            return null;
        }
        
        // check for loopback and any...
        if(addr[0] == (byte) 0
                && new BigInteger(1, this.addr).compareTo(BigInteger.ONE) <= 0)
        {
            return null;
        }

        // make a multicast address...
        snmc[0] = (byte) 255;
        
        // no flags but link-local scope...
        snmc[1] = 2 & 0xFF;
        
        // clear everything only leaving a 24-bit unicast part...
        snmc[11] = (byte) 1;
        snmc[12] = (byte) 255;
        snmc[13] = addr[13];
        snmc[14] = addr[14];
        snmc[15] = addr[15];
        
        return InetAddress.getByAddress(snmc);
    }
}
