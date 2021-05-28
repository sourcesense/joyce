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

package com.sourcesense.joyce.core.pipeline;

import com.sourcesense.joyce.core.exception.PipePanic;

public class Pipeline<I, O> {

    private final Step<I, O> current;

    public Pipeline(Step<I, O> current) {
        this.current = current;
    }

    public <R> Pipeline<I, R> pipe(Step<O, R> next) {
        return new Pipeline<>(input -> next.process(current.process(input)));
    }

    public O execute(I input) throws PipePanic {
        return current.process(input);
    }
}
