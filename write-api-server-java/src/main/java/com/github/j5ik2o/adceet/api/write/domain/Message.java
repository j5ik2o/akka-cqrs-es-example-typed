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

public class Message {

  public Message(MessageId id, ThreadId threadId, AccountId senderId, String body) {
    this.id = id;
    this.threadId = threadId;
    this.senderId = senderId;
    this.body = body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Message message = (Message) o;

    if (!id.equals(message.id)) return false;
    if (!threadId.equals(message.threadId)) return false;
    if (!senderId.equals(message.senderId)) return false;
    return body.equals(message.body);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + threadId.hashCode();
    result = 31 * result + senderId.hashCode();
    result = 31 * result + body.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Message{"
        + "id="
        + id
        + ", threadId="
        + threadId
        + ", senderId="
        + senderId
        + ", body='"
        + body
        + '\''
        + '}';
  }

  private final MessageId id;
  private final ThreadId threadId;
  private final AccountId senderId;
  private final String body;
}
