package com.eshqol.sigma
import android.app.Activity
import android.content.Context
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class DataBase {

    private val database = Firebase.database
    private val db = Firebase.firestore

    fun write(text: String, path: String) {
        val myRef = database.getReference(path)
        myRef.setValue(text)
    }

    fun onCreate(username: String, c: String) {
        val coins = hashMapOf("0" to 2000,
                                "1" to 0,
                                "2" to 0,
                                "3" to 0,
                                "a" to 0,
                                "c" to c)

        db.collection("root").document(username).set(coins)
    }

    fun setValue(username: String, path: String, value: Int) {
        db.collection("root").document(username).update(path, value)
    }

    fun setString(username: String, path: String, value: String) {
        db.collection("root").document(username).update(path, value)
    }


    fun addGame(username: String, result: String) {
        val docRef = db.collection("root").document(username)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val current = document.data!![result].toString().toInt()
                setValue(username, result, current + 1)
            }
        }
    }

    fun updateProfile(username: String, id: Int) {
        val docRef = db.collection("root").document(username)

        docRef.get().addOnSuccessListener {
            setValue(username, "p", id)
        }
    }

    fun updateCoins(activity: Activity, coins: Int) {
        val username = Helpers().getUsername()
        val db = Firebase.firestore
        val docRef = db.collection("root").document(username)

        docRef.get().addOnSuccessListener { document ->
            val current = document.data!!["0"].toString().toInt()
            setValue(username, "0", current + coins)

            val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            val amountC = (current + coins).toString()
            editor.putString("coins", amountC)
            editor.apply()
        }
    }

    fun updateRecord(level: String, time: Float){
        val username = Helpers().getUsername()
        setString(username, "${level}_record", time.toString())
    }

}

