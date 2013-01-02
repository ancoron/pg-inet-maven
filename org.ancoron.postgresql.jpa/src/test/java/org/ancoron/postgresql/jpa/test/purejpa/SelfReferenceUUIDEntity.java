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
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_uuid_self_ref")
public class SelfReferenceUUIDEntity implements Serializable {
    
    @Id
    @Column(name = "c_uuid")
    private UUID uuid;

    @Column(name = "c_name")
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(name = "test_uuid_self_ref_targets",
            joinColumns = {@JoinColumn(name = "fk_source")},
            inverseJoinColumns = {@JoinColumn(name = "fk_target")}
            )
    private Set<SelfReferenceUUIDEntity> targets = new HashSet<SelfReferenceUUIDEntity>();

    @ManyToMany(mappedBy = "targets", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<SelfReferenceUUIDEntity> sources = new HashSet<SelfReferenceUUIDEntity>();

    public SelfReferenceUUIDEntity() {
    }

    public SelfReferenceUUIDEntity(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SelfReferenceUUIDEntity> getTargets() {
        return targets;
    }

    public void setTargets(Set<SelfReferenceUUIDEntity> targets) {
        this.targets = targets;
    }

    public Set<SelfReferenceUUIDEntity> getSources() {
        return sources;
    }

    public void setSources(Set<SelfReferenceUUIDEntity> sources) {
        this.sources = sources;
    }

    public void addTarget(UUID target) {
        SelfReferenceUUIDEntity t = new SelfReferenceUUIDEntity(uuid, null);
        addTarget(t);
    }

    public void addTarget(SelfReferenceUUIDEntity t) {
        getTargets().add(t);
        t.getSources().add(this);
    }
}
