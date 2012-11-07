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
package org.ancoron.postgresql.jpa.test;

import java.util.List;
import java.util.UUID;
import javax.ejb.Local;

/**
 *
 * @author ancoron
 */
@Local
public interface UUIDTestBeanLocal {

    UUID addUUID(String name);

    UUIDTestEntity findByUUID(UUID uuid);

    UUIDTestEntity deleteUUID(UUID uuid);

    UUIDTestEntity findByName(String name);

    List<UUID> findAllIds();
    
}
