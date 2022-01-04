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

package com.sourcesense.joyce.core.dao;

import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaDao {

	Optional<SchemaEntity> get(String id);

	List<SchemaEntity> getAll(Boolean rootOnly);

	void save(SchemaEntity t);

	void delete(SchemaEntity t);

	List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace, Boolean rootOnly);

	List<SchemaEntity> getAllByReportsNotEmpty();

	List<String> getAllNamespaces();
}
