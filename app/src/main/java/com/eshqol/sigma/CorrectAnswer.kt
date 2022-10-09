package com.eshqol.sigma

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class CorrectAnswer : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.correct_activity)
        val time = intent.getStringExtra("time")
        val textTime = findViewById<TextView>(R.id.times111)
        textTime.text = "You answered in $time seconds!"

        val back = findViewById<Button>(R.id.goBack)
        back.setOnClickListener {
            val switchActivityIntent = Intent(this, Practice::class.java)
            startActivity(switchActivityIntent)
            finish()
        }

        val intTime = time.toString().toInt()

        val show = when {
            intTime <  20 -> (1..7).random() == 1
            intTime <  50 -> (1..6).random() == 1
            intTime <  150 -> (1..5).random() == 1
            intTime <  300 -> (1..4).random() == 1
            intTime <  600 -> (1..3).random() == 1
            intTime <  1000 -> (1..2).random() == 1
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