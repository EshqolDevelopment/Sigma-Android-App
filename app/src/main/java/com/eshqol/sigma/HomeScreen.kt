package com.eshqol.sigma

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeScreen : AppCompatActivity() {
    private val database = Firebase.database
    private var level = ""
    private val username = Helpers().getUsername()
    private val db = Firebase.firestore
    private var txtCode = ""
    private var profileId = ""
    private var coinsNumber = ""
    private var animRun = false
    private var mRewardedAd: RewardedAd? = null
    private val tagAdMob = "sigma"
    private val adRequest = AdRequest.Builder().build()
    private var last = 0L
    private var last1 = 0L
    private var last2 = 0L
    private var popWidth = 0
    private var popHeight = 0
    private var earnCoinsFromVideo = true
    private var firstAdd = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val profileImage = findViewById<ImageView>(R.id.profile_enemy1)
        val usernameLabel = findViewById<TextView>(R.id.userName)
        val pythonImage = findViewById<ImageView>(R.id.imageView)
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        var linkCode = intent.getStringExtra("linkCode")
        val snackNewQuestion = intent.getStringExtra("snack")
        val parentLayout = findViewById<View>(android.R.id.content)

        val easyTimes = sharedPreference.getString("EasyTimes", "1")
        if (easyTimes == "1"){
            val editor123 = sharedPreference.edit()
            editor123.putString("EasyTimes", "80")
            editor123.apply()
        }
        val mediumTimes = sharedPreference.getString("MediumTimes", "1")
        if (mediumTimes == "1"){
            val editor123 = sharedPreference.edit()
            editor123.putString("MediumTimes", "80")
            editor123.apply()
        }
        val hardTimes = sharedPreference.getString("HardTimes", "1")
        if (hardTimes == "1"){
            val editor123 = sharedPreference.edit()
            editor123.putString("HardTimes", "80")
            editor123.apply()
        }

        val metrics: DisplayMetrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        popWidth = (width.toFloat()*(1000f/1080f)).toInt()
        popHeight = (height.toFloat()*(800f/2120f)).toInt()

        if (snackNewQuestion != null){
            Handler(mainLooper).postDelayed({
                Snackbar.make(parentLayout, snackNewQuestion, 4000).show()
            }, 500)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home

        MobileAds.initialize(this) {
            it.toString().log()
        }

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.practice -> {
                    val switchActivityIntent = Intent(this, Practice::class.java)
                    startActivity(switchActivityIntent)
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                    finish()
                }
                R.id.leaderboard -> {
                    val switchActivityIntent = Intent(this, Leaderboard::class.java)
                    startActivity(switchActivityIntent)
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                    finish()
                }
            }
            true
        }

        pythonImage.setOnClickListener {
            if (!animRun){
                animRun = true
                pythonImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_click))
                Handler(Looper.getMainLooper()).postDelayed({
                    pythonImage.setImageResource(R.drawable.zen)
                }, 1500)
                Handler(Looper.getMainLooper()).postDelayed({
                    pythonImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_hide))
                }, 4000)
                Handler(Looper.getMainLooper()).postDelayed({
                    pythonImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_show))
                    pythonImage.setImageResource(R.drawable.python)
                    animRun = false
                }, 4800)
            }
        }

        db.collection("questions").document("version").get().addOnSuccessListener {
            val version = it.data?.get("python")

            val currentVersion = sharedPreference.getString("version", "-1")
            if (currentVersion?.toInt()!! < version.toString().toInt()){
                updateQuestions(version.toString())
            }
        }

        val coinsAmount = findViewById<TextView>(R.id.coins_amount)
        coinsNumber = sharedPreference.getString("coins", "").toString()
        coinsAmount.text = coinsNumber
        usernameLabel.text = username.replace("_", "")

        profileId = sharedPreference.getString("profileId", "").toString()
        profileImage.setProfileImage(profileId)
        if (profileId == ""){
            val docRef1 = db.collection("root").document(username)
            docRef1.get().addOnSuccessListener { document ->
                if (document.data?.get("p") != null) {

                    val profileIdFromBase = document.data!!["p"].toString()
                    profileId = profileIdFromBase
                    profileImage.setProfileImage(profileIdFromBase)
                    val editor1 = sharedPreference.edit()
                    editor1.putString("profileId", profileIdFromBase)
                    editor1.apply()
                }
            }
        }
        val ref = database.getReference("0/$username@$profileId")
        ref.removeValue()

        if (linkCode.toString().length == 6){
            try {
                linkCode = linkCode.toString()

                val mode = when {
                    linkCode.toInt() <= 400000 -> "easy"
                    linkCode.toInt() <= 700000 -> "medium"
                    else -> "hard"
                }

                if (mode == "easy" && coinsNumber.toInt() < 50 || mode == "medium" && coinsNumber.toInt() < 300 || mode == "hard" && coinsNumber.toInt() < 1000){
                    Snackbar.make(parentLayout, "Your don't have enough coins to participant in game.\nconsider watching a short video to earn some coins.", 6000).show()
                }
                else{
                    val database = Firebase.database
                    val ref11 = database.getReference("friends/$mode/${linkCode}")

                    var exist = false
                    var begin = false
                    var full = false

                    ref11.get().addOnSuccessListener {
                        if (it.value.toString() != "null"){
                            exist = true
                            val values = it.value.toString()
                            if ("begin" in values) begin = true
                            if ("full" in values) full = true
                        }

                        if (exist && !begin && !full){
                            DataBase().write("", "friends/$mode/${linkCode}/$username@$profileId")
                            waitingRoomJoinFromCode(linkCode)
                        }
                        else if (begin) {
                            Snackbar.make(parentLayout, "Your friends has already start playing", Snackbar.LENGTH_SHORT).show()
                        }
                        else if (full) {
                            Snackbar.make(parentLayout, "The group is full", Snackbar.LENGTH_SHORT).show()
                        }
                        else {
                            "This code not exist".show(this)
                        }
                    }
                }


            } catch (e: Exception){
                e.toString().show(this)
            }
        }

        val quickPlay = findViewById<Button>(R.id.quick_play)
        val playWithFriends = findViewById<Button>(R.id.button3)
        quickPlay.width = playWithFriends.width


        db.collection("root").document(username).get().addOnSuccessListener { document ->
            if (document.data?.get("0") != null) {
                val coinsFromBase = document.data!!["0"].toString()
                coinsAmount.text = coinsFromBase
                val editor1 = sharedPreference.edit()
                editor1.putString("coins", coinsFromBase)
                editor1.apply()
            }

            val coin = coinsAmount.text.toString().toInt()
            val win = document.data?.get("1").toString().toInt()
            val lose = document.data?.get("2").toString().toInt()
            val draw = document.data?.get("3").toString().toInt()
            val a = document.data?.get("a").toString().toInt()


            if ((win+lose+draw) != 0) {
                val score = (coin + win * 300 + draw * 100)/2
                if (score != a) {
                    DataBase().setValue(username, "a", score)
                }
            }
        }


        val coins = findViewById<ImageView>(R.id.imageView2)
        coins.setOnClickListener { notEnoughCoinsPopUp(it, true) }
        coinsAmount.setOnClickListener{ notEnoughCoinsPopUp(it, true) }

        RewardedAd.load(this,Helpers().addVideo, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(tagAdMob, adError.message)
                mRewardedAd = null
            }
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(tagAdMob, "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })

        val sharedPref = getSharedPreferences("CheckBoxList", Context.MODE_PRIVATE)
        if (sharedPref.getString("CheckBoxList", "") == ""){
            db.collection("practice").document(username).get().addOnSuccessListener {
                val q = it["q"]
                if (q != null ){
                    val myEdit = sharedPref.edit()
                    myEdit.putString("CheckBoxList", q.toString())
                    myEdit.apply()
                }
            }
        }


        val bestEasyTime = sharedPreference.getFloat("bestEasyTime1", -2F)
        val bestMediumTime = sharedPreference.getFloat("bestMediumTime1", -2F)
        val bestHardTime = sharedPreference.getFloat("bestHardTime1", -2F)

        if (bestEasyTime == -2F || bestMediumTime == -2F || bestHardTime == -2F){
            val editor1 = sharedPreference.edit()

            db.collection("root").document(username).get().addOnSuccessListener {

                val easyRecord = it.data?.get("easy_record")
                val mediumRecord = it.data?.get("medium_record")
                val hardRecord = it?.data?.get("hard_record")

                if (easyRecord != null)  editor1.putFloat("bestEasyTime1", easyRecord.toString().toFloat())
                else editor1.putFloat("bestEasyTime1", -1F)

                if (mediumRecord != null) editor1.putFloat("bestMediumTime1", mediumRecord.toString().toFloat())
                else editor1.putFloat("bestMediumTime1", -1F)

                if (hardRecord != null) editor1.putFloat("bestHardTime1", hardRecord.toString().toFloat())
                else editor1.putFloat("bestHardTime1", -1F)

                editor1.apply()
            }
        }


    }


    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    fun searchPlayers(view: View) {
        if (System.nanoTime()-last < 1000000000L) {
            return
        }
        last = System.nanoTime()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.pop_up_choose_kind, null)
        val popupWindow = PopupWindow(popupView, popWidth, popHeight, true)
        popupWindow.animationStyle = R.style.Animation

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
            motionEvent.y < 0 || motionEvent.y > view1.height
        })

        val close = popupView.findViewById<View>(R.id.imageView9) as ImageView
        val online = popupView.findViewById<View>(R.id.imageView3) as ImageView
        val alone = popupView.findViewById<View>(R.id.imageView4) as ImageView

        close.setOnClickListener {
            popupWindow.dismiss()
        }
        online.setOnClickListener {
            popupWindow.dismiss()
            startGame("online")
        }
        alone.setOnClickListener {
            popupWindow.dismiss()
            startGame("alone")
        }


    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams", "SetTextI18n")
    fun startGame(kind: String){
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.pop_up_window, null)
        val popupWindow = PopupWindow(popupView, popWidth, popHeight, true)
        popupWindow.animationStyle = R.style.Animation
        val view = findViewById<View>(android.R.id.content)

        try{
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            popupWindow.isOutsideTouchable = true
            popupWindow.isTouchable = true
            popupWindow.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                motionEvent.y < 0 || motionEvent.y > view1.height
            })

            val close = popupView.findViewById<ImageView>(R.id.imageView9)
            val easyS = popupView.findViewById<Button>(R.id.easyS)
            val mediumS = popupView.findViewById<Button>(R.id.mediumS)
            val hardS = popupView.findViewById<Button>(R.id.hardS)

            if (kind != "online") {
                val easyCoins = popupView.findViewById<TextView>(R.id.textView2)
                val mediumCoins = popupView.findViewById<TextView>(R.id.textView9)
                val hardCoins = popupView.findViewById<TextView>(R.id.textView10)

                easyCoins.text = "30 coins"
                mediumCoins.text = "150 coins"
                hardCoins.text = "400 coins"
            }

            close.setOnClickListener {
                popupWindow.dismiss()
            }
            easyS.setOnClickListener {
                if (kind == "online"){
                    val docRef1 = db.collection("root").document(username)
                    docRef1.get().addOnSuccessListener { document ->
                        if (document != null) {
                            if (document.data!!["0"].toString().toInt() < 50){
                                popupWindow.dismiss()
                                notEnoughCoinsPopUp(it)
                            } else {
                                level = "easy"
                                matchMaking()
                            }
                        }
                    }
                }
                else{
                    playAlone("easy")
                }
            }

            mediumS.setOnClickListener {
                if (kind == "online"){
                    val docRef1 = db.collection("root").document(username)
                    docRef1.get().addOnSuccessListener { document ->
                        if (document != null) {
                            if (document.data!!["0"].toString().toInt() < 300) {
                                popupWindow.dismiss()
                                notEnoughCoinsPopUp(it)
                            } else {
                                level = "medium"
                                matchMaking()
                            }
                        }
                    }
                } else{
                    playAlone("medium")
                }

            }
            hardS.setOnClickListener {
                if (kind == "online"){
                    val docRef1 = db.collection("root").document(username)
                    docRef1.get().addOnSuccessListener { document ->
                        if (document != null) {
                            if (document.data!!["0"].toString().toInt() < 1000) {
                                popupWindow.dismiss()
                                notEnoughCoinsPopUp(it)

                            } else {
                                level = "hard"
                                matchMaking()
                            }
                        }
                    }
                }
                else {
                    playAlone("hard")
                }
            }

        } catch (_: Exception){}

    }

    private fun playAlone(mode: String){
        val randomGen = Helpers().generateRandomGen(this, mode, 3)

        val intent = Intent(this, MainActivityAlone::class.java)

        intent.putExtra("code", "000000")
        intent.putExtra("mode", mode)
        intent.putExtra("players", arrayListOf(username))
        intent.putExtra("profiles", arrayListOf(profileId))
        intent.putExtra("num", "1")
        intent.putExtra("result", "0")
        intent.putExtra("timeLap", "0")
        intent.putExtra("random", randomGen)
        intent.putExtra("tic", System.currentTimeMillis())
        intent.putExtra("toc", System.nanoTime())

        startActivity(intent)
        overridePendingTransition(R.anim.grow_from_middle , R.anim.shrink_to_middle)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun matchMaking(){
        val intent = Intent(this, LoadingActivity::class.java)
        intent.putExtra("level", level)
        intent.putExtra("profile", profileId)
        startActivity(intent)
        finish()
    }

    fun goProfile(view: View) {
        val intent = Intent(this, Profile::class.java)
        intent.putExtra("username", username)
        intent.putExtra("id", profileId)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    fun inviteFriends(view: View) {
        if (System.nanoTime()-last1<1000000000L) {
            return
        }
        last1 = System.nanoTime()

        try{
            val coinsAmount = findViewById<TextView>(R.id.coins_amount)
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView2 = inflater.inflate(R.layout.pop_up_window_join, null)
            val popupWindow2 = PopupWindow(popupView2, popWidth, popHeight, true)
            popupWindow2.animationStyle = R.style.Animation1
            popupWindow2.showAtLocation(view, Gravity.CENTER, 0, 0)

            popupWindow2.isOutsideTouchable = true
            popupWindow2.isTouchable = true
            popupWindow2.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                motionEvent.y < 0 || motionEvent.y > view1.height
            })
            val dis = popupView2.findViewById<ImageView>(R.id.imageView99)
            dis.setOnClickListener{
                popupWindow2.dismiss()
            }

            val invite = popupView2.findViewById<View>(R.id.invite)

            invite.setOnClickListener{
                popupWindow2.dismiss()

                val popupView = inflater.inflate(R.layout.pop_up_window_invite, null)
                val popupWindow = PopupWindow(popupView, popWidth, popHeight, true)
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
                popupWindow.isOutsideTouchable = true
                popupWindow.isTouchable = true

                popupWindow.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                    if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                    motionEvent.y < 0 || motionEvent.y > view1.height
                })
                val close = popupView.findViewById<View>(R.id.imageView9) as ImageView
                close.setOnClickListener { popupWindow.dismiss() }

                val easy = popupView.findViewById<Button>(R.id.easy)
                easy.setOnClickListener {
                    popupWindow.dismiss()

                    if (coinsAmount.text.toString().toInt()<50){
                        notEnoughCoinsPopUp(it)
                    } else {
                        val popupView1 = inflater.inflate(R.layout.activity_invite, null)
                        val popupWindow1 = PopupWindow(popupView1, popWidth, popHeight, true)
                        popupWindow1.animationStyle = R.style.Animation2
                        popupWindow1.showAtLocation(view, Gravity.CENTER, 0, 0)
                        popupWindow1.isOutsideTouchable = true
                        popupWindow1.isTouchable = true
                        popupWindow1.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                            motionEvent.y < 0 || motionEvent.y > view1.height
                        })

                        val close1 = popupView1.findViewById<View>(R.id.imageView9) as ImageView
                        close1.setOnClickListener { popupWindow1.dismiss() }

                        val code = popupView1.findViewById<TextView>(R.id.textView14)
                        code.text = (100000..400000).random().toString()
                        txtCode = code.text.toString()

                        val randomGen = Helpers().generateRandomGen(this, "easy", 5)

                        DataBase().write("", "friends/easy/${code.text}/$username@$profileId")
                        DataBase().write(randomGen, "friends/easy/${code.text}/random")

                        close1.setOnClickListener {
                            popupWindow1.dismiss()
                            val myRe = database.getReference("friends/easy")
                            myRe.child(code.text.toString()).removeValue()
                        }
                        val share1 = popupView1.findViewById<View>(R.id.imageView14) as ImageView
                        share1.setOnClickListener {
                            shareCode()
                        }

                        val share2 = popupView1.findViewById<View>(R.id.imageView15) as ImageView
                        share2.setOnClickListener {
                            shareCode()
                        }
                    }
                }

                val medium = popupView.findViewById<Button>(R.id.medium)
                medium.setOnClickListener {
                    popupWindow.dismiss()

                    if (coinsAmount.text.toString().toInt()<300){
                        notEnoughCoinsPopUp(it)

                    } else {
                        val popupView1 = inflater.inflate(R.layout.activity_invite, null)
                        val popupWindow1 = PopupWindow(popupView1, popWidth, popHeight, true)
                        popupWindow1.animationStyle = R.style.Animation2
                        popupWindow1.showAtLocation(view, Gravity.CENTER, 0, 0)
                        popupWindow1.isOutsideTouchable = true
                        popupWindow1.isTouchable = true
                        popupWindow1.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                            motionEvent.y < 0 || motionEvent.y > view1.height
                        })

                        val code = popupView1.findViewById<TextView>(R.id.textView14)
                        code.text = (400001..700000).random().toString()
                        txtCode = code.text.toString()

                        val randomGen = Helpers().generateRandomGen(this, "medium", 5)

                        DataBase().write("", "friends/medium/${code.text}/$username@$profileId")

                        DataBase().write(randomGen, "friends/medium/${code.text}/random")

                        val close1 = popupView1.findViewById<View>(R.id.imageView9) as ImageView
                        close1.setOnClickListener {
                            popupWindow1.dismiss()
                            val myRe = database.getReference("friends/medium")
                            myRe.child(code.text.toString()).removeValue()
                        }
                        val share1 = popupView1.findViewById<View>(R.id.imageView14) as ImageView
                        share1.setOnClickListener {
                            shareCode()
                        }

                        val share2 = popupView1.findViewById<View>(R.id.imageView15) as ImageView
                        share2.setOnClickListener {
                            shareCode()
                        }
                    }
                }

                val hard = popupView.findViewById<Button>(R.id.hard)
                hard.setOnClickListener {
                    popupWindow.dismiss()

                    if (coinsAmount.text.toString().toInt()<1000){
                        notEnoughCoinsPopUp(it)
                    }
                    else{
                        val popupView1 = inflater.inflate(R.layout.activity_invite, null)
                        val popupWindow1 = PopupWindow(popupView1, popWidth, popHeight, true)
                        popupWindow1.animationStyle = R.style.Animation2
                        popupWindow1.showAtLocation(view, Gravity.CENTER, 0, 0)
                        popupWindow1.isOutsideTouchable = true
                        popupWindow1.isTouchable = true
                        popupWindow1.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
                            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                            motionEvent.y < 0 || motionEvent.y > view1.height
                        })

                        val code = popupView1.findViewById<TextView>(R.id.textView14)
                        code.text = (700001..999999).random().toString()
                        txtCode = code.text.toString()

                        val randomGen = Helpers().generateRandomGen(this, "hard", 5)

                        DataBase().write("", "friends/hard/${code.text}/$username@$profileId")

                        DataBase().write(randomGen, "friends/hard/${code.text}/random")

                        val close1 = popupView1.findViewById<View>(R.id.imageView9) as ImageView
                        close1.setOnClickListener {
                            popupWindow1.dismiss()
                            val myRe = database.getReference("friends/hard")
                            myRe.child(code.text.toString()).removeValue()
                        }
                        val share1 = popupView1.findViewById<View>(R.id.imageView14) as ImageView
                        share1.setOnClickListener {
                            shareCode()
                        }

                        val share2 = popupView1.findViewById<View>(R.id.imageView15) as ImageView
                        share2.setOnClickListener {
                            shareCode()
                        }
                    }
                }
            }

            val join = popupView2.findViewById<View>(R.id.join)
            val textCode = popupView2.findViewById<EditText>(R.id.textCode)

            join.setOnClickListener{ it ->
                txtCode = textCode.text.toString()
                val valid =  try{
                    txtCode.toInt()
                    true
                } catch (_: Exception){
                    false
                }
                if (valid){
                    val mode = when {
                        textCode.text.toString().toInt() <= 400000 -> "easy"
                        textCode.text.toString().toInt() <= 700000 -> "medium"
                        else -> "hard"
                    }

                    if (mode == "easy" && coinsNumber.toInt() < 50 || mode == "medium" && coinsNumber.toInt() < 300 || mode == "hard" && coinsNumber.toInt() < 1000) {
                        popupWindow2.dismiss()
                        notEnoughCoinsPopUp(it)
                    }
                    else{
                        val database = Firebase.database
                        val ref = database.getReference("friends/$mode/${textCode.text}")

                        var exist = false
                        var begin = false
                        var full = false

                        ref.get().addOnSuccessListener {
                            if (it.value.toString() != "null"){
                                exist = true
                                val values = it.value.toString()
                                if ("begin" in values) begin = true
                                if ("full" in values) full = true
                            }

                            if (exist && !begin && !full){
                                DataBase().write("", "friends/$mode/${textCode.text}/$username@$profileId")
                                waitingRoomJoin()
                            }
                            else if (begin)
                                "Your friends has already start playing".show(this)
                            else if (full)
                                "The group is full".show(this)
                            else {
                                "This code not exist".show(this)
                            }
                        }
                    }

                } else{
                    "The code must consing of only numbers".show(this)
                }
            }
        } catch (_: Exception){}
    }

    fun waitingRoom(view: View) {
        val intent = Intent(this, WaitingRoom::class.java)
        intent.putExtra("code", txtCode)
        intent.putExtra("join", "false")
        intent.putExtra("profileId", profileId)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }

    private fun waitingRoomJoin(){
        val intent = Intent(this, WaitingRoom::class.java)
        intent.putExtra("code", txtCode)
        intent.putExtra("join", "true")
        intent.putExtra("profileId", profileId)

        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }

    private fun waitingRoomJoinFromCode(code: String){
        val intent = Intent(this, WaitingRoom::class.java)
        intent.putExtra("code", code)
        intent.putExtra("join", "true")
        intent.putExtra("profileId", profileId)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }

    private fun updateQuestions(version: String){
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        db.collection("questions").document("python").get().addOnSuccessListener {
            val questionListBeginner = it.data?.get("beginner").toString()
            val questionListEasyString = it.data?.get("easy").toString()
            val questionListMediumString = it.data?.get("medium").toString()
            val questionListHardString = it.data?.get("hard").toString()
            val namesString = it.data?.get("names").toString()
            val namesBeginner = it.data?.get("Begginer_names").toString()
            val inputString = it.data?.get("input").toString()
            val outputString = it.data?.get("output").toString()
            val solutions = it.data?.get("solution").toString()

            val editor = sharedPreference.edit()
            editor.putString("questionListEasyString", questionListEasyString)
            editor.putString("questionListMediumString", questionListMediumString)
            editor.putString("questionListHardString", questionListHardString)
            editor.putString("questionListBeginnerString", questionListBeginner)
            editor.putString("namesString", "$namesBeginner@$namesString")
            editor.putString("solutions", solutions)
            editor.putString("inputString", inputString)
            editor.putString("outputString", outputString)
            editor.putString("version", version)

            editor.apply()
        }
    }

    private fun shareCode(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nI challenge you to sigma coding competition, just press on the link or enter the invitation code - $txtCode.\n\n"
            shareMessage =
                """
                ${shareMessage}https://www.eshqol.com/sigma/$txtCode 
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (_: Exception) { }
    }

    private fun watchAdd(){
        if (mRewardedAd != null){
            mRewardedAd?.show(this) {
                if (firstAdd){
                    "You earn 500 coins".snack(this, 4000)
                    firstAdd = false
                }
                earnCoinsFromVideo = true
                updateCoins(username)
            }

            RewardedAd.load(this,Helpers().addVideo, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(tagAdMob, adError.message)
                    mRewardedAd = null
                }
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(tagAdMob, "Ad was loaded.")
                    mRewardedAd = rewardedAd

                    mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            if (earnCoinsFromVideo) "You earn 500 coins".snack(this@HomeScreen)
                        }
                    }
                }
            })
        }
    }

    private fun updateCoins(username: String) {
        val db = Firebase.firestore
        val docRef = db.collection("root").document(username)

        docRef.get().addOnSuccessListener { document ->
            val current = document.data!!["0"].toString().toInt()
            DataBase().setValue(username, "0", current + 500)

            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            val amountC = (current + 500).toString()
            editor.putString("coins", amountC)
            editor.apply()
            val coinsAm = findViewById<TextView>(R.id.coins_amount)
            coinsAm.text = amountC
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "InflateParams")
    private fun notEnoughCoinsPopUp(it: View = window.decorView.findViewById(android.R.id.content), coinImage: Boolean = false){
        if (System.nanoTime()-last2<1000000000L) {
            return
        }
        last2 = System.nanoTime()

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.coins_video, null)
        val popupWindow = PopupWindow(popupView, popWidth, popHeight, true)
        popupWindow.animationStyle = R.style.Animation

        popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.setTouchInterceptor(OnTouchListener { view1, motionEvent ->
            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
            motionEvent.y < 0 || motionEvent.y > view1.height
        })

        val close = popupView.findViewById<View>(R.id.imageView9) as ImageView
        val titlePopUp = popupView.findViewById<View>(R.id.language) as TextView
        val subject = popupView.findViewById<View>(R.id.textView22) as TextView
        val add = popupView.findViewById<View>(R.id.add) as ImageView
        close.setOnClickListener {
            popupWindow.dismiss()
        }
        add.setOnClickListener {
            earnCoinsFromVideo = false
            watchAdd()
        }
        if (coinImage){
            titlePopUp.text = "Earn free coins"
            titlePopUp.textSize = 25.0f
            subject.text = "Watch a short video and earn 500 coins for free."
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.question, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.toString() == "Auto Send" -> {
                val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()

                val dialogTitle = if (sharedPreference.getString(
                        "AutoSend",
                        "false"
                    ) == "false"
                ) "Enable Auto Send (Beta)" else "Disable Auto Send"
                val parentLayout = findViewById<View>(android.R.id.content)

                AlertDialog.Builder(this)
                    .setTitle(dialogTitle)
                    .setMessage("Auto send detect when your answer is correct and send it automatically.\n(Python only)\n\nDo you want to proceed?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (dialogTitle == "Enable Auto Send (Beta)") {
                            editor.putString("AutoSend", "true")
                            editor.apply()

                            Snackbar.make(parentLayout, "Auto Send was enabled", Snackbar.LENGTH_SHORT).show()
                        } else {
                            editor.putString("AutoSend", "false")
                            editor.apply()

                            Snackbar.make(parentLayout, "Auto Send was disabled", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()

            }
            item.toString() == "Propose new question" -> {
                val intent = Intent(this, NewQuestion::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }




}
