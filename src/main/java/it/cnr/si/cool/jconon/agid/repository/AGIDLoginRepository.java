/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.agid.repository;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AGIDLoginRepository {
    public static final String AGID_STATE = "agid-state";
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDLoginRepository.class);

    private final HazelcastInstance hazelcastInstance;

    public AGIDLoginRepository(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @CachePut(value = AGID_STATE)
    public String register() {
        return UUID.randomUUID().toString();
    }

    public boolean isStateValid(String state) {
        return hazelcastInstance.getMap(AGID_STATE)
                .entrySet()
                .stream()
                .map(objectObjectEntry -> objectObjectEntry.getValue())
                .map(String.class::cast)
                .anyMatch(s -> s.equalsIgnoreCase(state));
    }

    @CacheEvict(value = AGID_STATE, allEntries = true)
    public void removeAllState() {
        LOGGER.info("cleared all AGID Login state");
    }

}
