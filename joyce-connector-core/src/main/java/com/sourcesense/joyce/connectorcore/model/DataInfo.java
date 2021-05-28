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

package com.sourcesense.joyce.connectorcore.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "#{T(com.sourcesense.joyce.connectorcore.configuration.DatabaseCollections).getDataInfo()}")
public abstract class DataInfo implements Serializable {

    private static final long serialVersionUID = 8071709882740413262L;
    @Id
    protected String _id;
    protected Integer version = 0;
    protected String schemaKey;
    protected Date insertDate = new Date();

    protected DataInfo(
            String _id,
            Integer version,
            String schemaKey) {

        this._id = _id;
        this.version = version;
        this.schemaKey = schemaKey;
    }
}

