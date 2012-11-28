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
package org.ancoron.postgresql.jpa.test.purejpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_uuid_pk_2")
public class UUIDPKEntity2 implements Serializable {
    
    private UUID mId;
    private String name;
    private NoConverterMethodEntity ncm;
    private Set<UUIDReferenceEntity2> refs = new HashSet<UUIDReferenceEntity2>();

    @Id
    @Column(name = "c_uuid")
    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        this.mId = id;
    }

    @Column(name = "c_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_ncm", nullable = false)
    public NoConverterMethodEntity getNcm() {
        return ncm;
    }

    public void setNcm(NoConverterMethodEntity ncm) {
        this.ncm = ncm;
    }

    @OneToMany(mappedBy = "reference", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    public Set<UUIDReferenceEntity2> getRefs() {
        return refs;
    }

    public void setRefs(Set<UUIDReferenceEntity2> refs) {
        this.refs = refs;
    }
}
