# Streamly-Kafka-Cassandra

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Introduction](#introduction)
- [Quickstart](#quickstart)
  - [1. Build the project](#1-build-the-project)
  - [2. Create an account](#2-create-an-account)
  - [3. Choose the topic to read from](#3-choose-the-topic-to-read-from)
  - [4. Create your keyspace](#4-create-your-keyspace)
  - [5. Get your access and secret keys](#5-get-your-access-and-secret-keys)
  - [6. Update your configuration file](#6-update-your-configuration-file)
  - [7. Submit your application](#7-submit-your-application)
  - [8. Monitor your application](#8-monitor-your-application)
  - [9. Check your application logs](#9-check-your-application-logs)
  - [10. Visualize your data](#10-visualize-your-data)
- [Copyright](#copyright)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Introduction

This is a sample stream processing application that you can deploy in [Streamly].
It is written in Java and consumes events from [Kafka] after every 2 seconds, counts those events and writes aggregates to [Cassandra].


## Quickstart


### 1. Build the project

Assuming git, java, and maven are installed on your machine. Issue the following commands in your terminal :

```bash
 host$ git clone https://github.com/streamlyio/streamly-spark-examples.git
 host$ cd streamly-spark-examples/streamly-kafka-cassandra
 host$ mvn clean install
```

### 2. Create an account
 - Go to [Streamly Registration Page][streamly-signup] and sign up by providing your email address and a namespace. <br /> 
  The namespace is a string that Streamly uses to scope resources. For instance, any keyspace, index, application, or topic you create must have a name that is prefixed with your namespace.  <br />

    **A user has one single namespace. Once an account is created, the associated namespace cannot be changed. Therefore, be sure to choose your namespace carefully.**

![streamly-signup-step1][streamly-signup-step1]

After about 2 min of clicking on `Sign up`, you should receive a confimation email. Click on this email to land on the following screen.

 - Complete your registration 

![streamly-signup-step2][streamly-signup-step2]

 - Log in into [Streamly] with your email and password

In the following steps, we assume the namespace is `greenspace`.

### 3. Choose the topic to read from
There are [Open Streams][open-streams] topics available to all registered users :

| Name                         | Description                                            	   |
|------------------------------|-------------------------------------------------------------------|
| system-bitcoin-transactions  |  real-time transaction events from bitcoin network                |
| system-bitcoin-peers         |  real-time peer events from bitcoin network                       |
| system-bitcoin-blocks        |  real-time block events from bitcoin network                      |
| system-ethereum-transactions |  real-time transaction events from ethereum network               |
| system-ethereum-blocks       |  real-time block events from ethereum network		    	   |
| system-ethereum-hashs        |  real-time (transaction/block) hash events from  ethereum network |                         
| system-ethereum-extras       |  misc real-time events from ethereum network           	   |
| system-apache-logs           | It contains apache logs gathered from various servers          |


This example consumes events from `system-bitcoin-transactions`.

### 4. Create your keyspace
To create a new keyspace :

  - Open the Streamly dashboard and switch to the Cassandra tab
  - Specify the keyspace name in the corresponding text field (e.g. `greenspace_keyspace`). Be sure to prefix it with your namespace.
  - Choose a replication strategy (e.g. `SimpleStrategy`) and define the replication factor (e.g. `1`)

![streamly-create-keyspace][streamly-create-keyspace]

  - Click on Create Keyspace button

The newly created keyspace should appear in the list of existing keyspaces on the right side of the screen:

![streamly-list-keyspace][streamly-list-keyspace]

The job will create the table precise in the spark.properties file (e.g. `greenspace_table`) with the adequate fields for you.

### 5. Get your access and secret keys
  - Open the Streamly dashboard and click on ![profile][profile]
  - Copy your access and secret keys in the `Access Keys Management` section

![streamly-list-apikeys][streamly-list-apikeys]

In this example : access key is `ci00jji37jfhq8q` and secret key is `r30qwridiw8qkxj`.
Access and secret keys authenticate users on Streamly services (Kafka, Mqtt, Cassandra, Elasticsearch,etc.).

### 6. Update your configuration file
Open `spark.properties` file and edit as appropriate.

| Name                                  | Description                						  |
|---------------------------------------|-----------------------------------------------------|
| main.class                            | The entry point for your application                |
| app.args                              | Arguments passed to the main method                 |
| app.resource                          | Name of the bundled jar including your application  |
| spark.cassandra.connection.port       | Cassandra native connection port                    |
| spark.cassandra.connection.host       | Comma separated list of Cassandra hosts  addresses  |
| spark.cassandra.auth.username         | Access key          			                      |
| spark.cassandra.auth.password         | Secret key             							  |

The resulting file should look as depicted below:

```properties
main.class=io.streamly.examples.StreamlyKafkaCassandra
app.args=apps.streamly.io:29093,system-bitcoin-transactions,greenspace_keyspace,greenspace_table
app.resource=file://streamly-kafka-cassandra-0.0.1.jar
spark.cassandra.connection.port=9042
spark.cassandra.connection.host=london201.streamly.io,london202.streamly.io,london205.streamly.io
spark.cassandra.auth.username=ci00jji37jfhq8q
spark.cassandra.auth.password=r30qwridiw8qkxj
```
### 7. Submit your application 
 - Open the Processing tab in the Streamly dashboard
 - Click on Add Application. A new application is created with name: `No Name`.
 - Provide a valid name for your application and click on ![save][save]. Again, your application name should start with your namespace. In this example the application name is `greenspace-kafka-cassandra`.
 - Upload `spark.properties` and `streamly-kafka-cassandra-0.0.1.jar` files
 - Click on ![start][start]

![streamly-kafka-cassandra][streamly-kafka-cassandra]

### 8. Monitor your application
Wait until your application's status changes to RUNNING. Click on ![show-ui][show-ui]. You should subsequently see a screen similar to below screen:

![streamly-kafka-cassandra-spark-ui][streamly-kafka-cassandra-spark-ui]
You can see how your Spark Streaming application _processes_ the Kafka events.

### 9. Check your application logs
Application logs are populated in Elasticsearch and can be visualized in Kibana. No manual configuration needed.

![streamly-kafka-cassandra-kibana-ui][streamly-kafka-cassandra-kibana-ui]

### 10. Visualize your data
  - In Streamly dashboard, go to Notebook tab
  - Create a new note
  - Query your table and explore your data

![streamly-kafka-cassandra-zeppelin-cassandra][streamly-kafka-cassandra-zeppelin-cassandra]

## Copyright
Copyright © 2017 Streamly, Inc.

[streamly]: https://board.streamly.io:20080
[streamly-signup]: https://board.streamly.io:20080/#/signup
[kafka]: https://kafka.apache.org/
[cassandra]: http://cassandra.apache.org/
[streamly-kafka-cassandra-spark-ui]: https://cloud.githubusercontent.com/assets/25694018/23463973/b8c5ab1a-fe93-11e6-99af-e580f0e4a7f8.png
[streamly-kafka-cassandra]: https://cloud.githubusercontent.com/assets/25694018/23463864/5e5b2394-fe93-11e6-907c-c7f45f88cd2f.png
[streamly-kafka-cassandra-kibana-ui]: https://cloud.githubusercontent.com/assets/25694018/23464035/e0156926-fe93-11e6-9d07-5e7a5e8ebfec.png
[streamly-kafka-cassandra-zeppelin-cassandra]: https://cloud.githubusercontent.com/assets/25694018/23656194/f5edaaf2-0338-11e7-9a65-58983d0c122e.png
[streamly-create-topic]: https://cloud.githubusercontent.com/assets/25694018/23129771/4375024a-f784-11e6-97ca-7d3b16b06929.png
[streamly-create-keyspace]: https://cloud.githubusercontent.com/assets/25694018/23342425/61cf2970-fc5a-11e6-81c3-6e5aab35e71e.png
[streamly-signup-step1]: https://cloud.githubusercontent.com/assets/25694018/23342086/2d3072e2-fc54-11e6-93b3-30223946e8d8.png
[streamly-signup-step2]: https://cloud.githubusercontent.com/assets/25694018/23342085/2d303ce6-fc54-11e6-8839-b9b6c00d2efd.png
[streamly-list-keyspace]: https://cloud.githubusercontent.com/assets/25694018/23607834/c53a97d0-0266-11e7-94eb-b3271812b28c.png
[streamly-list-apikeys]: https://cloud.githubusercontent.com/assets/25694018/23631833/ff32f0f0-02bf-11e7-9bca-8ccf17224620.png
[open-streams]: http://www.streamly.io/open-streams/
[save]: https://cloud.githubusercontent.com/assets/25694018/23614986/3086f3da-0285-11e7-9eb0-0c141e1fb5ff.png
[start]: https://cloud.githubusercontent.com/assets/25694018/23615196/e7976a50-0285-11e7-92d0-e10c1bab0165.png
[profile]: https://cloud.githubusercontent.com/assets/25694018/23615301/3da3d06e-0286-11e7-8118-038ee1a22e92.png
[show-ui]: https://cloud.githubusercontent.com/assets/25694018/23653314/64a964c0-032c-11e7-9610-4d89de66e7bf.png
