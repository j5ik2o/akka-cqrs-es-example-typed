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
package com.github.j5ik2o.adceet.api.write;

import akka.cluster.Member;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import java.util.Locale;

public final class RoleNames {
  private final Seq<RoleName> values;

  public RoleNames(Seq<RoleName> values) {
    this.values = values;
  }

  public Seq<RoleName> asSeq() {
    return values;
  }

  static RoleNames from(Member member) {
    Vector<RoleName> frontend, backend;
    if (member.hasRole(RoleName.FRONTEND.toString().toLowerCase(Locale.getDefault()))) {
      frontend = Vector.of(RoleName.FRONTEND);
    } else {
      frontend = Vector.empty();
    }
    if (member.hasRole(RoleName.FRONTEND.toString().toLowerCase(Locale.getDefault()))) {
      backend = Vector.of(RoleName.FRONTEND);
    } else {
      backend = Vector.empty();
    }
    return new RoleNames(frontend.appendAll(backend));
  }

  public boolean contains(RoleName roleName) {
    return values.contains(roleName);
  }
}
