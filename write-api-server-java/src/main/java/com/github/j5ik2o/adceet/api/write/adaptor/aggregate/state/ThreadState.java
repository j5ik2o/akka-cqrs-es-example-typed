/*
 * Copyright 2022 Junichi Kato
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
package com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state;

import com.github.j5ik2o.adceet.api.write.domain.events.ThreadCreated;
import com.github.j5ik2o.adceet.api.write.domain.events.ThreadEvent;

public sealed interface ThreadState permits Empty, Just {

    default ThreadState applyEvent(ThreadEvent threadEvent) {
        return switch (this) {
            case Empty empty -> switch (threadEvent) {
                case ThreadCreated threadCreated -> empty.create(threadCreated);
                default -> throw new IllegalStateException("Unexpected value: " + threadEvent);
            };
            case Just just -> just.update(threadEvent);
        };
    }

}