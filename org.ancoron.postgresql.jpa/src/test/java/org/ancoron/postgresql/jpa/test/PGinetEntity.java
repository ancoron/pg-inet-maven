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
package org.ancoron.postgresql.jpa.test;

import org.ancoron.postgresql.jpa.eclipselink.PGinetConverter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.postgresql.net.PGinet;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_ip_block")
@Converter(name="pginetConverter", converterClass=PGinetConverter.class)
public class PGinetEntity implements Serializable {
    
    @Id
    @GeneratedValue(generator = "sequence")
    @Column(name="c_id")
    private Long id;
    
    @ManyToOne(cascade= CascadeType.ALL, optional=true)
    @JoinColumn(name="fk_parent", nullable=true)
    private PGinetEntity parent;

    @OneToMany(cascade={}, mappedBy="parent", orphanRemoval=true)
    private Set<PGinetEntity> childs = new HashSet<PGinetEntity>();

    @Convert("pginetConverter")
    @Column(name="c_network", nullable=false, insertable=true, updatable=false)
    private PGinet network;

    public PGinetEntity() {
    }
    
    public PGinetEntity(String network) throws SQLException {
        this.network = new PGinet(network);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the value of parent
     *
     * @return the value of parent
     */
    public PGinetEntity getParent() {
        return parent;
    }

    /**
     * Set the value of parent
     *
     * @param parent new value of parent
     */
    public void setParent(PGinetEntity parent) {
        this.parent = parent;
    }

    /**
     * Get the value of childs
     *
     * @return the value of childs
     */
    public Set<PGinetEntity> getChilds() {
        return childs;
    }

    /**
     * Set the value of childs
     *
     * @param childs new value of childs
     */
    public void setChilds(Set<PGinetEntity> childs) {
        this.childs = childs;
    }

    /**
     * Get the value of network
     *
     * @return the value of network
     */
    public PGinet getNetwork() {
        return network;
    }

    /**
     * Set the value of network
     *
     * @param network new value of network
     */
    public void setNetwork(PGinet network) {
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
        if (!(object instanceof PGinetEntity)) {
            return false;
        }
        PGinetEntity other = (PGinetEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ancoron.postgresql.jpa.test.PGcidrEntity[ id=" + id + " ]";
    }
}
