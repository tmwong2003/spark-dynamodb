/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  *
  * Copyright © 2018 AudienceProject. All rights reserved.
  */
package com.audienceproject.spark.dynamodb

import com.amazonaws.services.dynamodbv2.model.{AttributeDefinition, CreateTableRequest, KeySchemaElement, ProvisionedThroughput}
import com.audienceproject.spark.dynamodb.implicits._

class WriteRelationTest extends AbstractInMemoryTest {

    test("Inserting from a local Dataset") {
        dynamoDB.createTable(new CreateTableRequest()
            .withTableName("InsertTest1")
            .withAttributeDefinitions(new AttributeDefinition("name", "S"))
            .withKeySchema(new KeySchemaElement("name", "HASH"))
            .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L)))

        import spark.implicits._

        val newItemsDs = spark.createDataset(Seq(
            ("lemon", "yellow", 0.1),
            ("orange", "orange", 0.2),
            ("pomegranate", "red", 0.2)
        ))
            .withColumnRenamed("_1", "name")
            .withColumnRenamed("_2", "color")
            .withColumnRenamed("_3", "weight")
        newItemsDs.write.dynamodb("InsertTest1")

        val validationDs = spark.read.dynamodb("InsertTest1")
        assert(validationDs.count() === 3)
        assert(validationDs.select("name").as[String].collect().forall(Seq("lemon", "orange", "pomegranate") contains _))
        assert(validationDs.select("color").as[String].collect().forall(Seq("yellow", "orange", "red") contains _))
        assert(validationDs.select("weight").as[Double].collect().forall(Seq(0.1, 0.2, 0.2) contains _))
    }

}
