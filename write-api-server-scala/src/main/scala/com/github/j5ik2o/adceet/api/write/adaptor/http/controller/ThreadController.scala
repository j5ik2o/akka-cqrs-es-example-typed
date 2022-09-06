package com.github.j5ik2o.adceet.api.write.adaptor.http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.j5ik2o.adceet.api.write.adaptor.http.json._
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.Validator
import com.github.j5ik2o.adceet.api.write.domain.{ Message, MessageId, ThreadId }
import com.github.j5ik2o.adceet.api.write.use.`case`.{ AddMemberUseCase, AddMessageUseCase, CreateThreadUseCase }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{ Operation, Parameter }
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{ POST, Path }

class ThreadController(
    private val createThreadUseCase: CreateThreadUseCase,
    private val addMemberUseCase: AddMemberUseCase,
    private val addMessageUseCase: AddMessageUseCase
) extends FailFastCirceSupport {

  def toRoute: Route = {
    concat(createThread, addMember, addMessage)
  }

  @Path("/threads")
  @POST
  @Operation(
    description = "Create a thread",
    method = "POST",
    requestBody = new RequestBody(
      required = true,
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[CreateThreadRequestJson]),
          mediaType = MediaType.APPLICATION_JSON
        )
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[CreateThreadResponseJson]),
            mediaType = MediaType.APPLICATION_JSON
          )
        )
      )
    )
  )
  def createThread: Route = {
    path("threads") {
      post {
        entity(as[CreateThreadRequestJson]) { json =>
          extractExecutionContext { implicit ec =>
            Validator
              .validateAccountId(json.accountId).fold(
                { errors =>
                  reject(validation.ValidationRejection(errors))
                },
                { accountId =>
                  val result = createThreadUseCase.execute(ThreadId(), accountId)
                  onSuccess(result) { threadId =>
                    complete(StatusCodes.OK, CreateThreadResponseJson(threadId.asString))
                  }
                }
              )
          }
        }
      }
    }
  }

  @Path("/threads/{threadId}/members")
  @POST
  @Operation(
    description = "Add a member to the thread",
    method = "POST",
    parameters = Array(new Parameter(name = "threadId", `in` = ParameterIn.PATH)),
    requestBody = new RequestBody(
      description = "add member request",
      required = true,
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[AddMemberRequestJson]),
          mediaType = MediaType.APPLICATION_JSON
        )
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[AddMemberRequestJson]),
            mediaType = MediaType.APPLICATION_JSON
          )
        )
      )
    )
  )
  def addMember: Route = {
    path("threads" / Segment / "members") { threadIdString =>
      post {
        entity(as[AddMemberRequestJson]) { json =>
          extractExecutionContext { implicit ec =>
            Validator
              .validateThreadIdWithAccountId(threadIdString, json.accountId).fold(
                { errors =>
                  reject(validation.ValidationRejection(errors))
                },
                { case (threadId, accountId) =>
                  val result = addMemberUseCase.execute(threadId, accountId)
                  onSuccess(result) { threadId =>
                    complete(StatusCodes.OK, AddMemberResponseJson(threadId.asString))
                  }
                }
              )
          }
        }
      }
    }
  }

  @Path("/threads/{threadId}/messages")
  @POST
  @Operation(
    description = "Add a message to the thread",
    method = "POST",
    parameters = Array(new Parameter(name = "threadId", `in` = ParameterIn.PATH)),
    requestBody = new RequestBody(
      description = "add message request",
      required = true,
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[AddMessageRequestJson]),
          mediaType = MediaType.APPLICATION_JSON
        )
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "OK",
        content = Array(
          new Content(
            schema = new Schema(implementation = classOf[AddMessageResponseJson]),
            mediaType = MediaType.APPLICATION_JSON
          )
        )
      )
    )
  )
  def addMessage: Route = {
    path("threads" / Segment / "messages") { threadIdString =>
      post {
        entity(as[AddMessageRequestJson]) { json =>
          extractExecutionContext { implicit ec =>
            Validator
              .validateThreadIdWithAccountId(threadIdString, json.accountId).fold(
                { errors =>
                  reject(validation.ValidationRejection(errors))
                },
                { case (threadId, accountId) =>
                  val message = Message(MessageId(), threadId, accountId, json.body)
                  val result  = addMessageUseCase.execute(message)
                  onSuccess(result) { threadId =>
                    complete(StatusCodes.OK, AddMessageResponseJson(threadId.asString, accountId.asString))
                  }
                }
              )
          }
        }
      }
    }
  }

}
