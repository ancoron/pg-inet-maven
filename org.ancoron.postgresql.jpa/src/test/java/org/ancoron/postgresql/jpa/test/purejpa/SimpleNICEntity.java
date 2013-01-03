/*
 * Copyright 2013 ancoron.
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
package org.ancoron.postgresql.jpa.test.purejpa;

import java.io.Serializable;
import java.sql.SQLException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPTarget;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_simple_nic")
public class SimpleNICEntity implements Serializable {
    
    @Id
    @Column(name = "c_mac")
    private PGmacaddr mac;

    @Column(name = "c_ip")
    private IPTarget ip;

    @Column(name = "c_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "fk_network")
    private SimpleNetworkEntity network;

    public SimpleNICEntity() {
    }

    public SimpleNICEntity(PGmacaddr mac, IPTarget ip, String name) {
        this.mac = mac;
        this.ip = ip;
        this.name = name;
    }

    public SimpleNICEntity(String mac, String ip, String name) {
        if(mac != null) {
            try {
                this.mac = new PGmacaddr(mac);
            } catch(SQLException x) {
                throw new IllegalArgumentException(x);
            }
        }
        if(ip != null) {
            this.ip = new IPTarget(ip);
        }
        this.name = name;
    }

    public PGmacaddr getMac() {
        return mac;
    }

    public void setMac(PGmacaddr mac) {
        this.mac = mac;
    }

    public IPTarget getIp() {
        return ip;
    }

    public void setIp(IPTarget ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleNetworkEntity getNetwork() {
        return network;
    }

    public void setNetwork(SimpleNetworkEntity network) {
        this.network = network;
    }
}
