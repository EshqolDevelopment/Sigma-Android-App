package com.eshqol.sigma

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class VsActivity : AppCompatActivity() {
    private val username = Helpers().getUsername()

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vs)

        val name = intent.getStringExtra("name").toString()
        val level = intent.getStringExtra("level").toString()
        val enemyProfile = intent.getStringExtra("enemyProfile").toString()
        val yourProfile = intent.getStringExtra("yourProfile").toString()
        val bot = intent.getStringExtra("bot").toString()

        val player1Image = findViewById<ImageView>(R.id.imageView17)
        val player2Image = findViewById<ImageView>(R.id.imageView19)
        val player1Name = findViewById<TextView>(R.id.textView28)
        val player2Name = findViewById<TextView>(R.id.textView29)
        val player1Coin = findViewById<TextView>(R.id.textView30)
        val player2Coin = findViewById<TextView>(R.id.textView31)
        val player1Rate = findViewById<TextView>(R.id.textView33)
        val player2Rate = findViewById<TextView>(R.id.textView36)
        val player1Flag = findViewById<ImageView>(R.id.imageView20)
        val player2Flag = findViewById<ImageView>(R.id.imageView22)

        player1Image.setProfileImage(yourProfile)
        player2Image.setProfileImage(enemyProfile)
        player1Name.text = username.replace("_", "")
        player2Name.text  = name.split('@')[1].replace("_", "")


        if (bot == "false") {
            db.collection("root").document(name.split('@')[1]).get().addOnSuccessListener {
                val coinEnemyAmount = it.data?.get("0").toString()
                val flagEnemy = it.data?.get("c").toString()
                player2Flag.setFlag(flagEnemy)

                val winEnemy = it.data?.get("1").toString().toInt()
                val loseEnemy = it.data?.get("2").toString().toInt()
                val drawEnemy = it.data?.get("3").toString().toInt()

                val percentEnemy = (((winEnemy.toFloat())/(winEnemy.toFloat()+loseEnemy.toFloat()+drawEnemy.toFloat()))*100).toInt()
                player2Coin.text = coinEnemyAmount
                player2Rate.text = percentEnemy.toString()

            }
        }
        else{
            val coinEnemyAmount = bot.split('@')[0]
            val flagEnemy = bot.split('@')[2]
            player2Flag.setFlag(flagEnemy)

            val percentEnemy = bot.split('@')[1]
            player2Coin.text = coinEnemyAmount
            player2Rate.text = percentEnemy

            "We could not find online players at this moment so we put you against one of our bots.".snack(this, 4000)
        }


        db.collection("root").document(username).get().addOnSuccessListener {
            val coinAmount = it.data?.get("0").toString()
            val flag = it.data?.get("c").toString()
            player1Flag.setFlag(flag)

            val win = it.data?.get("1").toString().toInt()
            val lose = it.data?.get("2").toString().toInt()
            val draw = it.data?.get("3").toString().toInt()

            val percent = (((win.toFloat())/(win.toFloat()+lose.toFloat()+draw.toFloat()))*100).toInt()
            player1Coin.text = coinAmount
            player1Rate.text = percent.toString()
        }

        Handler(mainLooper).postDelayed({
            if (level == "easy") updateCoins(username, -30)
            if (level == "medium") updateCoins(username, -200)
            if (level == "hard") updateCoins(username, -800)

            if (bot == "false"){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("data", name)
                intent.putExtra("level", level)
                intent.putExtra("enemyProfile", enemyProfile)
                intent.putExtra("num", "1")
                intent.putExtra("result", "0")
                intent.putExtra("wGames", "0")
                intent.putExtra("lGames", "0")
                startActivity(intent)
                overridePendingTransition(R.anim.grow_from_middle , R.anim.shrink_to_middle)
                finish()
            }else{
                val intent = Intent(this, MainActivityBot::class.java)
                intent.putExtra("data", name)
                intent.putExtra("level", level)
                intent.putExtra("enemyProfile", enemyProfile)
                intent.putExtra("num", "1")
                intent.putExtra("result", "0")
                intent.putExtra("wGames", "0")
                intent.putExtra("lGames", "0")
                startActivity(intent)
                overridePendingTransition(R.anim.grow_from_middle , R.anim.shrink_to_middle)
                finish()
            }

        }, 4500)
    }

    private fun updateCoins(username: String, amount: Int) {
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
    }
}