/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.connectorcore.dao;

import com.sourcesense.joyce.connectorcore.model.DataInfo;

import java.util.List;
import java.util.Optional;

public interface ConnectorDao<T extends DataInfo> {

    String INSERT_DATE = "insertDate";

    Optional<T> get(String id);
    List<T> getAll();
    T save(T dataInfo);
    List<T> saveAll(List<T> dataInfos);
    void delete(T dataInfo);
}
