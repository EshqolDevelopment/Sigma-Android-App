package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView


class ResultAlone : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = intent.getStringExtra("state").toString()
        val timeLap = intent.getStringExtra("timeLap").toString()
        val code = intent.getStringExtra("code").toString()
        val level = intent.getStringExtra("level").toString()
        val num = intent.getStringExtra("num").toString()
        val rand = intent.getStringExtra("random").toString()
        val players = intent.getStringArrayListExtra("players") as ArrayList<String>
        val profiles = intent.getStringArrayListExtra("profiles") as ArrayList<String>

        when (state){
            "right" -> setContentView(R.layout.activity_right)
            else -> setContentView(R.layout.activity_over)
        }

        val text = findViewById<TextView>(R.id.textView4)

        text.text = "You answered correctly you have ${3 - num.toInt()} more question remaining"

        Handler(mainLooper).postDelayed({
            moveToMain(code, level, num, rand, players, timeLap, profiles)
        }, 3050)

    }

    private fun moveToMain(code: String, level: String, num: String, rand: String, players: ArrayList<String>, timeLap : String, profiles: ArrayList<String>){
        val intent = Intent(this, MainActivityAlone::class.java)
        intent.putExtra("code", code)
        intent.putExtra("mode", level)
        intent.putExtra("num", (num.toInt()+1).toString())
        intent.putExtra("random", rand)
        intent.putExtra("players", players)
        intent.putExtra("timeLap", timeLap)
        intent.putExtra("profiles", profiles)
        intent.putExtra("tic", System.currentTimeMillis())
        intent.putExtra("toc", System.nanoTime())

        startActivity(intent)
        finish()
    }


    override fun onBackPressed() { }
}