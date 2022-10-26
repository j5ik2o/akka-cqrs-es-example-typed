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
package com.github.j5ik2o.adceet.test

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.TestSuite
import slick.basic.DatabaseConfig
import slick.jdbc.SetParameter.SetUnit
import slick.jdbc.{ JdbcProfile, SQLActionBuilder }

import scala.concurrent.Future

trait Slick3SpecSupport extends ScalaFutures { this: TestSuite =>

  private var _dbConfig: DatabaseConfig[JdbcProfile] = _

  private var _profile: JdbcProfile = _

  def slickDbConfig = _dbConfig

  protected def tables: Seq[String]

  def slickJdbcProfile = _profile

  def jdbcDriverClassName: String
  def dbHost: String
  def dbPort: Int
  def dbName: String
  def dbUserName: String
  def dbPassword: String

  def truncate(): Seq[Int] = {
    implicit val ec = slickDbConfig.db.executor.executionContext
    val futures = tables.map { table =>
      val q = SQLActionBuilder(List(s"TRUNCATE TABLE $table"), SetUnit).asUpdate
      slickDbConfig.db.run(q)
    }
    Future.sequence(futures).futureValue
  }

  def setUpSlick(): Unit = {
    val config = ConfigFactory.parseString(s"""
         |slick {
         |  profile = "slick.jdbc.MySQLProfile$$"
         |  db {
         |    connectionPool = disabled
         |    driver = "$jdbcDriverClassName"
         |    url = "jdbc:mysql://$dbHost:$dbPort/$dbName?allowPublicKeyRetrieval=true&useSSL=false&user=$dbUserName&password=$dbPassword"
         |    user = "$dbUserName"
         |    password = "$dbPassword"
         |  }
         |}
      """.stripMargin)
    _dbConfig = DatabaseConfig.forConfig[JdbcProfile]("slick", config)
    _profile = slickDbConfig.profile
  }

  def tearDownSlick(): Unit = {
    slickDbConfig.db.shutdown
  }

}
