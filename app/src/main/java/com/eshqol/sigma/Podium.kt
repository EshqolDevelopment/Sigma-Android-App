package com.eshqol.sigma

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class Podium : AppCompatActivity() {

    private val database = Firebase.database
    private val username = Helpers().getUsername()
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podium)

        if (Random.nextBoolean()){
            loadAd()
        }

        val code = intent.getStringExtra("code")
        val players = intent.getStringArrayListExtra("players")
        val profiles = intent.getStringArrayListExtra("profiles")

        val level = intent.getStringExtra("level").toString()
        val myRef = database.getReference("friends/$level/$code/times")

        val username1 = findViewById<TextView>(R.id.username1)
        val username2 = findViewById<TextView>(R.id.username2)
        val username3 = findViewById<TextView>(R.id.username3)
        val username4 = findViewById<TextView>(R.id.username4)
        val username5 = findViewById<TextView>(R.id.username5)

        val homeButton = findViewById<Button>(R.id.home111)

        val score1 = findViewById<TextView>(R.id.score1)
        val score2 = findViewById<TextView>(R.id.score2)
        val score3 = findViewById<TextView>(R.id.score3)
        val score4 = findViewById<TextView>(R.id.score4)
        val score5 = findViewById<TextView>(R.id.score5)

        val img1 = findViewById<ImageView>(R.id.img1)
        val img2 = findViewById<ImageView>(R.id.img2)
        val img3 = findViewById<ImageView>(R.id.img3)
        val img4 = findViewById<ImageView>(R.id.img4)
        val img5 = findViewById<ImageView>(R.id.img5)

        val coinWin = findViewById<TextView>(R.id.coin_win)

        val finalResult = findViewById<TextView>(R.id.finalResult)

        homeButton.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        var stop = false

        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.value.toString()
                if (data != "null"){
                    val result = getResult(data)
                    val keys = result.keys
                    val times = result.values
                    when (keys.indexOf(username)){
                        0 -> {
                            username1.text = username.replace("_", "")
                            finalResult.text = "You won the game!"
                            val len = players!!.size
                            val amount = when (level) {
                               "easy" -> 50 * len
                               "medium" -> 300 * len
                               else -> 1000 * len
                            }
                            coinWin.text = "+$amount coins"
                            if (!stop){
                                DataBase().addGame(username, "1")
                                updateCoins(username, amount)
                                stop = true
                            }
                        }
                        1 -> {
                            finalResult.text = "You finish second"
                            if (players!!.size>2) {
                                coinWin.text = "${getCoinsSecond(level)} coins"
                                updateCoins(username, getC(level))
                            } else {
                                coinWin.text = "${getLost(level)} coins"
                            }
                            DataBase().addGame(username, "1")
                            if (!stop){
                                if (players.size > 2) DataBase().addGame(username, "3")
                                else DataBase().addGame(username, "2")
                                stop = true
                            }
                        }
                        2 -> {
                            finalResult.text = "You finish third.."
                            coinWin.text = "${getLost(level)} coins"
                            if (!stop){
                                DataBase().addGame(username, "2")
                                stop = true
                            }
                        }
                        3 -> {
                            finalResult.text = "You finish fourth.."
                            coinWin.text = "${getLost(level)} coins"
                            if (!stop){
                                DataBase().addGame(username, "2")
                                stop = true
                            }
                        }
                        4 -> {
                            finalResult.text = "You finish last..."
                            coinWin.text = "${getLost(level)} coins"
                            if (!stop){
                                DataBase().addGame(username, "2")
                                stop = true
                            }
                        }
                    }

                    when (keys.size) {
                        1 -> {
                            val index1 = players?.indexOf(keys.toTypedArray()[0])
                            if (index1 != null){
                                img1.setProfileImage(profiles!![index1.toInt()])
                            }

                        }
                        2 -> {
                            val index1 = players?.indexOf(keys.toTypedArray()[0])
                            val index2 = players?.indexOf(keys.toTypedArray()[1])
                            if (index1 != null && index2 != null){
                                img1.setProfileImage(profiles!![index1.toInt()])
                                img2.setProfileImage(profiles[index2.toInt()])
                            }

                        }
                        3 -> {
                            val index1 = players?.indexOf(keys.toTypedArray()[0])
                            val index2 = players?.indexOf(keys.toTypedArray()[1])
                            val index3 = players?.indexOf(keys.toTypedArray()[2])
                            if (index1 != null && index2 != null && index3 != null){
                                img1.setProfileImage(profiles!![index1.toInt()])
                                img2.setProfileImage(profiles[index2.toInt()])
                                img3.setProfileImage(profiles[index3.toInt()])
                            }

                        }
                        4 -> {
                            val index1 = players?.indexOf(keys.toTypedArray()[0])
                            val index2 = players?.indexOf(keys.toTypedArray()[1])
                            val index3 = players?.indexOf(keys.toTypedArray()[2])
                            val index4 = players?.indexOf(keys.toTypedArray()[2])
                            if (index1 != null && index2 != null && index3 != null && index4 != null){
                                img1.setProfileImage(profiles!![index1.toInt()])
                                img2.setProfileImage(profiles[index2.toInt()])
                                img3.setProfileImage(profiles[index3.toInt()])
                                img4.setProfileImage(profiles[index4.toInt()])
                            }
                        }
                        5 -> {
                            val index1 = players?.indexOf(keys.toTypedArray()[0])
                            val index2 = players?.indexOf(keys.toTypedArray()[1])
                            val index3 = players?.indexOf(keys.toTypedArray()[2])
                            val index4 = players?.indexOf(keys.toTypedArray()[3])
                            val index5 = players?.indexOf(keys.toTypedArray()[4])
                            if (index1 != null && index2 != null && index3 != null && index4 != null && index5 != null){
                                img1.setProfileImage(profiles!![index1.toInt()])
                                img2.setProfileImage(profiles[index2.toInt()])
                                img3.setProfileImage(profiles[index3.toInt()])
                                img4.setProfileImage(profiles[index4.toInt()])
                                img5.setProfileImage(profiles[index5.toInt()])
                            }
                        }
                    }

                    try {
                        username1.text = keys.toTypedArray()[0].replace("_", "")
                        username2.text = keys.toTypedArray()[1].replace("_", "")
                        username3.text = keys.toTypedArray()[2].replace("_", "")
                        username4.text = keys.toTypedArray()[3].replace("_", "")
                        username5.text = keys.toTypedArray()[4].replace("_", "")

                    } catch (_: Exception){}

                    try {
                        score1.text = ((times.toTypedArray()[0])/1000).toString() + " seconds"
                        score2.text = ((times.toTypedArray()[1])/1000).toString() + " seconds"
                        score3.text = ((times.toTypedArray()[2])/1000).toString() + " seconds"
                        score4.text = ((times.toTypedArray()[3])/1000).toString() + " seconds"
                        score5.text = ((times.toTypedArray()[4])/1000).toString() + " seconds"

                    } catch (_: Exception){}
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getResult(data: String): Map<String, Int> {
        val map = data.substring(1, data.length - 1).split(", ").associate {
            val (left, right) = it.split("=")
            left to right.toInt()
        }

        return map.entries.sortedBy { it.value }.associate { it.toPair() }
    }

    override fun onBackPressed() { }

    private fun updateCoins(username: String, amount: Int): Int{
        val db = Firebase.firestore
        val docRef = db.collection("root").document(username)


        docRef.get().addOnSuccessListener { document ->
            val current = document.data!!["0"].toString().toInt()
            DataBase().setValue(username, "0", current + amount)

            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putString("coins", (current + amount).toString())
            editor.apply()
        }
        return amount
    }

    private fun getLost(level: String): String{
        return when (level) {
            "easy" -> "-30"
            "medium" -> "-250"
            else -> "-900"
        }
    }
    private fun getCoinsSecond(level: String): String{
        return when (level) {
            "easy" -> "+25"
            "medium" -> "+150"
            else -> "+400"
        }
    }

    private fun getC(level: String): Int{
        return when (level) {
            "easy" -> 75
            "medium" -> 400
            else -> 1300
        }
    }

    private fun loadAd(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,Helpers().addPhoto, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError.message.log()
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                showAd()
            }
        })
    }

    private fun showAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)

            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                }
            }
        }
    }
}