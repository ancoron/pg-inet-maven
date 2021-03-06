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
package org.ancoron.postgresql.jpa.test.mapping;

import java.io.Serializable;
import java.net.InetAddress;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_mapping_inetaddress")
@SequenceGenerator(name = "sequence", sequenceName = "seq_test")
public class InetAddressEntity implements Serializable {

    @Id
    @GeneratedValue(generator = "sequence")
    @Column(name="c_id")
    private Long id;
    
    @Column(name="c_network", nullable=false)
    private InetAddress ip;

    public InetAddressEntity() {
    }
    
    public InetAddressEntity(InetAddress ip) {
        this.ip = ip;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
}
