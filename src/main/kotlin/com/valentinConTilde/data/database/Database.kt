package com.valentinConTilde.data.database

import com.mongodb.kotlin.client.coroutine.MongoClient

object Database {

    // Access environment variables
    private val dbUser = System.getenv("DB_USER") ?: throw IllegalStateException("DB_USER not set")
    private val dbPassword = System.getenv("DB_PASSWORD") ?: throw IllegalStateException("DB_PASSWORD not set")
    private val dbServer = System.getenv("DB_SERVER") ?: throw IllegalStateException("DB_SERVER not set")

    // Replace the placeholder with your MongoDB deployment's connection string
    private val uri =  "mongodb+srv://$dbUser:$dbPassword@$dbServer?retryWrites=true&w=majority&appName=Cluster0"

    val mongoClient = MongoClient.create(uri)
    val database = mongoClient.getDatabase("valentin_on_my_way")


}