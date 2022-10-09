package com.eshqol.sigma

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.BaseInputConnection
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread


class MainActivityBot : AppCompatActivity() {
    private var last = 0
    private var result = "0"
    private var wGames = "0"
    private var lGames = "0"
    private var enemyProfile = ""
    private var timeFinish = true
    private var groupCode = ""
    private var funcText = ""
    private var level = ""
    private var num = ""
    private var player2 = ""
    private var name = ""
    private var lostA = false
    private var notQuit = false
    private var draw = false
    private var stop = false
    private var inputString = ""
    private var outputString = ""
    private var multiArguments = ""
    private var flagAlready = false
    private lateinit var alert: AlertDialog
    private lateinit var snack: Snackbar
    private lateinit var snackbar: Snackbar
    private var ticLap = System.currentTimeMillis()
    private var finalSec = 0
    private var currentQuestionNumber = ""
    private var lastText = ""
    private val help = Helpers()
    private lateinit var menu: Menu
    private var pythonVersion = ""
    private var jsVersion = ""
    private var lastPythonText = ""
    private var lastJavascriptText = ""


    private val orangeWords = {
        when (help.currentLanguage) {
            "Python" -> help.orangeWords
            else -> help.orangeWordsJS
        }
    }

    private val purpleWords = {
        when (help.currentLanguage) {
            "Python" -> help.purpleWords
            else -> help.purpleWordsJS
        }
    }

    private fun refreshColorOnStart() {
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val autoSendState = sharedPreference.getString("AutoSend", "false") == "true"
        if (autoSendState) checkWhileTrue()

        val editText = findViewById<EditText>(R.id.functionText)
        last = 0
        var txt = editText.text.toString()
        if (txt.last() != ' ')
            txt += " "

        val spannable = SpannableString(txt)
        val orangeWords = orangeWords()
        val purpleWords = purpleWords()
        val numbers = Helpers().numbers
        val functionNames = arrayListOf<String>()
        var keepGreen = false
        var note = false

        try {
            val strings = txt.split(" ")
            for (i in strings.indices) {
                if (strings[i] == "def" || strings[i] == "function") {
                    val stt = strings[i + 1].split("(")
                    functionNames.add(stt[0])
                }
            }

            for (words in numbers) {
                if (txt.contains(words)) {
                    for (i in txt.indices) {
                        if (txt[i] in numbers) {
                            var v = i
                            while (txt[v - 1] in numbers) {
                                v--
                            }
                            if (txt[v - 1] in numbers || (txt[v - 1] == ' ' || txt[v - 1] == '[' || txt[v - 1] == '=' || txt[v - 1] == ':' || txt[v - 1] == '(' || txt[v - 1] == ','))
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#71a6d2")),
                                    i,
                                    i + 1,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                        }
                    }
                }
            }


            for (words in orangeWords) {
                if (txt.contains(words)) {

                    for (i in 0..txt.length - words.length) {
                        if (i == 0 || words == "def" || words == "function") {
                            if (txt.substring(i, i + words.length) == words && txt.substring(
                                    i,
                                    i + words.length + 1
                                ) == "$words "
                            ) {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#cb6b2e")),
                                    i,
                                    i + words.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        } else {
                            if (txt.substring(i, i + words.length) == words && txt.substring(
                                    i - 1,
                                    i + words.length + 1
                                ) == " $words "
                            ) {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#cb6b2e")),
                                    i,
                                    i + words.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            } else if (txt.substring(i, i + words.length) == words && txt.substring(
                                    i - 1,
                                    i + words.length
                                ) == " $words" && txt[i + words.length] in "([:)]"
                            )
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#cb6b2e")),
                                    i,
                                    i + words.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                        }
                    }
                }
            }
            for (word in purpleWords) {
                if (txt.contains(word)) {
                    for (i in 0..txt.length - word.length) {
                        if (txt.substring(
                                i,
                                i + word.length
                            ) == word && txt[i + word.length] in ",([: )]" && txt[i - 1] in ",([: )]"
                        ) {
                            spannable.setSpan(
                                ForegroundColorSpan(Color.parseColor("#a020f0")),
                                i,
                                i + word.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }
            for (word in functionNames) {
                if (txt.contains(word)) {
                    for (i in 0..txt.length - word.length) {
                        if (txt.substring(i, i + word.length) == word && txt[i + word.length] == '(') {
                            spannable.setSpan(
                                ForegroundColorSpan(Color.parseColor("#FFFF00")),
                                i,
                                i + word.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }

            if (txt.contains('"')) {
                for (i in txt.indices) {
                    if (txt[i] == '"') {
                        keepGreen = !keepGreen
                    }
                    if (keepGreen) {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.parseColor("#00FF00")),
                            i,
                            i + 2,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            for (i in txt.indices) {
                if (txt[i] == '#' || txt[i] == '/') {
                    note = true
                } else if (txt[i] == '\n') {
                    note = false
                }
                if (note) {
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor("#909090")),
                        i,
                        i + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

        } catch (_: Exception) { }
        editText.setText(spannable)
    }

    private fun resultActivity(s: String) {
        notQuit = true
        lostA = true
        timeFinish = false
        try{
            alert.dismiss()
        } catch (_: Exception){}
        try{
            snackbar.dismiss()
        } catch (_: Exception){}


        val intent = Intent(this, ResultBot::class.java)
        intent.putExtra("state", s)
        intent.putExtra("data", name)
        intent.putExtra("level", level)
        intent.putExtra("num", num)
        intent.putExtra("enemyProfile", enemyProfile)
        intent.putExtra("code", groupCode)

        when (s) {
            "won" -> {
                intent.putExtra("result", (result.toInt() + 1).toString())
                intent.putExtra("wGames", (wGames.toInt() + 1).toString())
                intent.putExtra("lGames", (lGames.toInt()).toString())
            }
            "lost" -> {
                intent.putExtra("result", (result.toInt() - 1).toString())
                intent.putExtra("wGames", (wGames.toInt()).toString())
                intent.putExtra("lGames", (lGames.toInt() + 1).toString())
            }
            "quit" -> {
                intent.putExtra("result", (0).toString())
                intent.putExtra("wGames", (0).toString())
                intent.putExtra("lGames", (0).toString())
            }
            else -> {
                intent.putExtra("result", (result.toInt()).toString())
                intent.putExtra("wGames", (wGames.toInt() + 1).toString())
                intent.putExtra("lGames", (lGames.toInt() + 1).toString())
            }
        }
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val timer = findViewById<TextView>(R.id.timer)
        val editText = findViewById<EditText>(R.id.functionText)
        val quit = findViewById<ImageView>(R.id.quit)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        val A1 = findViewById<ImageView>(R.id.A_1)
        val A2 = findViewById<ImageView>(R.id.A_2)
        val A3 = findViewById<ImageView>(R.id.A_3)
        val A4 = findViewById<ImageView>(R.id.A_4)
        val A5 = findViewById<ImageView>(R.id.A_5)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val a  = getIconPreference(sharedPreference.getString("1","tab").toString())
        val b  = getIconPreference(sharedPreference.getString("2","equal").toString())
        val c  = getIconPreference(sharedPreference.getString("3","plus").toString())
        val d  = getIconPreference(sharedPreference.getString("4","minus").toString())
        val e = getIconPreference(sharedPreference.getString("5","squareBrackets").toString())

        A1.setImageResource(a)
        A2.setImageResource(b)
        A3.setImageResource(c)
        A4.setImageResource(d)
        A5.setImageResource(e)

        val questionListEasy = sharedPreference.getString("questionListEasyString", "0")?.split("@")?.toTypedArray()!!
        val questionListMedium = sharedPreference.getString("questionListMediumString", "0")?.split("@")?.toTypedArray()!!
        val questionListHard = sharedPreference.getString("questionListHardString", "0")?.split("@")?.toTypedArray()!!
        inputString = sharedPreference.getString("inputString", "0").toString()
        outputString = sharedPreference.getString("outputString", "0").toString()
        val names = sharedPreference.getString("namesString", "0")?.split("@")?.toTypedArray()!!

        name = intent.getStringExtra("data").toString()
        level = intent.getStringExtra("level").toString()
        num = intent.getStringExtra("num").toString()
        result = intent.getStringExtra("result").toString()
        wGames = intent.getStringExtra("wGames").toString()
        lGames = intent.getStringExtra("lGames").toString()
        lGames = intent.getStringExtra("lGames").toString()
        enemyProfile = intent.getStringExtra("enemyProfile").toString()


        val profileEnemyImage = findViewById<ImageView>(R.id.profile_enemy)
        profileEnemyImage.setProfileImage(enemyProfile)

        seekBar.setOnTouchListener { _, _ -> true }

        seekBar.progress = wGames.toInt()

        val randGenerator = name.split("@")[0]

        groupCode = name.split("@")[2]
        val questions = randGenerator.split(',')

        val q1: String
        val q2: String
        val q3: String
        val q4: String
        val q5: String
        when (level) {
            "easy" -> {
                q1 = questionListEasy[questions[0].toInt()]
                q2 = questionListEasy[questions[1].toInt()]
                q3 = questionListEasy[questions[2].toInt()]
                q4 = questionListEasy[questions[3].toInt()]
                q5 = questionListEasy[questions[4].toInt()]

            }
            "medium" -> {
                q1 = questionListMedium[questions[0].toInt()]
                q2 = questionListMedium[questions[1].toInt()]
                q3 = questionListMedium[questions[2].toInt()]
                q4 = questionListMedium[questions[3].toInt()]
                q5 = questionListMedium[questions[4].toInt()]
            }
            else -> {
                q1 = questionListHard[questions[0].toInt()]
                q2 = questionListHard[questions[1].toInt()]
                q3 = questionListHard[questions[2].toInt()]
                q4 = questionListHard[questions[3].toInt()]
                q5 = questionListHard[questions[4].toInt()]
            }
        }

        currentQuestionNumber = when (num){
            "1" -> questions[0]
            "2" -> questions[1]
            "3" -> questions[2]
            "4" -> questions[3]
            else -> questions[4]
        }


        val question1 = q1.split("& ")[0]
        val question2 = q2.split("& ")[0]
        val question3 = q3.split("& ")[0]
        val question4 = q4.split("& ")[0]
        val question5 = q5.split("& ")[0]

        val funcText1 = question1.split("(")[0]
        val funcText2 = question2.split("(")[0]
        val funcText3 = question3.split("(")[0]
        val funcText4 = question4.split("(")[0]
        val funcText5 = question5.split("(")[0]

        val description1 = q1.split("& ")[1]
        val description2 = q2.split("& ")[1]
        val description3 = q3.split("& ")[1]
        val description4 = q4.split("& ")[1]
        val description5 = q5.split("& ")[1]

        val timer1 = q1.split("& ")[2]
        val timer2 = q2.split("& ")[2]
        val timer3 = q3.split("& ")[2]
        val timer4 = q4.split("& ")[2]
        val timer5 = q5.split("& ")[2]

        val player2name = findViewById<TextView>(R.id.player2Name)
        val desc = findViewById<TextView>(R.id.question)

        player2name.text = name.split("@")[1]
        player2 = player2name.text.toString()
        val metrics: DisplayMetrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        quit.setOnClickListener {
            Helpers().closeKeyboard(this)

            try {
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.pop_up_exit, null)
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
                val exit = popupView.findViewById<View>(R.id.button12) as Button
                exit.setOnClickListener {
                    stop = true
                    val switchActivityIntent = Intent(this, HomeScreen::class.java)
                    startActivity(switchActivityIntent)
                    finish()
                }

            } catch (_: Exception) { }
        }
        val sec: Int

        when (num) {
            "1" -> {
                editText.setText("def $question1:\n    # Write your code here\n    ")
                desc.text = "\n$description1\n"
                sec = timer1.toInt()
                funcText = funcText1
            }
            "2" -> {
                editText.setText("def $question2:\n    # Write your code here\n    ")
                desc.text = "\n$description2\n"
                sec = timer2.toInt()
                funcText = funcText2
            }
            "3" -> {
                editText.setText("def $question3:\n    # Write your code here\n    ")
                desc.text = "\n$description3\n"
                sec = timer3.toInt()
                funcText = funcText3
            }
            "4" -> {
                editText.setText("def $question4:\n    # Write your code here\n    ")
                desc.text = "\n$description4\n"
                sec = timer4.toInt()
                funcText = funcText4
            }
            else -> {
                editText.setText("def $question5:\n    # Write your code here\n    ")
                desc.text = "\n$description5\n"
                sec = timer5.toInt()
                funcText = funcText5
            }
        }

        funcText.replace("_", " ").lowercase().log()
        for (item in names){
            if (funcText.replace("_", " ").lowercase() in item.lowercase()){
                multiArguments = ("$" in item).toString()
            }
        }
        refreshColorOnStart()

        help.refreshColors(editText)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val tic = System.currentTimeMillis()
        finalSec = sec

        val randLostTime = (getBotTime()/100)*finalSec

        thread {
            Thread.sleep(randLostTime.toLong()*1000 + 3000)
            if (timeFinish && !stop) {
                val timeDif = (System.currentTimeMillis()-ticLap)/1000F
                val percentTime = ((timeDif/finalSec.toFloat())*100F)*2
                val editor = sharedPreference.edit()

                when (level){
                    "easy" -> {
                        val easyTimes = sharedPreference.getString("EasyTimes", "1")
                        editor.putString("EasyTimes", "$easyTimes@$percentTime")
                        editor.apply()
                    }
                    "medium" -> {
                        val mediumTimes = sharedPreference.getString("MediumTimes", "1")
                        editor.putString("MediumTimes", "$mediumTimes@$percentTime")
                        editor.apply()
                    }
                    else -> {
                        val hardTimes = sharedPreference.getString("HardTimes", "1")
                        editor.putString("HardTimes", "$hardTimes@$percentTime")
                        editor.apply()
                    }
                }

                resultActivity("lost")
            }
        }

        val tim = object : CountDownTimer((sec * 1000).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val currentTime = System.currentTimeMillis() - tic
                val secPass = sec * 1000 - currentTime
                timer.text = (secPass / 1000).toString() + " seconds"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                val editor = sharedPreference.edit()

                timer.text = "Times up"
                if (timeFinish && !stop){

                    when (level){
                        "easy" -> {
                            val easyTimes = sharedPreference.getString("EasyTimes", "1")
                            editor.putString("EasyTimes", "$easyTimes@100")
                            editor.apply()
                        }
                        "medium" -> {
                            val mediumTimes = sharedPreference.getString("MediumTimes", "1")
                            editor.putString("MediumTimes", "$mediumTimes@100")
                            editor.apply()
                        }
                        else -> {
                            val hardTimes = sharedPreference.getString("HardTimes", "1")
                            editor.putString("HardTimes", "$hardTimes@100")
                            editor.apply()
                        }
                    }
                    resultActivity("draw")
                }
            }
        }
        tim.start()


        A1.setOnClickListener {
            try{
                press(a, editText)
            } catch (_: Exception){}
        }

        A2.setOnClickListener {
            try{
                press(b, editText)
            } catch (_: Exception){}
        }

        A3.setOnClickListener {
            try{
                press(c, editText)
            } catch (_: Exception){}
        }

        A4.setOnClickListener {
            try{
                press(d, editText)
            } catch (_: Exception){}
        }

        A5.setOnClickListener {
            try{
                press(e, editText)
            } catch (_: Exception){}
        }

        val pyToJsArgs = {str: String ->
            val returnValue = if (str.count { it == ':' } == 0){
                str
            }else{
                str.replace(": str", "").replace(": int", "").replace(": float", "").replace(": list", "").replace(": dict", "")
            }
            returnValue
        }


        when (num){
            "1" -> {
                pythonVersion = "def $question1:\n    # Write your code here\n    "
                jsVersion = "function ${pyToJsArgs(question1)}{\n    // Write your code here\n    \n}"
            }
            "2" -> {
                pythonVersion = "def $question2:\n    # Write your code here\n    "
                jsVersion = "function ${pyToJsArgs(question2)}{\n    // Write your code here\n    \n}"
            }
            "3" -> {
                pythonVersion = "def $question3:\n    # Write your code here\n    "
                jsVersion = "function ${pyToJsArgs(question3)}{\n    // Write your code here\n    \n}"
            }

        }

    }


    @SuppressLint("SetTextI18n")
    fun functionCheck(view: View) {
        val editText = findViewById<EditText>(R.id.functionText)
        val textView = findViewById<TextView>(R.id.textView)
        textView.movementMethod = ScrollingMovementMethod()

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        var obj = "Time out"

        Helpers().closeKeyboard(this)


        if (help.currentLanguage == "Python"){
            if (lastPythonText != editText.text.toString() && textView.text.toString() != "Processing...") {
                lastPythonText = editText.text.toString()

                val t1 = thread {
                    val py = Python.getInstance()
                    val pyobj = py.getModule("CodingTrivia")
                    obj = try {
                        pyobj.callAttr("main", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }
                }
                t1.join(800)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj
                    if (textView.text.toString() == "True") {
                        Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                        draw = true
                        notQuit = true
                        val timeDif = (System.currentTimeMillis()-ticLap).toFloat()/1000F
                        val percentTime = ((timeDif/finalSec.toFloat())*90F)
                        val editor = sharedPreference.edit()

                        when (level){
                            "easy" -> {
                                val easyTimes = sharedPreference.getString("EasyTimes", "1")
                                editor.putString("EasyTimes", "$easyTimes@$percentTime")
                                editor.apply()
                            }
                            "medium" -> {
                                val mediumTimes = sharedPreference.getString("MediumTimes", "1")
                                editor.putString("MediumTimes", "$mediumTimes@$percentTime")
                                editor.apply()
                            }
                            else -> {
                                val hardTimes = sharedPreference.getString("HardTimes", "1")
                                editor.putString("HardTimes", "$hardTimes@$percentTime")
                                editor.apply()
                            }
                        }

                        if (timeFinish) resultActivity("won")
                        textView.text = "Processing your answer..."
                    }

                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect"
                }
            }
        }
        else if (help.currentLanguage == "Javascript"){
            if (lastJavascriptText != editText.text.toString() && textView.text.toString() != "Processing...") {
                lastJavascriptText = editText.text.toString()

                val t1 = thread {
                    val py = Python.getInstance()
                    val pyobj = py.getModule("JavaScriptCheck")
                    obj = try {
                        pyobj.callAttr("check_js", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }
                }
                t1.join(4000)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj
                    if (textView.text.toString() == "True") {
                        Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                        draw = true
                        notQuit = true
                        val timeDif = (System.currentTimeMillis()-ticLap).toFloat()/1000F
                        val percentTime = ((timeDif/finalSec.toFloat())*90F)
                        val editor = sharedPreference.edit()

                        when (level){
                            "easy" -> {
                                val easyTimes = sharedPreference.getString("EasyTimes", "1")
                                editor.putString("EasyTimes", "$easyTimes@$percentTime")
                                editor.apply()
                            }
                            "medium" -> {
                                val mediumTimes = sharedPreference.getString("MediumTimes", "1")
                                editor.putString("MediumTimes", "$mediumTimes@$percentTime")
                                editor.apply()
                            }
                            else -> {
                                val hardTimes = sharedPreference.getString("HardTimes", "1")
                                editor.putString("HardTimes", "$hardTimes@$percentTime")
                                editor.apply()
                            }
                        }

                        if (timeFinish) resultActivity("won")
                        textView.text = "Processing your answer..."
                    }

                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect"
                }
            }
        }

    }

    private fun checkWhileTrue(){
        val mainHandler = Handler(mainLooper)

        mainHandler.post(object : Runnable {
            override fun run() {
                if (timeFinish && !stop){
                    thread {
                        justCheck()
                    }
                    mainHandler.postDelayed(this, Helpers().autoSendTime)
                }
            }
        })
    }

    fun justCheck(){
        if (help.currentLanguage == "Python"){
            val editText = findViewById<EditText>(R.id.functionText)
            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
            var obj = "Time out"
            val newText = editText.text.toString()

            if (newText == lastText) return
            lastText = newText

            if (!newText.contains("return")) return

            val t1 = Thread {
                val py = Python.getInstance()
                val pyobj = py.getModule("CodingTrivia")
                obj = try {
                    pyobj.callAttr("main", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                } catch (_: Exception) {
                    "An error occurred"
                }
            }
            t1.start()
            t1.join(500)

            try {
                if (obj == "True") {
                    Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                    draw = true
                    notQuit = true
                    val timeDif = (System.currentTimeMillis()-ticLap).toFloat()/1000F
                    val percentTime = ((timeDif/finalSec.toFloat())*90F)
                    val editor = sharedPreference.edit()

                    when (level){
                        "easy" -> {
                            val easyTimes = sharedPreference.getString("EasyTimes", "1")
                            editor.putString("EasyTimes", "$easyTimes@$percentTime")
                            editor.apply()
                        }
                        "medium" -> {
                            val mediumTimes = sharedPreference.getString("MediumTimes", "1")
                            editor.putString("MediumTimes", "$mediumTimes@$percentTime")
                            editor.apply()
                        }
                        else -> {
                            val hardTimes = sharedPreference.getString("HardTimes", "1")
                            editor.putString("HardTimes", "$hardTimes@$percentTime")
                            editor.apply()
                        }
                    }

                    if (timeFinish) resultActivity("won")
                }

            } catch (_: Exception) { }
        }
    }

    private fun tab(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        for (i in 0..3) {
            textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        }
    }

    private fun equal(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_EQUALS))
    }

    private fun returnIco(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_E))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_T))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_U))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_N))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
    }

    private fun forLoop(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_O))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_I))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_I))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_N))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_N))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_G))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_E))
        parentheses(editText)
    }

    private fun parentheses(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN))
        thread {
            try{
                Thread.sleep(250)
                editText.setSelection(editText.selectionEnd - 2)
            } catch (_: Exception){}

        }
    }

    private fun minus(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MINUS))
    }

    fun plus(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PLUS))
    }

    private fun semiColon(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEMICOLON))
    }

    private fun merchaot(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_APOSTROPHE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_APOSTROPHE))
        thread {
            try{
                Thread.sleep(250)
                editText.setSelection(editText.selectionEnd - 1)
            } catch (_: Exception){}

        }
    }

    private fun square(editText: EditText) {
        val textFieldInputConnection = BaseInputConnection(editText, true)
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_LEFT_BRACKET))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_RIGHT_BRACKET))
        thread {
            try{
                Thread.sleep(250)
                editText.setSelection(editText.selectionEnd - 2)
            } catch (_: Exception){}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop = true
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onBackPressed() {
        try {
            val metrics: DisplayMetrics = this.resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val it = window.decorView.findViewById<View>(android.R.id.content)

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.pop_up_exit, null)
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
            val exit = popupView.findViewById<View>(R.id.button12) as Button
            exit.setOnClickListener {
                stop = true
                timeFinish = false
                val switchActivityIntent = Intent(this, HomeScreen::class.java)
                startActivity(switchActivityIntent)
                finish()
            }

        } catch (_: Exception) { }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.menu = menu
        }
        menuInflater.inflate(R.menu.flag, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Helpers().closeKeyboard(this)

        val editText = findViewById<EditText>(R.id.functionText)
        val descriptionText = findViewById<TextView>(R.id.question)

        when (item.toString()){
            "Suggest a draw" -> {
                if (!flagAlready){
                    val parentLayout = findViewById<View>(android.R.id.content)
                    alert = AlertDialog.Builder(this)
                        .setTitle("Suggested a draw")
                        .setMessage("After raising a white flag you want have the option to reverse it. do you want to proceed?")
                        .setPositiveButton("Yes") { _, _ ->
                            try {
                                flagAlready = true
                                snack = Snackbar.make(parentLayout, "Waiting for response from the opponent...", Snackbar.LENGTH_INDEFINITE)

                                val view = snack.view
                                val params = view.layoutParams as FrameLayout.LayoutParams
                                params.gravity = Gravity.TOP
                                view.layoutParams = params

                                snack.show()
                                item.setIcon(R.drawable.fat_flag)
                                thread {
                                    Thread.sleep((4000..6000).random().toLong())
                                    if ((0..2).random() == 0){
                                        if (timeFinish) resultActivity("draw")
                                    }
                                    else{
                                        runOnUiThread { snack.setText("The opponent rejected your offer") }
                                        Thread.sleep(2000)
                                        runOnUiThread { snack.dismiss() }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                        .setNegativeButton("No", null)
                        .setIcon(R.drawable.ic_baseline_flag_24)
                        .show()
                }else{
                    "You can't offer a draw more than once a question".show(this)
                }
            }

            "Python" -> {
                help.currentLanguage = "Python"

                if (this::menu.isInitialized){
                    menu[0].setIcon(R.drawable.python_f)
                    menu[1].setIcon(R.drawable.js)
                }
                descriptionText.text = descriptionText.text.toString().replace("null", "None").replace("true", "True").replace("false", "False")

                if (help.pythonText == "") editText.setText(pythonVersion)
                else editText.setText(help.pythonText)

                refreshColorOnStart()
            }

            "Javascript" -> {
                help.currentLanguage = "Javascript"

                if (this::menu.isInitialized){
                    menu[0].setIcon(R.drawable.python_svg)
                    menu[1].setIcon(R.drawable.js_f)
                }
                descriptionText.text = descriptionText.text.toString().replace("None", "null").replace("True", "true").replace("False", "false")

                if (help.jsText == "") editText.setText(jsVersion.replace("var", "value"))
                else editText.setText(help.jsText)

                refreshColorOnStart()
            }
        }


        return super.onOptionsItemSelected(item)
    }

    private fun getIconPreference(ico: String): Int {
        return when (ico){
            "equal" -> R.drawable.equal123
            "minus" -> R.drawable.minus
            "plus" -> R.drawable.plus123
            "para" -> R.drawable.para_icon
            "semiColon" -> R.drawable.semi_colon_ico
            "tab" -> R.drawable.tab123
            "squareBrackets" -> R.drawable.brackets
            "forLoop" -> R.drawable.loop_ico
            "returnIco" -> R.drawable.return_ico
            else -> R.drawable.merchaot_ico
        }
    }

    private fun press(btn: Int, editText: EditText){
        when (btn){
            R.drawable.merchaot_ico -> merchaot(editText)
            R.drawable.return_ico -> returnIco(editText)
            R.drawable.plus123 -> plus(editText)
            R.drawable.para_icon -> parentheses(editText)
            R.drawable.semi_colon_ico -> semiColon(editText)
            R.drawable.minus -> minus(editText)
            R.drawable.brackets-> square(editText)
            R.drawable.loop_ico -> forLoop(editText)
            R.drawable.tab123 -> tab(editText)
            else -> equal(editText)
        }
    }

    private fun getBotTime(): Float{
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val times = when (level){
            "easy" -> {
                sharedPreference.getString("EasyTimes", "1").toString()
            }
            "medium" -> {
                sharedPreference.getString("MediumTimes", "1").toString()
            }
            else -> {
                sharedPreference.getString("HardTimes", "1").toString()
            }
        }
        val arrTimes = times.split('@')
        var sum = 0F

        return if (arrTimes.size > 2) {
            for (i in 1 until arrTimes.size) sum += arrTimes[i].toFloat()
            sum/(arrTimes.size-1)
        }
        else {
            for (time in arrTimes) sum += time.toFloat()
            sum/(arrTimes.size)
        }
    }
}