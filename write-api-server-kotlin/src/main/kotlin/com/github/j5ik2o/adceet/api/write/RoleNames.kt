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
package com.github.j5ik2o.adceet.api.write

import akka.cluster.Member
import io.vavr.collection.Seq
import io.vavr.collection.Vector
import java.util.Locale

data class RoleNames(private val values: Seq<RoleName>) {
    companion object {
        fun from(member: Member): RoleNames {
            val roleNames = (
                    if (member.hasRole(RoleName.FRONTEND.toString().lowercase(Locale.getDefault()))) {
                        Vector.of(RoleName.FRONTEND)
                    } else {
                        Vector.empty()
                    }
                    ).appendAll(
                    if (member.hasRole(RoleName.FRONTEND.toString().lowercase(Locale.getDefault()))) {
                        Vector.of(RoleName.FRONTEND)
                    } else {
                        Vector.empty()
                    }
                )
            return RoleNames(roleNames)
        }
    }

    fun contains(roleName: RoleName): Boolean {
        return values.contains(roleName)
    }

    fun asSeq(): Seq<RoleName> {
        return values
    }

}
