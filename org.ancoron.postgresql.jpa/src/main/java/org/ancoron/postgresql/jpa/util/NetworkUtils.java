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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.postgresql.net.PGcidr;

/**
 * Utility class for dealing with IP based networks.
 *
 * @author ancoron
 */
public class NetworkUtils {
    
    private static final int split_min = 2;
    private static final int split_max = 1<<30;

    /**
     * Split a given {@link IPNetwork} into the given number of sub-networks.
     * 
     * <p>
     * This method creates the given number of networks which all are
     * sub-network definitions of the given one, filling the space completely.
     * </p>
     * 
     * <p>
     * Example:
     * <pre>
     * NetworkUtils.splitByCount(new IPNetwork("10.0.0.0/8"), 16);
     * </pre>
     * ...will produce exactly 16 sub-networks matching the following
     * definitions:
     * <pre>
     * "10.0.0.0/12",
     * "10.16.0.0/12",
     * "10.32.0.0/12",
     * "10.48.0.0/12",
     * "10.64.0.0/12",
     * "10.80.0.0/12",
     * "10.96.0.0/12",
     * "10.112.0.0/12",
     * "10.128.0.0/12",
     * "10.144.0.0/12",
     * "10.160.0.0/12",
     * "10.176.0.0/12",
     * "10.192.0.0/12",
     * "10.208.0.0/12",
     * "10.224.0.0/12",
     * "10.240.0.0/12"
     * </pre>
     * </p>
     * 
     * <p>
     * In case <code>null</code> is given for the input {@link IPNetwork} the
     * result of this method will also be <code>null</code>.
     * </p>
     * 
     * @param network The {@link IPNetwork} to split (may be <code>null</code>)
     * @param n The number of networks to produce which must be a power of 2 and
     * within the range of {@value #split_min} up to {@value #split_max}
     * 
     * @return the specified number of sub-networks or <code>null</code>
     * 
     * @throws IllegalArgumentException if the given number of networks to
     * produce is invalid
     * 
     * @throws IllegalStateException if an unexpected exception occurs while
     * calculating the new networks
     */
    public static IPNetwork[] splitByCount(final IPNetwork network, int n) {
        if(network == null) {
            return null;
        }
        
        if(n < split_min) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is lower than "
                    + split_min);
        } else if(n > split_max) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is higher than "
                    + split_max);
        } else if((n & 1) != 0) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is not a valid power of 2");
        }
        
        int x = 1;

        while((1<<x) < n) {
            x++;
        }
        
        if(Math.pow(2D, (double) x) != (double) n) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is not a valid power of 2");
        }
        
        return splitByPower(network, x);
    }

    /**
     * Split a given {@link IPNetwork} into the given number of sub-networks.
     * 
     * <p>
     * This method creates the given number of networks which all are
     * sub-network definitions of the given one, filling the space completely.
     * </p>
     * 
     * <p>
     * Example:
     * <pre>
     * NetworkUtils.splitByPower(new IPNetwork("10.0.0.0/8"), 4);
     * </pre>
     * ...will produce exactly 2<sup>4</sup> (=16) sub-networks matching the following
     * definitions:
     * <pre>
     * "10.0.0.0/12",
     * "10.16.0.0/12",
     * "10.32.0.0/12",
     * "10.48.0.0/12",
     * "10.64.0.0/12",
     * "10.80.0.0/12",
     * "10.96.0.0/12",
     * "10.112.0.0/12",
     * "10.128.0.0/12",
     * "10.144.0.0/12",
     * "10.160.0.0/12",
     * "10.176.0.0/12",
     * "10.192.0.0/12",
     * "10.208.0.0/12",
     * "10.224.0.0/12",
     * "10.240.0.0/12"
     * </pre>
     * </p>
     * 
     * <p>
     * In case <code>null</code> is given for the input {@link IPNetwork} the
     * result of this method will also be <code>null</code>.
     * </p>
     * 
     * <p>
     * <b>IPv6 networks:</b> in case the given IPv6 network has been specified
     * using an embedded IPv4 address, the resulting sub-networks will also
     * embed IPv4 addresses, e.g.:
     * <pre>
     * NetworkUtils.splitByPower(new IPNetwork("2002::123.45.67.64/122"), 3);
     * </pre>
     * ...will produce exactly 2<sup>3</sup> (=8) sub-networks matching the following
     * definitions:
     * <pre>
     * "2002::123.45.67.64/125",
     * "2002::123.45.67.72/125",
     * "2002::123.45.67.80/125",
     * "2002::123.45.67.88/125",
     * "2002::123.45.67.96/125",
     * "2002::123.45.67.104/125",
     * "2002::123.45.67.112/125",
     * "2002::123.45.67.120/125"
     * </pre>
     * </p>
     * 
     * @param network The {@link IPNetwork} to split (may be <code>null</code>)
     * @param x The number of networks to produce described as a power of 2
     * (2<sup>x</sup>)
     * 
     * @return the specified number of sub-networks or <code>null</code>
     * 
     * @throws IllegalArgumentException if the given number of networks to
     * produce is invalid
     * 
     * @throws IllegalStateException if an unexpected exception occurs while
     * calculating the new networks
     * 
     * @see IPNetwork#next() 
     * @see IPNetwork#hasEmbeddedIPv4() 
     */
    public static IPNetwork[] splitByPower(final IPNetwork network, int x) {
        if(network == null) {
            return null;
        }

        final int masklen = network.getNetmask() + x;
        final int maxlen = network.isV6() ? 128 : 32;
        final int n = (int) Math.pow(2, x);

        if(n < split_min) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is lower than "
                    + split_min);
        } else if(n > split_max) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): value is higher than "
                    + split_max);
        }
        
        if(masklen > maxlen) {
            throw new IllegalArgumentException("Unable to split " + network
                    + " into " + n + " network(s): not enough free bits in netmask");
        }
        
        final IPNetwork[] nets = new IPNetwork[n];
        nets[0] = new IPNetwork(network.getValue().split("/")[0] + "/" + masklen);

        for(int i=1; i<n; i++) {
            nets[i] = nets[i-1].next();
        }
        
        return nets;
    }

    /**
     * Split a given {@link IPNetwork} into exactly 2 sub-networks.
     * 
     * <p>
     * This method creates the 2 networks which all are sub-network definitions
     * of the given one, filling the space completely.
     * </p>
     * 
     * <p>
     * Example:
     * <pre>
     * NetworkUtils.split(new IPNetwork("192.168.107.0/24"));
     * </pre>
     * ...will produce exactly 2<sup>4</sup> (=16) sub-networks matching the following
     * definitions:
     * <pre>
     * "192.168.107.0/25",
     * "192.168.107.128/25"
     * </pre>
     * </p>
     * 
     * <p>
     * In case <code>null</code> is given for the input {@link IPNetwork} the
     * result of this method will also be <code>null</code>.
     * </p>
     * 
     * @param network The {@link IPNetwork} to split (may be <code>null</code>)
     * 
     * @return exactly 2 sub-networks or <code>null</code>
     * 
     * @throws IllegalArgumentException if the given number of networks to
     * produce is invalid
     * 
     * @throws IllegalStateException if an unexpected exception occurs while
     * calculating the new networks
     */
    public static IPNetwork[] split(final IPNetwork network) {
        return splitByPower(network, 1);
    }
    
    public static IPNetwork merge(final IPNetwork[] networks) {
        if(networks == null) {
            return null;
        }

        final int n = networks.length;
        int x = 1;

        while((1<<x) < n) {
            x++;
        }
        
        if(Math.pow(2D, (double) x) != (double) n) {
            throw new IllegalArgumentException("Unable to merge " + n
                    + " networks into one: invalid number of sub-networks");
        }

        IPNetwork last = networks[0];
        final int masklen = last.getNetmask() - x;

        if(masklen < 1) {
            throw new IllegalArgumentException("Unable to merge " + n
                    + " networks into one: any address");
        }

        for(int k=1; k<networks.length; k++) {
            if(!last.next().equals(networks[k])) {
                // networks not in order or invalid...
                throw new IllegalArgumentException("Unable to merge " + n
                        + " networks into one: arguments not in order or invalid");
            }
            last = networks[k];
        }
        
        // get possible super-network address...
        byte[] low = new byte[last.isV6() ? 16 : 4];
        int i = masklen / 8;
        int s = 8 - (masklen % 8);
        System.arraycopy(last.getAddr() , 0, low, 0, i);

        // unset least significant bits...
        if(s > 0 && s < 8) {
            byte b = (byte) last.getAddr()[i];
            for(int j=0; j<s; j++) {
                b = (byte) (b & ~(1 << j));
            }
            low[i] = b;
        }
        
        // try to create the super-network...
        IPNetwork net = null;
        try {
            InetAddress inet = InetAddress.getByAddress(low);
            net = new IPNetwork(inet.getHostAddress() + "/" + masklen);
            net.setEmbeddedIPv4(last.hasEmbeddedIPv4());
        } catch(UnknownHostException ex) {
            throw new IllegalStateException("Unable to calculate merged network", ex);
        }
        
        return net;
    }
}
