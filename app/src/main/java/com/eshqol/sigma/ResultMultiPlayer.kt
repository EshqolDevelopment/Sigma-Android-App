package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView


class ResultMultiPlayer : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = intent.getStringExtra("state").toString()
        val timeLap = intent.getStringExtra("timeLap").toString()
        val code = intent.getStringExtra("code").toString()
        val level = intent.getStringExtra("level").toString()
        val num = intent.getStringExtra("num").toString()
        val rand = intent.getStringExtra("random").toString()
        val snackBarAlreadyShown = intent.getStringExtra("snackBarAlreadyShown").toString()
        val players = intent.getStringArrayListExtra("players") as ArrayList<String>
        val profiles = intent.getStringArrayListExtra("profiles") as ArrayList<String>
        val numbersOfQuestions = intent.getStringExtra("numbersOfQuestions").toString()

        when (state){
            "right" -> setContentView(R.layout.activity_right)
            else -> setContentView(R.layout.activity_over)
        }

        val text = findViewById<TextView>(R.id.textView4)

        text.text = "You answered correctly you have ${numbersOfQuestions.toInt() - num.toInt()} more question remaining"


        Handler(mainLooper).postDelayed({
            moveToMain(code, level, num, rand, players, timeLap, profiles, snackBarAlreadyShown, numbersOfQuestions)
        }, 3050)

    }

    private fun moveToMain(code: String, level: String, num: String, rand: String, players: ArrayList<String>, timeLap : String, profiles: ArrayList<String>, snackBarAlreadyShown: String, numbersOfQuestions: String){
        val intent = Intent(this, MainActivityMultiPlayer::class.java)
        intent.putExtra("code", code)
        intent.putExtra("mode", level)
        intent.putExtra("num", (num.toInt()+1).toString())
        intent.putExtra("random", rand)
        intent.putExtra("players", players)
        intent.putExtra("timeLap", timeLap)
        intent.putExtra("profiles", profiles)
        intent.putExtra("tic", System.currentTimeMillis())
        intent.putExtra("toc", System.nanoTime())
        intent.putExtra("snackBarAlreadyShown", snackBarAlreadyShown)
        intent.putExtra("numbersOfQuestions", numbersOfQuestions)

        startActivity(intent)
        finish()
    }


    fun goHome(view: View) {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() { }
}