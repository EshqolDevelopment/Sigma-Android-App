package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.BaseInputConnection
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.scale
import androidx.core.view.get
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.concurrent.thread


class MainActivityMultiPlayer : AppCompatActivity() {
    private var tic = 0L
    private var last = 0
    private var timeFinish = true
    private var timeLap = "0"
    private var copyLap = "0"
    private var funcText = ""
    private var level = ""
    private var num = ""
    private var players = arrayListOf<String>()
    private var profiles = arrayListOf<String>()
    private var code = ""
    private var rand = ""
    private val username = Helpers().getUsername()
    private var draw = false
    private var stop = false
    private var inputString = ""
    private var outputString = ""
    private var multiArguments = ""
    private var currentQuestionNumber = ""
    private var snackBarAlreadyShown = ""
    private var numbersOfQuestions = ""
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

    private fun refreshColorOnStart(){
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
            for (i in strings.indices){
                if (strings[i] == "def" || strings[i] == "function"){
                    val stt = strings[i+1].split("(")
                    functionNames.add(stt[0])
                }
            }

            for (words in numbers){
                if (txt.contains(words)){
                    for (i in txt.indices){
                        if (txt[i] in numbers){
                            var v = i
                            while (txt[v-1] in numbers){
                                v--
                            }
                            if (txt[v-1] in numbers || (txt[v-1] == ' ' || txt[v-1] == '[' || txt[v-1] == '=' || txt[v-1] == ':' || txt[v-1] == '(' || txt[v-1] == ','))
                                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#71a6d2")), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }


            for (words in orangeWords){
                if (txt.contains(words)){

                    for (i in 0..txt.length-words.length){
                        if (i == 0 || words == "def" || words == "function"){
                            if (txt.substring(i, i+words.length) == words && txt.substring(i, i+words.length+1) == "$words "){
                                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#cb6b2e")), i, i+words.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }else{
                            if (txt.substring(i, i+words.length) == words && txt.substring(i-1, i+words.length+1) == " $words "){
                                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#cb6b2e")), i, i+words.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            else if(txt.substring(i, i+words.length) == words && txt.substring(i-1, i+words.length) == " $words" && txt[i+words.length] in "([:)]")
                                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#cb6b2e")), i, i+words.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
            for (word in purpleWords){
                if (txt.contains(word)){
                    for (i in 0..txt.length-word.length){
                        if (txt.substring(i, i+word.length) == word && txt[i+word.length] in "([: )]" && txt[i-1] in "([: )]"){
                            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#a020f0")), i, i+word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
            for (word in functionNames){
                if (txt.contains(word)){
                    for (i in 0..txt.length-word.length){
                        if (txt.substring(i, i+word.length) == word && txt[i+word.length] == '('){
                            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FFFF00")), i, i+word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }

            if (txt.contains('"')){
                for (i in txt.indices){
                    if (txt[i] == '"'){
                        keepGreen = !keepGreen
                    }
                    if (keepGreen){
                        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#00FF00")), i, i+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            for (i in txt.indices){
                if (txt[i] == '#' || txt[i] == '/'){
                    note = true
                }
                else if (txt[i] == '\n'){
                    note = false
                }
                if (note){
                    spannable.setSpan(ForegroundColorSpan(Color.parseColor("#909090")), i, i+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        } catch (_: Exception){ }
        editText.setText(spannable)
    }

    @SuppressLint("SetTextI18n")
    private fun resultActivity(s: String){
        timeFinish = false
        if (!stop){
            stop = true
            if (num == numbersOfQuestions){
                DataBase().write(timeLap, "friends/$level/$code/times/$username")
                val textV = findViewById<TextView>(R.id.textView)
                textV.text = "Processing result..."
                val intent1 = Intent(this, Podium::class.java)
                intent1.putExtra("code", code)
                intent1.putExtra("level", level)
                intent1.putExtra("players", players)
                intent1.putExtra("profiles", profiles)

                startActivity(intent1)
                finish()
            }

            else{
                val intent = Intent(this, ResultMultiPlayer::class.java)
                intent.putExtra("state", s)
                intent.putExtra("timeLap", timeLap)
                intent.putExtra("code", code)
                intent.putExtra("level", level)
                intent.putExtra("num", num)
                intent.putExtra("random", rand)
                intent.putExtra("players", players)
                intent.putExtra("profiles", profiles)
                intent.putExtra("snackBarAlreadyShown", snackBarAlreadyShown)
                intent.putExtra("numbersOfQuestions", numbersOfQuestions)

                startActivity(intent)
                finish()
            }
        }

    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_multi_player)
        val timer = findViewById<TextView>(R.id.timer)
        val editText = findViewById<EditText>(R.id.functionText)
        val quit = findViewById<ImageView>(R.id.quit)
        val profile1 = findViewById<ImageView>(R.id.playerN1)
        val profile2 = findViewById<ImageView>(R.id.playerN2)
        val profile3 = findViewById<ImageView>(R.id.playerN3)
        val profile4 = findViewById<ImageView>(R.id.playerN4)
        val profile5 = findViewById<ImageView>(R.id.playerN5)

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

        code = intent.getStringExtra("code").toString()
        rand = intent.getStringExtra("random").toString()
        level = intent.getStringExtra("mode").toString()
        num = intent.getStringExtra("num").toString()
        timeLap = intent.getStringExtra("timeLap").toString()
        snackBarAlreadyShown = intent.getStringExtra("snackBarAlreadyShown").toString()
        val toc = intent.getLongExtra("tic", 0)
        tic = intent.getLongExtra("toc", 0)
        players = intent.getStringArrayListExtra("players") as ArrayList<String>
        profiles = intent.getStringArrayListExtra("profiles") as ArrayList<String>
        numbersOfQuestions = intent.getStringExtra("numbersOfQuestions").toString()

        copyLap = timeLap

        if (profiles.size>0) profile1.setProfileImage(profiles[0])
        if (profiles.size>1) profile2.setProfileImage(profiles[1])
        if (profiles.size>2) profile3.setProfileImage(profiles[2])
        if (profiles.size>3) profile4.setProfileImage(profiles[3])
        if (profiles.size>4) profile5.setProfileImage(profiles[4])

        val randGenerator = rand
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

        val desc = findViewById<TextView>(R.id.question)
        val metrics: DisplayMetrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        quit.setOnClickListener {
            Helpers().closeKeyboard(this)

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
        timeLap = (copyLap.toLong() + sec*1000).toString()
        refreshColorOnStart()

        help.refreshColors(editText)

        val myRef = Firebase.database.getReference("friends/$level/$code/question")
        val que1 = findViewById<TextView>(R.id.que1)
        val que2 = findViewById<TextView>(R.id.que2)
        val que3 = findViewById<TextView>(R.id.que3)
        val que4 = findViewById<TextView>(R.id.que4)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        seekBar.max = numbersOfQuestions.toInt()
        if (numbersOfQuestions != "3"){
            que1.alpha = 0f
            que2.alpha = 0f
            que3.alpha = 0f
            que4.alpha = 0f
        }

        seekBar.setOnTouchListener { _, _ -> true }

        var start = ""
        for (player in players){
            start += "${player.replace("_", "")}\n"
        }
        que1.text = start

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (stop){
                    myRef.removeEventListener(this)
                }
                else{
                    val data = dataSnapshot.value.toString()
                    if (data != "null"){
                        val sliceData = data.substring(1,data.length-2)
                        val paceArr = sliceData.split("=, ")
                        var question0Player = ""
                        var question1Player = ""
                        var question2Player = ""
                        var question3Player = ""
                        var question4Player = ""
                        var question5Player = ""

                        for (player in players)
                            question0Player += "$player\n"

                        val snack = Snackbar.make(findViewById(android.R.id.content), "", 3500)
                        val view = snack.view
                        val params = view.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.TOP
                        view.layoutParams = params

                        for (player in players){
                            for (i in 1..numbersOfQuestions.toInt()){
                                if ("$username$i" in paceArr)
                                    seekBar.progress = i
                                if ("$player$i" in paceArr){
                                    question0Player = question0Player.replace("$player\n", "")
                                    when (i) {
                                        1 -> {
                                            question1Player += "$player\n"

                                            val txt = when (i) {
                                                numbersOfQuestions.toInt() -> " finished the game!"
                                                numbersOfQuestions.toInt() - 1 -> " moved to the last question!"
                                                else -> " moved to the seconds question!"
                                            }

                                            val snackText = SpannableStringBuilder()
                                                .scale(1.2F) {
                                                    bold { append(player.replace("_", "")) }
                                                        .append(txt)}

                                            if ((snackText !in snackBarAlreadyShown) && (player.replace("_", "") != username.replace("_", ""))){
                                                snack.setText(snackText).show()
                                                snackBarAlreadyShown += snackText.toString()
                                            }
                                        }
                                        2 -> {
                                            question2Player += "$player\n"

                                            val txt = when (i) {
                                                numbersOfQuestions.toInt() -> " finished the game!"
                                                numbersOfQuestions.toInt() - 1 -> " moved to the last question!"
                                                else -> " moved to the third question!"
                                            }

                                            val snackText = SpannableStringBuilder()
                                                .scale(1.2F) {
                                                    bold { append(player.replace("_", "")) }
                                                        .append(txt)}

                                            if ((snackText !in snackBarAlreadyShown) && (player.replace("_", "") != username.replace("_", ""))){
                                                snack.setText(snackText).show()
                                                snackBarAlreadyShown += snackText.toString()
                                            }
                                        }
                                        3 -> {
                                            question3Player += "$player\n"

                                            val txt = when (i) {
                                                numbersOfQuestions.toInt() -> " finished the game!"
                                                numbersOfQuestions.toInt() - 1 -> " moved to the last question!"
                                                else -> " moved to the forth question!"
                                            }

                                            val snackText = SpannableStringBuilder()
                                                .scale(1.2F) {
                                                    bold { append(player.replace("_", "")) }
                                                        .append(txt)}

                                            if ((snackText !in snackBarAlreadyShown) && (player.replace("_", "") != username.replace("_", ""))){
                                                snack.setText(snackText).show()
                                                snackBarAlreadyShown += snackText.toString()
                                            }
                                        }
                                        4 -> {
                                            question4Player += "$player\n"

                                            val txt = when (i) {
                                                numbersOfQuestions.toInt() -> " finished the game!"
                                                numbersOfQuestions.toInt() - 1 -> " moved to the last question!"
                                                else -> " moved to the fifth question!"
                                            }

                                            val snackText = SpannableStringBuilder()
                                                .scale(1.2F) {
                                                    bold { append(player.replace("_", "")) }
                                                        .append(txt)}

                                            if ((snackText !in snackBarAlreadyShown) && (player.replace("_", "") != username.replace("_", ""))){
                                                snack.setText(snackText).show()
                                                snackBarAlreadyShown += snackText.toString()
                                            }
                                        }

                                        else -> {
                                            question5Player += "$player\n"

                                            val snackText = SpannableStringBuilder()
                                                .scale(1.2F) {
                                                    bold { append(player.replace("_", "")) }
                                                        .append(" finished the game!")}

                                            if ((snackText !in snackBarAlreadyShown) && (player.replace("_", "") != username.replace("_", ""))){
                                                snack.setText(snackText).show()
                                                snackBarAlreadyShown += snackText.toString()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        que1.text = question0Player.replace("_", "")
                        que2.text = question1Player.replace("_", "")
                        que3.text = question2Player.replace("_", "")
                        que4.text = question3Player.replace("_", "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        var notOver = true
        val tim = object : CountDownTimer((sec * 1000).toLong(), 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val currentTime = System.currentTimeMillis() - toc
                val secPass = sec*1000 - currentTime
                timer.text = (secPass / 1000L).toInt().toString() + " seconds"
                if ((secPass / 1000L).toInt() < 0){
                    onFinish()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                timer.text = "Times up"
                if (timeFinish && !stop && notOver) {
                    notOver = false
                    if (num != "1"){
                        val myRef1 = Firebase.database.getReference("friends/$level/$code/question/$username${num.toInt()-1}")
                        myRef1.removeValue()
                    }
                    DataBase().write("","friends/$level/$code/question/$username$num")

                    resultActivity("over")
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

        var obj = "Time out"

        Helpers().closeKeyboard(this)

        if (help.currentLanguage == "Python"){
            if (lastPythonText != editText.text.toString() && textView.text.toString() != "Processing...") {
                lastPythonText = editText.text.toString()

                val t1 = thread {
                    val py = Python.getInstance()
                    val pyobj = py.getModule("CodingTrivia")
                    obj = try{
                        pyobj.callAttr("main", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }

                }
                t1.join(800)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj

                    if (obj == "True"){
                        Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                        textView.text = "Processing your answer..."
                        draw = true
                        timeLap = (copyLap.toLong() + (System.nanoTime()-tic)/1000000).toString()
                        timeFinish = false
                        editText.isEnabled = false

                        if (num != "1"){
                            val myRef = Firebase.database.getReference("friends/$level/$code/question/$username${num.toInt()-1}")
                            myRef.removeValue()
                        }

                        DataBase().write("","friends/$level/$code/question/$username$num")

                        resultActivity("right")
                    }

                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect Answer"
                }
            }
        }

        else if (help.currentLanguage == "Javascript"){
            if (lastJavascriptText != editText.text.toString() && textView.text.toString() != "Processing...") {
                lastJavascriptText = editText.text.toString()

                val t1 = thread {
                    val py = Python.getInstance()
                    val pyobj = py.getModule("JavaScriptCheck")
                    obj = try{
                        pyobj.callAttr("check_js", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }

                }
                t1.join(4000)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj

                    if (obj == "True"){
                        Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                        textView.text = "Processing your answer..."
                        draw = true
                        timeLap = (copyLap.toLong() + (System.nanoTime()-tic)/1000000).toString()
                        timeFinish = false
                        editText.isEnabled = false

                        if (num != "1"){
                            val myRef = Firebase.database.getReference("friends/$level/$code/question/$username${num.toInt()-1}")
                            myRef.removeValue()
                        }

                        DataBase().write("","friends/$level/$code/question/$username$num")

                        resultActivity("right")
                    }

                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect Answer"
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
            var obj = "Time out"
            val newText = editText.text.toString()

            if (newText == lastText) return
            lastText = newText

            if (!newText.contains("return")) return

            val t1 = Thread {
                val py = Python.getInstance()
                val pyobj = py.getModule("CodingTrivia")

                obj = try{
                    pyobj.callAttr("main", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                } catch (_: Exception) {
                    "An error occurred"
                }

            }
            t1.start()
            t1.join(500)

            try {
                if (obj == "True"){
                    Helpers().addQuestionToLast20(this, currentQuestionNumber, level)

                    timeFinish = false
                    draw = true
                    timeLap = (copyLap.toLong() + (System.nanoTime()-tic)/1000000).toString()
//                editText.isEnabled = false

                    if (num != "1"){
                        val myRef = Firebase.database.getReference("friends/$level/$code/question/$username${num.toInt()-1}")
                        myRef.removeValue()
                    }

                    DataBase().write("","friends/$level/$code/question/$username$num")

                    resultActivity("right")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.menu = menu
        }
        menuInflater.inflate(R.menu.competition, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Helpers().closeKeyboard(this)

        val editText = findViewById<EditText>(R.id.functionText)
        val descriptionText = findViewById<TextView>(R.id.question)

        when (item.toString()){
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

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onBackPressed() {
        val metrics: DisplayMetrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val view = findViewById<View>(android.R.id.content)

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.pop_up_exit, null)
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

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
    }
}
