package com.valentinConTilde.mongoSample


import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

// Create data class to represent a MongoDB document
data class Movie(val title: String, val year: Int, val cast: List<String>)

fun main() {
    // Access environment variables
    val dbUser = System.getenv("DB_USER") ?: throw IllegalStateException("DB_USER not set")
    val dbPassword = System.getenv("DB_PASSWORD") ?: throw IllegalStateException("DB_PASSWORD not set")
    val dbServer = System.getenv("DB_SERVER") ?: throw IllegalStateException("DB_SERVER not set")

    // Replace the placeholder with your MongoDB deployment's connection string
    val uri =  "mongodb+srv://$dbUser:$dbPassword@$dbServer?retryWrites=true&w=majority&appName=Cluster0"

    val mongoClient = MongoClient.create(uri)
    val database = mongoClient.getDatabase("sample_mflix")
    // Get a collection of documents of type Movie
    val collection = database.getCollection<Movie>("movies")

    runBlocking {
        val doc = collection.find(eq("title", "Back to the Future")).firstOrNull()
        if (doc != null) {
            println("\n\n\n")
            println(doc)
            println("\n\n\n")
        } else {
            println("No matching documents found.")
        }
    }

    mongoClient.close()
}

