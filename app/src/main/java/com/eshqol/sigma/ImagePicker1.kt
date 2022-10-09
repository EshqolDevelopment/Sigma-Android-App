package com.eshqol.sigma

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ImagePicker1 : AppCompatActivity() {
    private val username = Helpers().getUsername()
    private val db = Firebase.firestore
    private var pingProfileId = ""
    private var wins = ""
    private var coins = ""
    private var p16 = ""
    private var p17 = ""
    private var p18 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        val img1 = findViewById<ImageView>(R.id.profile1)
        val img2 = findViewById<ImageView>(R.id.profile2)
        val img3 = findViewById<ImageView>(R.id.profile3)
        val img4 = findViewById<ImageView>(R.id.profile4)
        val img5 = findViewById<ImageView>(R.id.profile5)
        val img6 = findViewById<ImageView>(R.id.profile6)
        val img7 = findViewById<ImageView>(R.id.profile7)
        val img8 = findViewById<ImageView>(R.id.profile8)
        val img9 = findViewById<ImageView>(R.id.profile9)
        val img10 = findViewById<ImageView>(R.id.profile10)
        val img11 = findViewById<ImageView>(R.id.profile11)
        val img12 = findViewById<ImageView>(R.id.profile12)
        val img13 = findViewById<ImageView>(R.id.profile13)
        val img14 = findViewById<ImageView>(R.id.profile14)
        val img15 = findViewById<ImageView>(R.id.profile15)
        val img16 = findViewById<ImageView>(R.id.profile16)
        val img17 = findViewById<ImageView>(R.id.profile17)
        val img18 = findViewById<ImageView>(R.id.profile18)
        val lock13 = findViewById<ImageView>(R.id.lock13)
        val lock14 = findViewById<ImageView>(R.id.lock14)
        val lock15 = findViewById<ImageView>(R.id.lock15)
        val lock16 = findViewById<ImageView>(R.id.lock16)
        val lock17 = findViewById<ImageView>(R.id.lock17)
        val lock18 = findViewById<ImageView>(R.id.lock18)

        val backButton = findViewById<Button>(R.id.back)

        val pingUsername = intent.getStringExtra("username")
        pingProfileId = intent.getStringExtra("id").toString()
        coins = intent.getStringExtra("coins").toString()
        wins = intent.getStringExtra("wins").toString()

        if (wins.toInt() >= 20) lock14.alpha = 0f
        if (wins.toInt() >= 50) lock13.alpha = 0f
        if (wins.toInt() >= 200) lock15.alpha = 0f

        val docRef1 = db.collection("root").document(username)
        docRef1.get(Source.CACHE).addOnSuccessListener { document ->
            p16 = document.data!!["p16"].toString()
            p17 = document.data!!["p17"].toString()
            p18 = document.data!!["p18"].toString()
            if (p16 == "1"){
                lock16.alpha = 0f
            }
            if (p17 == "1"){
                lock17.alpha = 0f
            }
            if (p18 == "1"){
                lock18.alpha = 0f
            }
        }

        val docRef = db.collection("root").document(username)
        docRef.get().addOnSuccessListener { document ->
            p16 = document.data!!["p16"].toString()
            p17 = document.data!!["p17"].toString()
            p18 = document.data!!["p18"].toString()
            if (p16 == "1"){
                lock16.alpha = 0f
            }
            if (p17 == "1"){
                lock17.alpha = 0f
            }
            if (p18 == "1"){
                lock18.alpha = 0f
            }
        }


        backButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("username", pingUsername)
            intent.putExtra("id", pingProfileId)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        }

        img1.setOnClickListener{ setProfile(1) }
        img2.setOnClickListener{ setProfile(2) }
        img3.setOnClickListener{ setProfile(3) }
        img4.setOnClickListener{ setProfile(4) }
        img5.setOnClickListener{ setProfile(5) }
        img6.setOnClickListener{ setProfile(6) }
        img7.setOnClickListener{ setProfile(7) }
        img8.setOnClickListener{ setProfile(8) }
        img9.setOnClickListener{ setProfile(9) }
        img10.setOnClickListener{ setProfile(10) }
        img11.setOnClickListener{ setProfile(11) }
        img12.setOnClickListener{ setProfile(12) }
        img13.setOnClickListener{ buy(13, it, lock14) }
        img14.setOnClickListener{ buy(14, it, lock13) }
        img15.setOnClickListener{ buy(15, it, lock15) }
        img16.setOnClickListener{ buy(16, it, lock16) }
        img17.setOnClickListener{ buy(17, it, lock17) }
        img18.setOnClickListener{ buy(18, it, lock18) }
    }

    private fun setProfile(imageId: Int){
        val username = Helpers().getUsername()
        DataBase().updateProfile(username, imageId)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("profileId", imageId.toString())
        editor.apply()

        val intent = Intent(this, Profile::class.java)
        intent.putExtra("username", username)
        intent.putExtra("id",  imageId.toString())
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }

    override fun onBackPressed() {
        val intent = Intent(this, Profile::class.java)
        intent.putExtra("username", username)
        intent.putExtra("id", pingProfileId)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }


    private fun updateCoins(username: String, amount: Int, current: Int) {
        DataBase().setValue(username, "0", current + amount)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("coins", (current + amount).toString())
        editor.apply()

    }


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "InflateParams")
    private fun buy(n: Int, it: View, lock: ImageView){
        if (n == 13 && lock.alpha == 0f){
            setProfile(13)
        }
        else if (n == 14 && lock.alpha == 0f){
            setProfile(14)
        }
        else if (n == 15 && lock.alpha == 0f){
            setProfile(15)
        }
        else if (n == 16 && lock.alpha == 0f){
            setProfile(16)
        }
        else if (n == 17 && lock.alpha == 0f){
            setProfile(17)
        }
        else if (n == 18 && lock.alpha == 0f){
            setProfile(18)
        }

        else{
            try {
                val metrics: DisplayMetrics = this.resources.displayMetrics
                val width = metrics.widthPixels
                val height = metrics.heightPixels

                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.pop_up_buy_profile, null)
                val popupWindow = PopupWindow(popupView, width, height, true)
                popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)

                popupWindow.isOutsideTouchable = true
                popupWindow.isTouchable = true
                popupWindow.setTouchInterceptor(View.OnTouchListener { view1, motionEvent ->
                    if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                    motionEvent.y < 0 || motionEvent.y > view1.height
                })

                val close = popupView.findViewById<View>(R.id.button11) as Button
                close.setOnClickListener {
                    popupWindow.dismiss()
                }
                val yes = popupView.findViewById<View>(R.id.button12) as Button
                val priceDetails = popupView.findViewById<View>(R.id.textView27) as TextView
                val textV = popupView.findViewById<View>(R.id.textView25) as TextView
                val photo = popupView.findViewById<View>(R.id.imageView23) as ImageView

                when(n){
                    13 -> {
                        photo.setImageResource(R.drawable.p13)
                        textV.alpha = 0f
                        priceDetails.text = "You have to win 20 times to unlock this profile picture"
                        yes.text = "OK"
                        close.isEnabled = false
                        close.alpha = 0f
                        yes.setOnClickListener {
                            popupWindow.dismiss()
                        }
                    }
                    14 -> {
                        photo.setImageResource(R.drawable.p14)
                        textV.alpha = 0f
                        priceDetails.text = "You have to win 50 times to unlock this profile picture"
                        yes.text = "OK"
                        close.isEnabled = false
                        close.alpha = 0f
                        yes.setOnClickListener {
                            popupWindow.dismiss()
                        }
                    }
                    15 -> {
                        photo.setImageResource(R.drawable.p15)
                        textV.alpha = 0f
                        priceDetails.text = "You have to win 200 times to unlock this profile picture"
                        yes.text = "OK"
                        close.isEnabled = false
                        close.alpha = 0f
                        yes.setOnClickListener {
                            popupWindow.dismiss()
                        }
                    }
                    16 -> {
                        photo.setImageResource(R.drawable.p16)
                        priceDetails.text = "The price is 100000 coins"
                        yes.setOnClickListener {
                            val docRef = db.collection("root").document(username)
                            docRef.get().addOnSuccessListener { document ->
                                val current = document.data!!["0"].toString().toInt()
                                if (current -100000 < 0){
                                    "You don't have enough coins to buy this profile picture".show(this)
                                }
                                else{
                                    updateCoins(username, -100000, current)
                                    DataBase().setString(username, "p16", "1")
                                    popupWindow.dismiss()
                                    lock.alpha = 0f
                                }
                            }
                        }
                    }
                    17 -> {
                        photo.setImageResource(R.drawable.p17)
                        priceDetails.text = "The price is 30000 coins"
                        yes.setOnClickListener {
                            val docRef = db.collection("root").document(username)
                            docRef.get().addOnSuccessListener { document ->
                                val current = document.data!!["0"].toString().toInt()
                                if (current - 30000 < 0){
                                    "You don't have enough coins to buy this profile picture".show(this)
                                }
                                else{
                                    updateCoins(username, -30000, current)
                                    DataBase().setString(username, "p17", "1")
                                    popupWindow.dismiss()
                                    lock.alpha = 0f
                                }
                            }
                        }
                    }
                    18 -> {
                        photo.setImageResource(R.drawable.p18)
                        priceDetails.text = "The price is 10000 coins"
                        yes.setOnClickListener {
                            val docRef = db.collection("root").document(username)
                            docRef.get().addOnSuccessListener { document ->
                                val current = document.data!!["0"].toString().toInt()
                                if (current -10000 < 0){
                                    "You don't have enough coins to buy this profile picture".show(this)
                                }
                                else{
                                    updateCoins(username, -10000, current)
                                    DataBase().setString(username, "p18", "1")
                                    popupWindow.dismiss()
                                    lock.alpha = 0f
                                }

                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

    }
}