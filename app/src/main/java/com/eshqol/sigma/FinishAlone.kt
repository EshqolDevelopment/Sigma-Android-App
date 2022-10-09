package com.eshqol.sigma

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.concurrent.thread


class FinishAlone : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private var stopChangeColor = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_alone)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val time = intent.getStringExtra("time")
        val level = intent.getStringExtra("level")
        val coinsAmount = intent.getIntExtra("coins", 0)

        val textTime = findViewById<TextView>(R.id.textView4)
        val newRecord = findViewById<TextView>(R.id.textView37)
        val coinsTextView = findViewById<TextView>(R.id.textView42)

        coinsTextView.text = "+$coinsAmount coins"
        textTime.text = "You completed the challenge in $time seconds!"

        val shareName = when(level) {
            "easy" -> "bestEasyTime1"
            "medium" -> "bestMediumTime1"
            else -> "bestHardTime1"
        }
        val getBestTime = sharedPreference.getFloat(shareName, -1F)

        if (getBestTime == -1F || time.toString().toFloat() < getBestTime){
            val editor = sharedPreference.edit()
            editor.putFloat(shareName, time.toString().toFloat())
            editor.apply()
            thread {
                while (!stopChangeColor){
                    newRecord.setTextColor(Color.parseColor("#ff0000"))
                    Thread.sleep(600)
                    newRecord.setTextColor(Color.parseColor("#00ff00"))
                    Thread.sleep(600)
                    newRecord.setTextColor(Color.parseColor("#00008b"))
                    Thread.sleep(600)
                }
            }
            if (level != null && time != null) {
                DataBase().updateRecord(level, time.toFloat())
            }

        }else{
            newRecord.alpha = 0f
        }

        val back = findViewById<Button>(R.id.goBack)
        back.setOnClickListener {
            stopChangeColor = true
            val switchActivityIntent = Intent(this, HomeScreen::class.java)
            startActivity(switchActivityIntent)
            finish()
        }

        val intTime = time.toString().toFloat()

        val show = when {
            intTime <  20 -> (1..7).random() == 1
            intTime <  50 -> (1..6).random() == 1
            intTime <  150 -> (1..5).random() == 1
            intTime <  300 -> (1..4).random() == 1
            intTime <  600 -> (1..2).random() == 1
            intTime <  1000 -> (1..3).random() != 1
            intTime <  1500 -> (1..4).random() != 1
            else -> (1..50).random() != 1
        }

        if (show)
            loadAd()

    }

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