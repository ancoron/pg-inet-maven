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

    public static final InetAddress MAX_V4_ADDRESS;
    public static final BigInteger MAX_V4_VALUE;
    public static final InetAddress MAX_V6_ADDRESS;
    public static final BigInteger MAX_V6_VALUE;
    
    static {
        try {
            MAX_V4_ADDRESS = InetAddress.getByName("255.255.255.255");
            MAX_V6_ADDRESS = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        } catch (UnknownHostException ex) {
            throw new IllegalStateException();
        }
        
        MAX_V4_VALUE = new BigInteger(1, MAX_V4_ADDRESS.getAddress());
        MAX_V6_VALUE = new BigInteger(1, MAX_V6_ADDRESS.getAddress());
    }
    
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

    @Override
    public void setValue(String v) throws SQLException {
        super.setValue(v);
        
        init();
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

    /**
     * Get the IPTarget with the IP address decreased by one.
     * 
     * <p>
     * This method returns <code>null</code> in case the current IP address is
     * completely zero (e.g. <code>0.0.0.0</code> or <code>::</code>).
     * </p>
     * 
     * @return The next lower IPTarget or <code>null</code>
     */
    public IPTarget getPrevious() {
        byte[] prev = previous();
        
        return prev != null ? new IPTarget(prev) : null;
    }

    /**
     * Get the IPTarget with the IP address increased by one.
     * 
     * <p>
     * This method returns <code>null</code> in case the current IP address is
     * completely full (e.g. <code>255.255.255.255</code> or
     * <code>ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff</code>).
     * </p>
     * 
     * @return The next higher IPTarget or <code>null</code>
     */
    public IPTarget getNext() {
        byte[] next = next();
        
        return next != null ? new IPTarget(next) : null;
    }

    private byte[] previous() {
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
        
        previous[i] = (byte) (previous[i] - (byte) 1);
        
        return previous;
    }
    
    private byte[] next() {
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

        next[i] = (byte) (next[i] + (byte) 1);
        
        return next;
    }
    
    /**
     * Get the embedded IPv4 address of an IPv6 address.
     * 
     * @return the IPv4 address or <tt>null</tt>
     */
    public IPTarget getEmbeddedIPv4() {
        if(v6) {
            boolean emb = true;
            if(!embedded_ipv4) {
                // check IPv4-compatible...
                for(int i=0; i<10; i++) {
                    emb &= (addr[i] == (byte) 0x00);
                }

                if(addr[10] == (byte) 0xFF && addr[11] == (byte) 0xFF) {
                    // old format...
                    emb &= true;
                } else if(addr[10] == (byte) 0x00 && addr[11] == (byte) 0x00) {
                    emb &= true;
                } else {
                    emb = false;
                }

                // avoid '::1' ...
                emb &= addr[12] != (byte) 0x00;
            }
            
            if(emb) {
                byte[] a = new byte[4];
                System.arraycopy(addr, 12, a, 0, 4);
                return new IPTarget(a);
            }
        }
        
        return null;
    }
    
    /**
     * This method implements the <tt>subtract</tt> operator to get the
     * difference between two IPTarget instances.
     * 
     * @param ip An IPTarget of the same type (IPv4/IPv6)
     * 
     * @return A positive or negative {@link BigInteger} value if the given
     * IPTarget is lower or higher than this instance or <tt>null</tt> if the
     * given argument is <tt>null</tt>
     * 
     * @throws IllegalArgumentException if the given IPTarget is of different
     * size (IPv4 vs. IPv6)
     */
    public BigInteger subtract(IPTarget ip) {
        if(ip == null) {
            return null;
        }

        if(this.v6 != ip.v6) {
            throw new IllegalArgumentException("Cannot subtract IPTarget value "
                    + ip + " from " + this + " - different sizes");
        }
        
        BigInteger a = new BigInteger(1, this.addr);
        BigInteger b = new BigInteger(1, ip.addr);
        
        return a.subtract(b);
    }

    /**
     * This method implements the <tt>subtract</tt> operator to get a new
     * IPTarget based on the current one and an offset.
     * 
     * @param offset The offset (within reasonable limits)
     * 
     * @return A new IPTarget instance or <tt>null</tt> if the given argument
     * was null
     * 
     * @throws IllegalArgumentException if the given offset is not within a
     * reasonable range
     */
    public IPTarget subtract(BigInteger offset) {
        if(offset == null) {
            return null;
        }
        
        if(offset.compareTo(BigInteger.ZERO) == 0) {
            return new IPTarget(this);
        }
        
        BigInteger a = new BigInteger(1, this.addr);
        
        byte[] ip = new byte[addr.length];
        BigInteger bi = a.subtract(offset);

        if(bi.compareTo(BigInteger.ZERO) < 0
                || !v6 && bi.compareTo(MAX_V4_VALUE) > 0
                || v6 && bi.compareTo(MAX_V6_VALUE) > 0) {
            // exceeding highest IP address...
            throw new IllegalArgumentException("Cannot subtract value "
                    + offset + " from " + this + " - result is out of range");
        }
        
        byte[] b = bi.toByteArray();

        // handle sign bit...
        System.arraycopy(b, (b.length == ip.length + 1) ? 1 : 0, ip, 0, ip.length);
        
        return new IPTarget(ip);
    }

    /**
     * This method implements the <tt>subtract</tt> operator to get a new
     * IPTarget based on the current one and an offset.
     * 
     * <p>
     * This is just a convenience method and is exactly the same as calling 
     * <tt>add(BigInteger.valueOf(offset))</tt>.
     * </p>
     * 
     * @param offset The offset (within reasonable limits)
     * 
     * @return A new IPTarget instance or <tt>null</tt> if the given argument
     * was null
     * 
     * @throws IllegalArgumentException if the given offset is not within a
     * reasonable range
     * 
     * @see #subtract(java.math.BigInteger) 
     */
    public IPTarget subtract(long offset) {
        return subtract(BigInteger.valueOf(offset));
    }

    /**
     * This method implements the <tt>add</tt> operator to get a new
     * IPTarget based on the current one and an offset.
     * 
     * @param offset The offset (within reasonable limits)
     * 
     * @return A new IPTarget instance or <tt>null</tt> if the given argument
     * was null
     * 
     * @throws IllegalArgumentException if the given offset is not within a
     * reasonable range
     */
    public IPTarget add(BigInteger offset) {
        if(offset == null) {
            return null;
        }
        
        if(offset.compareTo(BigInteger.ZERO) == 0) {
            return new IPTarget(this);
        }
        
        BigInteger a = new BigInteger(1, this.addr);
        
        byte[] ip = new byte[addr.length];
        BigInteger bi = a.add(offset);

        if(bi.compareTo(BigInteger.ZERO) < 0
                || !v6 && bi.compareTo(MAX_V4_VALUE) > 0
                || v6 && bi.compareTo(MAX_V6_VALUE) > 0) {
            // exceeding highest IP address...
            throw new IllegalArgumentException("Cannot add value "
                    + offset + " from " + this + " - result is out of range");
        }
        
        byte[] b = bi.toByteArray();

        // handle sign bit...
        System.arraycopy(b, (b.length == ip.length + 1) ? 1 : 0, ip, 0, ip.length);
        
        return new IPTarget(ip);
    }

    /**
     * This method implements the <tt>add</tt> operator to get a new
     * IPTarget based on the current one and an offset.
     * 
     * <p>
     * This is just a convenience method and is exactly the same as calling 
     * <tt>add(BigInteger.valueOf(offset))</tt>.
     * </p>
     * 
     * @param offset The offset (within reasonable limits)
     * 
     * @return A new IPTarget instance or <tt>null</tt> if the given argument
     * was null
     * 
     * @throws IllegalArgumentException if the given offset is not within a
     * reasonable range
     * 
     * @see #add(java.math.BigInteger) 
     */
    public IPTarget add(long offset) {
        // we use 'valueOf' to benefit from internal caching...
        return add(BigInteger.valueOf(offset));
    }
    
    /**
     * Check if this represents an IP multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMulticastAddress()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a multicast address,
     * <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMulticastAddress() 
     */
    public boolean isMulticast() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMulticastAddress();
    }
    
    /**
     * Check if this represents a global multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMCGlobal()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a global multicast 
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMCGlobal() 
     */
    public boolean isMulticastGlobal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMCGlobal();
    }
    
    /**
     * Check if this represents a link-local multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMCLinkLocal()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a link-local multicast 
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMCLinkLocal() 
     */
    public boolean isMulticastLinkLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMCLinkLocal();
    }
    
    /**
     * Check if this represents a node-local multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMCNodeLocal()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a node-local multicast 
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMCNodeLocal() 
     */
    public boolean isMulticastNodeLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMCNodeLocal();
    }
    
    /**
     * Check if this represents a organization-local multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMCOrgLocal()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a org-local multicast 
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMCOrgLocal() 
     */
    public boolean isMulticastOrgLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMCOrgLocal();
    }
    
    /**
     * Check if this represents a site-local multicast address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isMCSiteLocal()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a site-local multicast 
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isMCSiteLocal() 
     */
    public boolean isMulticastSiteLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isMCSiteLocal();
    }
    
    /**
     * Check if this represents a link-local address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isLinkLocalAddress()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents a link-local
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isLinkLocalAddress() 
     */
    public boolean isLinkLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isLinkLocalAddress();
    }
    
    /**
     * Check if this represents the any-local address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isAnyLocalAddress()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents the any-local
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isAnyLocalAddress() 
     */
    public boolean isAnyLocal() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isAnyLocalAddress();
    }
    
    /**
     * Check if this represents the loopback address.
     * 
     * <p>
     * Invoking this method is exactly the same as using 
     * <tt>getHost().isLoopbackAddress()</tt>.
     * </p>
     *
     * @return <tt>true</tt> if this IPTarget represents the loopback
     * address, <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException if this instance is not yet initialized 
     * with a value or has been reset by calling {@link #init() } on an already
     * initialized instance
     * 
     * @see #getHost() 
     * @see InetAddress#isLoopbackAddress() 
     */
    public boolean isLoopback() {
        if(host == null) {
            throw new IllegalStateException("IPTarget is not yet initialized or has been reset");
        }
        return host.isLoopbackAddress();
    }

    @Override
    public String toString() {
        if(addr == null) {
            return null;
        }
        return super.toString();
    }
}
