package com.snakehandgestures

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private val db = FirebaseFirestore.getInstance()

fun init() {
    val auth = Firebase.auth

    auth.signInAnonymously()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Anonymous access completed. UID: ${auth.currentUser?.uid}")
            } else {
                println("Error during anonymous access: ${task.exception}")
            }
        }
}

data class Entry(
    val user_id: String,
    val score: Int
)

fun addScore(name: String, score: Int) {
    val scoresCollection = db.collection("scores")

    val query = scoresCollection.whereEqualTo("user_id", name)
    query.get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Add new score
                val newEntry = Entry(name, score)
                scoresCollection.add(newEntry)
                    .addOnSuccessListener { documentReference ->
                        println("Document added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Error while adding document: $e")
                    }
            } else {
                // If already exists an entry with the same name, overwrite the score
                val existingDocument = result.documents.first()
                val documentId = existingDocument.id
                val updatedEntry = Entry(name, score)

                scoresCollection.document(documentId)
                    .set(updatedEntry)
                    .addOnSuccessListener {
                        println("Document overwritten succesfully!")
                    }
                    .addOnFailureListener { e ->
                        println("Error while overwriting document: $e")
                    }
            }
        }
        .addOnFailureListener { e ->
            println("Error: $e")
        }
}

fun getSortedScores(callback: (List<Pair<String, Int>>) -> Unit) {
    val scoresCollection = db.collection("scores")

    val query = scoresCollection.orderBy("score", Query.Direction.DESCENDING)

    query.get()
        .addOnSuccessListener { result ->
            val sortedScores = mutableListOf<Pair<String, Int>>()

            for (document in result) {
                val name = document.getString("user_id") ?: ""
                val score = document.getLong("score")?.toInt() ?: 0
                sortedScores.add(Pair(name, score))
            }

            callback(sortedScores)
        }
        .addOnFailureListener { e ->
            println("Errore nella query: $e")
            callback(emptyList())  // In case of error, returns un empty list
        }
}