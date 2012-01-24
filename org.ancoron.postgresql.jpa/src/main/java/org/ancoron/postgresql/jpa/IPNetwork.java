/*
 * Copyright 2011-2012 ancoron.
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
import java.sql.SQLException;
import java.util.Arrays;
import org.postgresql.net.PGcidr;

/**
 *
 * @author ancoron
 */
public class IPNetwork extends PGcidr implements Serializable, Cloneable, Comparable<IPNetwork> {

    private byte[] broadcastAddress;
    private byte[] wildcard;
    private byte[] netmaskAddress;
    
    private boolean v6 = false;
    
    public IPNetwork() {
        super();
    }
    
    public IPNetwork(PGcidr net) {
        this();
        
        if(net != null) {
            try {
                setValue(net.getValue());
            } catch (SQLException ex) {
                throw new IllegalArgumentException(
                        "Unable to create Network instance from given PGinet "
                        + net.toString(), ex);
            }
        }
    }
    
    public IPNetwork(String net) {
        this();

        if(net != null) {
            try {
                setValue(net);
            } catch (SQLException ex) {
                throw new IllegalArgumentException(
                        "Unable to construct network for given String '" + net
                        + "': " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void setValue(String v) throws SQLException {
        super.setValue(v);
        
        init();
    }
    
    public final void init() {
        if(addr != null) {
            v6 = addr.length == 16;
            
            if(!v6 || hasEmbeddedIPv4()) {
                broadcastAddress = high();
            } else {
                broadcastAddress = null;
            }
            
            netmaskAddress = netmask();
            wildcard = wildcard();
        } else {
            // reset everything...
            broadcastAddress = null;
            wildcard = null;
            netmaskAddress = null;
        }
    }

    /**
     * The broadcast address for an IPv4 subnet.
     * 
     * <p>
     * This returns <code>null</code> in case of an IPv6 address as there are
     * no broadcast addresses anymore by definition.
     * </p>
     * 
     * @return The broadcast address or <code>null</code>
     */
    public byte[] getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(byte[] broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }
    
    private byte[] netmask() {
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

    private byte[] wildcard() {
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

    public byte[] getWildcard() {
        return wildcard;
    }

    public void setWildcard(byte[] wildcard) {
        this.wildcard = wildcard;
    }

    public byte[] getNetmaskAddress() {
        return netmaskAddress;
    }

    public void setNetmaskAddress(byte[] netmaskAddress) {
        this.netmaskAddress = netmaskAddress;
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
        final IPNetwork other = (IPNetwork) obj;

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
    public int compareTo(IPNetwork o) {
        if(o == null) {
            return 1;
        }
        
        if(this.equals(o)) {
            return 0;
        }
        
        BigInteger alow = new BigInteger(1, low());
        BigInteger ahigh = new BigInteger(1, high());
        BigInteger a = alow.add(ahigh);

        BigInteger blow = new BigInteger(1, o.low());
        BigInteger bhigh = new BigInteger(1, o.high());
        BigInteger b = blow.add(bhigh);

        return a.compareTo(b);
    }
    
    protected byte[] high() {
        byte[] hi = new byte[addr.length];
        int i = netmask / 8;
        int s = 8 - (netmask % 8);
        System.arraycopy(addr, 0, hi, 0, i);
        // set least significant bits...
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
        return hi;
    }

    protected byte[] low() {
        byte[] low = new byte[addr.length];
        int i = netmask / 8;
        int s = 8 - (netmask % 8);
        System.arraycopy(addr, 0, low, 0, i);

        // unset least significant bits...
        if(s > 0 && s < 8) {
            byte b = (byte) addr[i];
            for(int j=0; j<s; j++) {
                b = (byte) (b & ~(1 << j));
            }
            low[i] = b;
        }
        return low;
    }

    /**
     * Check if the IPv6 network is made IPv4 compatible.
     * 
     * <p>
     * The following formats are detected:
     * <ul>
     * <li><tt>::a.b.c.d/96</tt></li>
     * <li><tt>::ffff:a.b.c.d/96</tt></li>
     * </ul>
     * </p>
     * 
     * @return <tt>true</tt> if this network is IPv4 compatible, <tt>false</tt>
     * otherwise
     */
    public boolean hasEmbeddedIPv4() {
        if(v6) {
            if(embedded_ipv4) {
                return true;
            }
            
            boolean emb = true;
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

            emb &= netmask > 96;
            
            // avoid '::1' ...
            emb &= addr[12] != (byte) 0x00;
            
            return emb;
        }
        
        return false;
    }
    
    public IPTarget getHighestTarget() {
        byte[] hi = high();
        
        if((!v6 || hasEmbeddedIPv4()) && netmask < (addr.length * 8)) {
            // decrease by one for IPv4...
            int i = hi.length - 1;
            hi[i] = (byte) (hi[i] - (byte) 1);
        }
        
        return new IPTarget(hi);
    }
    
    public IPTarget getLowestTarget() {
        byte[] low = low();
        
        if((!v6 || hasEmbeddedIPv4()) && netmask < (addr.length * 8)) {
            // increase by one for IPv4...
            int i = low.length - 1;
            low[i] = (byte) (low[i] + (byte) 1);
        }
        
        return new IPTarget(low);
    }

    public boolean contains(IPTarget ip) {
        return getLowestTarget().compareTo(ip) <= 0
                && getHighestTarget().compareTo(ip) >= 0;
    }
}
