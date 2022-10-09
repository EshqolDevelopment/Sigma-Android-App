package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class WaitingRoom : AppCompatActivity() {

    private val username = Helpers().getUsername()
    private var mode = ""
    private val database = Firebase.database
    private var randomGen = ""
    private var code = ""
    private var userArray = arrayListOf<String>()
    private var profileArray = arrayListOf<String>()
    private var tempArr = arrayListOf<String>()
    private var join = ""
    private var profileId = ""
    private var stop = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)

        code = intent.getStringExtra("code").toString()
        join = intent.getStringExtra("join").toString()
        profileId = intent.getStringExtra("profileId").toString()

        val player1 = findViewById<TextView>(R.id.player1)
        val player2 = findViewById<TextView>(R.id.player2)
        val player3 = findViewById<TextView>(R.id.player3)
        val player4 = findViewById<TextView>(R.id.player4)
        val player5 = findViewById<TextView>(R.id.player5)

        val player1Image = findViewById<ImageView>(R.id.proImage1)
        val player2Image = findViewById<ImageView>(R.id.proImage2)
        val player3Image = findViewById<ImageView>(R.id.proImage3)
        val player4Image = findViewById<ImageView>(R.id.proImage4)
        val player5Image = findViewById<ImageView>(R.id.proImage5)
        val seekBar = findViewById<SeekBar>(R.id.seekBar3)

        val codeNumbers = findViewById<TextView>(R.id.codeNumbers)
        val startPlaying = findViewById<TextView>(R.id.startPlaying)

        val share1 = findViewById<ImageView>(R.id.imageView29)
        val share2 = findViewById<ImageView>(R.id.imageView30)

        val descQue = findViewById<TextView>(R.id.textView43)
        val qu1 = findViewById<TextView>(R.id.textView44)
        val qu2 = findViewById<TextView>(R.id.textView45)
        val qu3 = findViewById<TextView>(R.id.textView46)
        val qu4 = findViewById<TextView>(R.id.textView47)
        val qu5 = findViewById<TextView>(R.id.textView48)

        if (join == "true"){
            startPlaying.visibility = View.INVISIBLE
            seekBar.visibility = View.INVISIBLE
            qu1.visibility = View.INVISIBLE
            qu2.visibility = View.INVISIBLE
            qu3.visibility = View.INVISIBLE
            qu4.visibility = View.INVISIBLE
            qu5.visibility = View.INVISIBLE
            descQue.visibility = View.INVISIBLE
        }
        share1.setOnClickListener { shareCode() }
        share2.setOnClickListener { shareCode() }

        codeNumbers.text = code

        mode = when {
            code.toInt() <= 400000 -> "easy"
            code.toInt() <= 700000 -> "medium"
            else -> "hard"
        }

        val pathUsernames = "friends/$mode/$code"
        val ref = database.getReference(pathUsernames)

        userArray = arrayListOf()
        profileArray = arrayListOf()
        tempArr = arrayListOf()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (stop){
                    ref.removeEventListener(this)
                }
                else{
                    if (dataSnapshot.value == null){
                        ref.removeEventListener(this)
                        val intent = Intent(this@WaitingRoom, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                        "The host canceled the game".show(this@WaitingRoom)
                        return
                    }
                    for (postSnapshot in dataSnapshot.children) {
                        if (postSnapshot.key == "random")
                            randomGen = postSnapshot.value.toString()
                        else if (postSnapshot.key.toString() == "begin"){

                            val numbersOfQuestions = postSnapshot.value.toString().split("@")[1]
                            if (randomGen != ""){
                                beginPlaying(numbersOfQuestions)
                                ref.removeEventListener(this)
                                return
                            }
                        }
                        else if (postSnapshot.key.toString() !in tempArr && postSnapshot.key.toString()!="winner1" && postSnapshot.key.toString()!="winner2" && postSnapshot.key.toString()!="winner3" && postSnapshot.key.toString()!="winner1f" && postSnapshot.key.toString()!="winner2f" && postSnapshot.key.toString()!="winner3f" && postSnapshot.key.toString()!="full") {
                            userArray.add(postSnapshot.key.toString().split('@')[0])
                            profileArray.add(postSnapshot.key.toString().split('@')[1])
                            tempArr.add(postSnapshot.key.toString())
                        }
                    }
                    for (i in 0 until userArray.size){
                        when (i) {
                            0 -> {player1.text = userArray[i].replace("_", ""); player1Image.setProfileImage(profileArray[i])}
                            1 -> {player2.text = userArray[i].replace("_", ""); player2Image.setProfileImage(profileArray[i])}
                            2 -> {player3.text = userArray[i].replace("_", ""); player3Image.setProfileImage(profileArray[i])}
                            3 -> {player4.text = userArray[i].replace("_", ""); player4Image.setProfileImage(profileArray[i])}
                            4 -> {
                                player5.text = userArray[i].replace("_", "")
                                player5Image.setProfileImage(profileArray[i])
                                DataBase().write("", "friends/$mode/$code/full")
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        DataBase().write("", "friends/$mode/${code}/$username@$profileId")


        startPlaying.setOnClickListener{
            if (userArray.size>1){
                DataBase().write("${userArray.size}@${seekBar.progress + 1}", "friends/$mode/$code/begin")
                DataBase().write((System.currentTimeMillis()/1000000).toString(),"friends/$mode/$code/startTime")
            }else{
                "There isn't enough players to begin the game.".snack(this)
            }

        }
    }
    fun beginPlaying(numbersOfQuestions: String) {
        val intent = Intent(this, MainActivityMultiPlayer::class.java)

        when (mode) {
            "easy" -> updateCoins(username, -30)
            "medium" -> updateCoins(username, -250)
            else -> updateCoins(username, -900)
        }

        intent.putExtra("numbersOfQuestions", numbersOfQuestions)
        intent.putExtra("code", code)
        intent.putExtra("mode", mode)
        intent.putExtra("players", userArray)
        intent.putExtra("profiles", profileArray)
        intent.putExtra("num", "1")
        intent.putExtra("result", "0")
        intent.putExtra("timeLap", "0")
        intent.putExtra("random", randomGen)
        intent.putExtra("tic", System.currentTimeMillis())
        intent.putExtra("toc", System.nanoTime())
        intent.putExtra("snackBarAlreadyShown", "")
        stop = true

        startActivity(intent)
        finish()
    }

    fun exitWaiting(view: View) {
        stop = true

        if (join == "true"){
            val myRe = database.getReference("friends/$mode/$code")
            myRe.child("$username@$profileId").removeValue()
        }
        else{
            val myRe = database.getReference("friends/$mode/$code")
            myRe.removeValue()
            "The host canceled the game".show(this@WaitingRoom)
        }

        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() { }


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


    private fun shareCode(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nI challenge you to sigma coding competition, just press on the link or enter the invitation code - $code.\n\n"
            shareMessage =
                """
                ${shareMessage}https://www.eshqol.com/sigma/$code
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))

        } catch (_: Exception) {}
    }


}