package com.eshqol.sigma

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Leaderboard : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        this.title = "Leaderboard"
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.leaderboard


        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.practice -> {
                    val switchActivityIntent = Intent(this, Practice::class.java)
                    startActivity(switchActivityIntent)
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                    finish()
                }
                R.id.home -> {
                    val switchActivityIntent = Intent(this, HomeScreen::class.java)
                    startActivity(switchActivityIntent)
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                    finish()
                }
            }
            true
        }

        val recyclerview = findViewById<RecyclerView>(R.id.table)
        val names = ArrayList<String>()
        val scores = ArrayList<String>()
        val profilesImages = ArrayList<String>()
        val countries = ArrayList<String>()

        db.collection("leaderboard").document("leaderboard").get(Source.CACHE).addOnSuccessListener { result ->
            val data = result.data!!
            for (document in data["leaderboard"].toString().split("@")) {
                val document1 = document.split(".")
                scores.add(document1[2])
                names.add(document1[0])
                countries.add(document1[1])
                profilesImages.add(document1[3])
            }

            val myAdapter = MyAdapter(this, names, scores, profilesImages, countries)
            recyclerview.adapter = myAdapter
            recyclerview.layoutManager = LinearLayoutManager(this)

        }


        val names1 = ArrayList<String>()
        val scores1 = ArrayList<String>()
        val profilesImages1 = ArrayList<String>()
        val countries1 = ArrayList<String>()

        db.collection("leaderboard").document("leaderboard").get().addOnSuccessListener { result ->
            val data = result.data!!
            for (document in data["leaderboard"].toString().split("@")) {
                val document1 = document.split(".")
                scores1.add(document1[2])
                names1.add(document1[0])
                countries1.add(document1[1])
                profilesImages1.add(document1[3])
            }

            val myAdapter = MyAdapter(this, names1, scores1, profilesImages1, countries1)
            recyclerview.adapter = myAdapter
            recyclerview.layoutManager = LinearLayoutManager(this)

        }


        val points = findViewById<TextView>(R.id.textView23)
        val pointsInfo = findViewById<ImageView>(R.id.imageView37)
        points.setOnClickListener {
            "Points are calculated based on the the number of coins a user have and the user winning rate".snack(this, duration = 5000, top = true)
        }
        pointsInfo.setOnClickListener {
            "Points are calculated based on the the number of coins a user have and the user winning rate".snack(this, duration = 5000, top = true)
        }


    }


    override fun onBackPressed() {
        try {
            val switchActivityIntent = Intent(this, HomeScreen::class.java)
            startActivity(switchActivityIntent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        } catch (_: Exception) {}
    }



}
