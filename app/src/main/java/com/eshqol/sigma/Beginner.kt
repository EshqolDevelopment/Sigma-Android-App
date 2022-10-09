package com.eshqol.sigma

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class Beginner: AppCompatActivity() {

    private var answer = 0
    private var functionNameNext = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_begginer)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val questionListBeginner = sharedPreference.getString("questionListBeginnerString", "0")?.split("@")?.toTypedArray()!!

        val functionName = intent.getStringExtra("functionName").toString()

        var x = ""
        var find = false

        for (item in questionListBeginner){
            if (functionName == item.split('&')[0].lowercase()){
                x = item
                find = true
                continue
            }
            if (find) {
                functionNameNext = item.split('&')[0].lowercase()
                break
            }
        }

        val desc = findViewById<TextView>(R.id.textView38)
        val button0 = findViewById<Button>(R.id.button4)
        val button1 = findViewById<Button>(R.id.button5)
        val button2 = findViewById<Button>(R.id.button6)
        val button3 = findViewById<Button>(R.id.button9)

        val data = x.split("&")
        desc.text = data[1]

        val answer1 = data[6]
        val random = data.subList(2, 6).shuffled()

        answer = random.indexOf(data[answer1.toInt()+2])

        button0.text = random[0]
        button1.text = random[1]
        button2.text = random[2]
        button3.text = random[3]


        when (answer.toString()) {
            "0" -> {
                button0.setOnClickListener { correct() }
                button1.setOnClickListener { incorrect(1) }
                button2.setOnClickListener { incorrect(2) }
                button3.setOnClickListener { incorrect(3) }
            }
            "1" -> {
                button0.setOnClickListener { incorrect(0) }
                button1.setOnClickListener { correct() }
                button2.setOnClickListener { incorrect(2) }
                button3.setOnClickListener { incorrect(3) }
            }
            "2" -> {
                button0.setOnClickListener { incorrect(0) }
                button1.setOnClickListener { incorrect(1) }
                button2.setOnClickListener { correct() }
                button3.setOnClickListener { incorrect(3) }
            }
            "3" -> {
                button0.setOnClickListener { incorrect(0) }
                button1.setOnClickListener { incorrect(1) }
                button2.setOnClickListener { incorrect(2) }
                button3.setOnClickListener { correct() }
            }
        }

        val close = findViewById<ImageView>(R.id.imageView6)
        close.setOnClickListener{
            val intent = Intent(this, Practice::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        }
    }

    private fun incorrect(press: Int) {
        val button0 = findViewById<Button>(R.id.button4)
        val button1 = findViewById<Button>(R.id.button5)
        val button2 = findViewById<Button>(R.id.button6)
        val button3 = findViewById<Button>(R.id.button9)

        val buttonWrong: Button = when(press){
            0 -> button0
            1-> button1
            2-> button2
            else -> button3
        }
        buttonWrong.setBackgroundColor(Color.RED)
    }

    private fun correct() {
        val parentLayout = findViewById<View>(android.R.id.content)

        val button: Button = when(answer){
            0 -> findViewById(R.id.button4)
            1-> findViewById(R.id.button5)
            2-> findViewById(R.id.button6)
            else -> findViewById(R.id.button9)
        }

        button.setBackgroundColor(Color.GREEN)
        if (functionNameNext != ""){
            val snack = Snackbar
                .make(parentLayout, "You answer correctly. Would you like to try out the next question?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes") {
                    val intent = Intent(this, Beginner::class.java)
                    intent.putExtra("functionName", functionNameNext)
                    startActivity(intent)
                    finish()
                }
            snack.show()
        }
        else{
            val snack = Snackbar
                .make(parentLayout, "You've reached the end. Would you like to try out a non trivia question?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes") {
                    val intent = Intent(this, MainActivityPractice::class.java)
                    intent.putExtra("tic", System.currentTimeMillis())
                    intent.putExtra("functionName", "bigger_than_5")
                    intent.putExtra("multiArguments", "false")
                    startActivity(intent)
                    finish()
                }
            snack.show()
        }
    }

    private var backPressOnce = false
    private fun exit(){
        if (!backPressOnce) {
            "Click the back button again to exit".show(this)
            backPressOnce = true
        }
        else{
            val intent = Intent(this, Practice::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        }
    }

    override fun onBackPressed() {
        exit()
    }


}