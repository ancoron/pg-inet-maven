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

import java.net.InetAddress;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.ancoron.postgresql.jpa.eclipselink.IPNetworkConverter;
import org.ancoron.postgresql.jpa.eclipselink.IPTargetConverter;
import org.ancoron.postgresql.jpa.eclipselink.InetAddressConverter;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.Converters;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name = "network_config")
@Converters({
    @Converter(name = "inetConverter", converterClass = InetAddressConverter.class),
    @Converter(name = "netConverter", converterClass = IPNetworkConverter.class),
    @Converter(name = "ipConverter", converterClass = IPTargetConverter.class)
})
public class NetworkConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="c_id")
    private Long id;
    
    @Column(name = "c_def")
    @Convert("netConverter")
    private IPNetwork definition;

    @Column(name = "c_gateway")
    @Convert("inetConverter")
    private InetAddress gateway;

    @ElementCollection
    @CollectionTable(
            name="network_config_dns",
            joinColumns=@JoinColumn(name="fk_netcfg"),
            uniqueConstraints={
                @UniqueConstraint(name="unq_net_cfg_dns_idx", columnNames={"fk_netcfg", "c_index"}),
                @UniqueConstraint(name="unq_net_cfg_dns_ip", columnNames={"fk_netcfg", "c_ip"})
            }
    )
    @Column(name="c_ip")
    @OrderColumn(name="c_index")
    @Convert("ipConverter")
    private List<IPTarget> dnsServers;

    public NetworkConfiguration() {
    }

    public NetworkConfiguration(IPNetwork definition, InetAddress gateway, List<IPTarget> dnsServers) {
        this.definition = definition;
        this.gateway = gateway;
        this.dnsServers = dnsServers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IPNetwork getDefinition() {
        return definition;
    }

    public void setDefinition(IPNetwork definition) {
        this.definition = definition;
    }

    public List<IPTarget> getDnsServers() {
        return dnsServers;
        // return null;
    }

    public void setDnsServers(List<IPTarget> dnsServers) {
        this.dnsServers = dnsServers;
    }

    public InetAddress getGateway() {
        return gateway;
    }

    public void setGateway(InetAddress gateway) {
        this.gateway = gateway;
    }
}
