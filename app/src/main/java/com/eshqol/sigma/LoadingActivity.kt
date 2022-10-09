package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.concurrent.thread


class LoadingActivity : AppCompatActivity() {

    private val username = Helpers().getUsername()
    private val database = Firebase.database
    private var close = false
    private var stop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading1)
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val level = intent.getStringExtra("level").toString()
        val profileId = intent.getStringExtra("profile").toString()

        val lastQuestionName = when (level) {
            "easy" -> "lastQuestionsEasy"
            "medium" -> "lastQuestionsMedium"
            else -> "lastQuestionsHard"
        }

        val lastQuestionsArr = sharedPreference.getString(lastQuestionName, "@-1").toString().substring(1).split("@")

        val last20 = if (lastQuestionsArr.size > 20) lastQuestionsArr.takeLast(20) else lastQuestionsArr

        var randGen1 = ""
        var first = true
        for (i in last20){
            randGen1 += if (first) i
            else ",$i"
            first = false
        }

        DataBase().write(randGen1, "Searching for match/$level/$username@$profileId")

        val ref = database.getReference("0/$username@$profileId")

        val cancel = findViewById<Button>(R.id.button2)

        cancel.setOnClickListener {
            stop = true
            val myRef = database.getReference("Searching for match/$level")
            myRef.child("$username@$profileId").removeValue()
            close = true
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!stop){
                    val value = dataSnapshot.getValue<String>().toString()
                    if (close) {
                        stop = true
                        ref.removeEventListener(this)
                    }
                    else if (value != "null" && dataSnapshot.key!!.split("@")[0] == username){
                        stop = true
                        ref.removeEventListener(this)
                        ref.removeValue()

                        val valList = value.split('@')
                        val filterValue = "${valList[0]}@${valList[1]}@${valList[3]}"
                        val enemyProfile = valList[2]
                        val refEnemy = database.getReference("0/${valList[1]}@$enemyProfile")

                        thread {
                            Thread.sleep(10000)
                            refEnemy.get().addOnSuccessListener {
                                if (it.value != null){
                                    val player2 = filterValue.split('@')[1]
                                    DataBase().write("quit@$player2", "playing/$level/${valList[3]}/winner1")
                                }
                            }
                            refEnemy.removeValue()
                        }
                        play(filterValue, level, enemyProfile, profileId, "false")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


        val rangeTime = (9000..12000).random().toLong()

        Handler(mainLooper).postDelayed({
            if (!stop) {
                stop = true

                val ref1 = database.getReference("Searching for match/$level/$username@$profileId")
                ref1.removeValue()

                val listOfNames = arrayOf(
                    "alexa@IL@8@5000",
                    "siri@US@5@12000",
                    "jerome@US@2@12000",
                    "joe@US@9@12000",
                    "oliver@UK@10@10000",
                    "jonjon@DE@5@18000",
                    "noscope25@US@10@8000",
                    "nishikori@JP@14@65000",
                    "harry7@FR@6@25000",
                    "pedro83@br@1@7564",
                    "charlie123@US@7@1875",
                    "karla505@dk@5@3453",
                    "diego%@mx@12@16345",
                    "~ethan~@ca@18@11240",
                    "magnusø@no@14@9854",
                    "thomas62@ch@16@3245",
                    "matthew@au@1@2345",
                    "lyman1@uk@11@18000",
                    "sunita@in@9@9000",
                    "shon@ht@7@23423",
                    "ghough@nl@5@9985",
                    "ching@tw@9@12323",
                    "dilara96451@tr@2@1232",
                    "francesco84@it@2@7770",
                    "-athanasios-@gr@3@6231",
                )

                val cRandom = (-500..500).random()
                val winPercentRandom = (40..60).random()
                val args = listOfNames.random()

                val randomCountry = args.split('@')[1]
                val botName = args.split('@')[0]
                val botId = args.split('@')[2]
                val coinRandom = (args.split('@')[3].toInt() + cRandom).toString()

                val randGen = Helpers().generateRandomGen(this, level, 5)

                play(
                    "$randGen@$botName@00000",
                    level,
                    botId,
                    profileId,
                    "$coinRandom@$winPercentRandom@$randomCountry"
                )
            }
        }, rangeTime)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun play(name: String, level: String, enemyProfile: String, yourProfile: String, bot: String) {
        stop = true
        val intent = Intent(this, VsActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("level", level)
        intent.putExtra("enemyProfile", enemyProfile)
        intent.putExtra("yourProfile", yourProfile)
        intent.putExtra("bot", bot)
        startActivity(intent)
        overridePendingTransition(R.anim.slice, R.anim.slice)
        finish()
    }


    override fun onBackPressed() { }
}
