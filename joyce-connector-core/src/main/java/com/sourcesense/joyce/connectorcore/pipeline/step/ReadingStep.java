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

package com.sourcesense.joyce.connectorcore.pipeline.step;

import com.sourcesense.joyce.connectorcore.dto.ProcessableData;
import com.sourcesense.joyce.connectorcore.model.DataInfo;
import com.sourcesense.joyce.connectorcore.dto.ProcessablePayload;
import com.sourcesense.joyce.core.pipeline.Step;
import com.sourcesense.joyce.core.pipeline.step.AbstractStep;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
public abstract class ReadingStep<I extends DataInfo, D extends ProcessableData>
        extends AbstractStep<Optional<Void>, ProcessablePayload<I, D>>
        implements Step<Optional<Void>, ProcessablePayload<I, D>> {

}
