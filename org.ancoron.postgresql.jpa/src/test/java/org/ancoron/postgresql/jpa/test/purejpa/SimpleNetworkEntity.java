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
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_simple_network")
public class SimpleNetworkEntity implements Serializable {
    
    @Id
    @Column(name = "c_net")
    private IPNetwork net;

    @Column(name = "c_name")
    private String name;

    @OneToMany(mappedBy = "network", orphanRemoval = false, cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    private Set<SimpleNICEntity> devices = new HashSet<SimpleNICEntity>();

    public SimpleNetworkEntity() {
    }

    public SimpleNetworkEntity(IPNetwork net, String name) {
        this.net = net;
        this.name = name;
    }

    public SimpleNetworkEntity(String net, String name) {
        if(net != null) {
            this.net = new IPNetwork(net);
        }
        this.name = name;
    }

    public IPNetwork getNet() {
        return net;
    }

    public void setNet(IPNetwork net) {
        this.net = net;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SimpleNICEntity> getDevices() {
        return devices;
    }

    public void setDevices(Set<SimpleNICEntity> devices) {
        this.devices = devices;
    }

    public void addDevice(SimpleNICEntity nic) {
        getDevices().add(nic);
        nic.setNetwork(this);
    }

    public void removeDevice(SimpleNICEntity nic) {
        getDevices().remove(nic);
        nic.setNetwork(null);
    }
}
