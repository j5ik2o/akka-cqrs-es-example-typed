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
package com.github.j5ik2o.adceet.api.write.domain;

import com.github.j5ik2o.adceet.api.write.domain.errors.ThreadError;
import com.github.j5ik2o.adceet.api.write.domain.events.ThreadCreated;
import io.vavr.collection.Vector;
import io.vavr.control.Either;

import java.util.UUID;

public final class ThreadFactory {

    public static Either<ThreadError, ThreadCreated> create(ThreadId id, AccountId accountId) {
       return Either.right(new ThreadCreated(UUID.randomUUID(), id, accountId));
    }

    public static Thread applyEvent(ThreadCreated event) {
        return new Thread(event.threadId(), Vector.of(event.accountId()), Vector.empty());
    }

}
