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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name = "test_mapping_noconv_2")
public class InheritedNoConvEntity extends AbstractNoConvEntity implements Serializable {

    @ElementCollection
    @CollectionTable(name="test_mapping_noconv_2_dns",
            joinColumns=@JoinColumn(name="fk_noconv_2_id"))
    @Column(name="c_ip")
    public List<InetAddress> dnsServers = new ArrayList<InetAddress>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="test_mapping_noconv_2_arp",
            joinColumns=@JoinColumn(name="fk_noconv_2_id"))
    @MapKeyColumn(name="c_mac")
    @Column(name="c_ip")
    public Map<PGmacaddr, PGinet> arp = new HashMap<PGmacaddr, PGinet>();

    @Column(name="c_uuid")
    public UUID uuid;
}
