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
package org.ancoron.postgresql.jpa.test;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.eclipselink.NetworkConverter;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_network_advanced")
@Converter(name="netConverter", converterClass=NetworkConverter.class)
public class AdvancedNetworkEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="c_id")
    private Long id;
    
    @Convert("netConverter")
    @Column(name="c_network", nullable=false)
    private IPNetwork network;

    public AdvancedNetworkEntity() {
    }
    
    public AdvancedNetworkEntity(IPNetwork network) {
        this.network = network;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the value of network
     *
     * @return the value of network
     */
    public IPNetwork getNetwork() {
        return network;
    }

    /**
     * Set the value of network
     *
     * @param network new value of network
     */
    public void setNetwork(IPNetwork network) {
        this.network = network;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AdvancedNetworkEntity)) {
            return false;
        }
        AdvancedNetworkEntity other = (AdvancedNetworkEntity) object;
        
        if(this.id == null && other.id == null) {
            return false;
        }
        
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ancoron.postgresql.jpa.test.AdvancedNetworkEntity[ id=" + id + " ]";
    }
}
