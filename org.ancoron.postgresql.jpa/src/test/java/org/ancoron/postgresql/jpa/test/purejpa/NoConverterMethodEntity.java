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
import java.net.InetAddress;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_mapping_noconvmeth")
public class NoConverterMethodEntity implements Serializable {

    private Long id;
    private UUID uuid;
    private InetAddress inetAddr;
    private PGcidr cidr;
    private PGmacaddr macaddr;
    private IPNetwork network;
    private IPTarget target;
    private PGinet inet;

    @Column(name = "c_uuid")
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(name="c_cidr")
    public PGcidr getCidr() {
        return cidr;
    }

    public void setCidr(PGcidr cidr) {
        this.cidr = cidr;
    }

    @Id
    @GeneratedValue(generator = "sequence")
    @Column(name="c_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="c_inet_addr")
    public InetAddress getInetAddr() {
        return inetAddr;
    }

    public void setInetAddr(InetAddress inetAddr) {
        this.inetAddr = inetAddr;
    }

    @Column(name="c_macaddr")
    public PGmacaddr getMacaddr() {
        return macaddr;
    }

    public void setMacaddr(PGmacaddr macaddr) {
        this.macaddr = macaddr;
    }

    @Column(name="c_inet")
    public PGinet getInet() {
        return inet;
    }

    public void setInet(PGinet inet) {
        this.inet = inet;
    }

    @Column(name="c_ip_network")
    public IPNetwork getNetwork() {
        return network;
    }

    public void setNetwork(IPNetwork network) {
        this.network = network;
    }

    @Column(name="c_ip_target")
    public IPTarget getTarget() {
        return target;
    }

    public void setTarget(IPTarget target) {
        this.target = target;
    }
}
