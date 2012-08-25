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
package org.ancoron.postgresql.jpa.test.purejpa;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.postgresql.net.PGinet;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="test_mapping_noconv")
public class NoConverterEntity extends AbstractNoConvEntity implements Serializable {

    @Column(name="c_ip_network")
    public IPNetwork network;
    
    @Column(name="c_ip_target")
    public IPTarget target;
    
    @Column(name="c_inet")
    public PGinet inet;

    public NoConverterEntity() {
    }
}
