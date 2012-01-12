/*
 * Copyright 2011 ancoron.
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
import java.net.InetAddress;
import java.sql.SQLException;
import org.postgresql.net.PGinet;

/**
 *
 * @author ancoron
 */
public class Network implements Serializable {

    private PGinet net = null;
    private byte[] broadcast;
    private InetAddress host;
    private byte[] hostmask;
    private short maskLength;
    private byte[] netmask;
    
    private boolean v6;
    
    public Network() {}
    
    public Network(PGinet net) {
        this.net = net;
    }
    
    public Network(String net) {
        try {
            this.net = new PGinet(net);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to construct network", ex);
        }
    }

    public PGinet getNet() {
        return net;
    }

    public void setNet(PGinet net) {
        this.net = net;
    }

    public byte[] getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(byte[] broadcast) {
        this.broadcast = broadcast;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public byte[] getHostmask() {
        return hostmask;
    }

    public void setHostmask(byte[] hostmask) {
        this.hostmask = hostmask;
    }

    public short getMaskLength() {
        return maskLength;
    }

    public void setMaskLength(short maskLength) {
        this.maskLength = maskLength;
    }

    public byte[] getNetmask() {
        return netmask;
    }

    public void setNetmask(byte[] netmask) {
        this.netmask = netmask;
    }

    public boolean isV6() {
        return v6;
    }

    public void setV6(boolean v6) {
        this.v6 = v6;
    }

    @Override
    public String toString() {
        return net != null ? net.toString() : "null";
    }
}
