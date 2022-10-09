package com.eshqol.sigma

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class Result : AppCompatActivity() {

    private val username = Helpers().getUsername()
    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = intent.getStringExtra("state").toString()
        val data = intent.getStringExtra("data").toString()
        val level = intent.getStringExtra("level").toString()
        val num = intent.getStringExtra("num").toString()
        val calcRes = intent.getStringExtra("result").toString()
        val wGames = intent.getStringExtra("wGames").toString()
        val lGames = intent.getStringExtra("lGames").toString()
        val enemyProfile = intent.getStringExtra("enemyProfile").toString()


        if (state == "quit") {
            setContentView(R.layout.activity_win)
            val player1 = findViewById<TextView>(R.id.username1)
            val player2 = findViewById<TextView>(R.id.username2)
            val coin = findViewById<TextView>(R.id.coin_win)
            val result = findViewById<TextView>(R.id.result)
            val enemyImage = findViewById<ImageView>(R.id.profile_enemy)
            val yourProfile = findViewById<ImageView>(R.id.profile_you)
            enemyImage.setProfileImage(enemyProfile)

            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
            val profileId = sharedPreference.getString("profileId", "1").toString()
            yourProfile.setProfileImage(profileId)

            player1.text = username.replace("_", "")
            player2.text = data.split("@")[1].replace("_", "")
            coin.text = getCoins(level, 1)
            result.text = "The other player quit"
            val x = coinsNum(level, 1)

            DataBase().updateCoins(this, x)
            DataBase().addGame(username, "1")

            if ((0..1).random()==0){
                loadAd()
            }
        }

        else if (wGames.toInt() == 3 || lGames.toInt() == 3){

            if (wGames.toInt() == 3 && lGames.toInt() == 3) {
                setContentView(R.layout.activity_tie)
                val player1 = findViewById<TextView>(R.id.username1)
                val player2 = findViewById<TextView>(R.id.username2)
                val coin = findViewById<TextView>(R.id.coin_win)
                val result = findViewById<TextView>(R.id.result)
                val enemyImage = findViewById<ImageView>(R.id.profile_enemy)
                val yourProfile = findViewById<ImageView>(R.id.profile_you)
                enemyImage.setProfileImage(enemyProfile)

                val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                val profileId = sharedPreference.getString("profileId", "1").toString()
                yourProfile.setProfileImage(profileId)

                player1.text = username.replace("_", "")
                player2.text = data.split("@")[1].replace("_", "")
                coin.text = getCoins(level, 2)
                result.text = "($wGames - $lGames)"
                val x = coinsNum(level, 2)

                DataBase().updateCoins(this, x)
                DataBase().addGame(username, "3")

                if ((0..1).random()==0){
                    loadAd()
                }

            }
            else if (wGames.toInt() == 3){
                setContentView(R.layout.activity_win)
                val player1 = findViewById<TextView>(R.id.username1)
                val player2 = findViewById<TextView>(R.id.username2)
                val coin = findViewById<TextView>(R.id.coin_win)
                val result = findViewById<TextView>(R.id.result)
                val enemyImage = findViewById<ImageView>(R.id.profile_enemy)
                val yourProfile = findViewById<ImageView>(R.id.profile_you)
                enemyImage.setProfileImage(enemyProfile)

                val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                val profileId = sharedPreference.getString("profileId", "1").toString()
                yourProfile.setProfileImage(profileId)

                player1.text = username.replace("_", "")
                player2.text = data.split("@")[1].replace("_", "")
                coin.text = getCoins(level, 1)
                result.text = "($wGames - $lGames)"
                val x = coinsNum(level, 1)

                DataBase().updateCoins(this, x)
                DataBase().addGame(username, "1")
                if ((1..10).random() <= 8){
                    loadAd()
                }

            }
            else {
                setContentView(R.layout.activity_lost)
                val player1 = findViewById<TextView>(R.id.username1)
                val player2 = findViewById<TextView>(R.id.username2)
                val coin = findViewById<TextView>(R.id.coin_lose)
                val result = findViewById<TextView>(R.id.result)
                val enemyImage = findViewById<ImageView>(R.id.profile_enemy)
                val yourProfile = findViewById<ImageView>(R.id.profile_you)
                enemyImage.setProfileImage(enemyProfile)

                val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                val profileId = sharedPreference.getString("profileId", "1").toString()
                yourProfile.setProfileImage(profileId)

                player1.text = username.replace("_", "")
                player2.text = data.split("@")[1].replace("_", "")
                coin.text = getCoins(level, 0)
                result.text = "($wGames - $lGames)"
                DataBase().addGame(username, "2")

                if ((0..2).random()==0){
                    loadAd()
                }
            }
        }


        else{
            when (state) {
                "won" -> setContentView(R.layout.activity_result)
                "lost" -> setContentView(R.layout.activity_result0)
                else -> setContentView(R.layout.activity_result1)
            }


            Handler(mainLooper).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("data", data)
                intent.putExtra("level", level)
                intent.putExtra("num", (num.toInt()+1).toString())
                intent.putExtra("result", calcRes)
                intent.putExtra("wGames", wGames)
                intent.putExtra("lGames", lGames)
                intent.putExtra("enemyProfile", enemyProfile)
                startActivity(intent)
                finish()
            }, 3000)
        }
    }

    private fun getCoins(level: String, win: Int): String {
        if(level == "easy" && win==1){
            return "+50"
        }
        else if(level == "easy" && win==0){
            return "-30"
        }
        else if(level == "easy" && win==2){
            return "+10"
        }
        else if(level == "medium" && win==1){
            return "+300"
        }
        else if(level == "medium" && win==0){
            return "-200"
        }
        else if(level == "medium" && win==2){
            return "+50"
        }
        else if(level == "hard" && win==1){
            return "+1000"
        }
        else if(level == "hard" && win==0){
            return "-800"
        }
        else if(level == "hard" && win==2){
            return "+100"
        }
        else{
            return "0"
        }
    }

    private fun coinsNum(level: String, win: Int): Int {
        if(level == "easy" && win==1){
            return 90
        }
        else if(level == "easy" && win==2){
            return 50
        }
        else if(level == "medium" && win==1){
            return 620
        }
        else if(level == "medium" && win==2){
            return 330
        }
        else if(level == "hard" && win==1){
            return 1970
        }
        else if(level == "hard" && win==2){
            return 1070
        }
        else{
            return 0
        }
    }

    fun goHome(view: View) {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {}

    private fun loadAd(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, Helpers().addPhoto, adRequest, object : InterstitialAdLoadCallback() {
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