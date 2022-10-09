package com.eshqol.sigma
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.set
import kotlin.concurrent.thread


class MainActivityPractice : AppCompatActivity() {
    private var funcText = ""
    private var functionName = ""
    private var multiArguments = ""
    private var stop = false
    private var inputString = ""
    private var outputString = ""
    private val db = Firebase.firestore
    private var theSolution = ""
    private var level = ""
    private var solutionUnlock = false
    private var currentLanguage = "Python"

    private var last = 0
    private var deletion = 0
    private var stopRefresh = false
    private val help = Helpers()
    private var pythonVersion = ""
    private var jsVersion = ""
    private var lastPythonText = ""
    private var lastJavascriptText = ""

    private var pythonText = ""
    private var jsText = ""

    private val username = Helpers().getUsername()
    private lateinit var menu: Menu
    private lateinit var py1: Python
    private lateinit var pyobj1: PyObject
    private lateinit var pyobj3: PyObject


    private val orangeWords = {
        when (currentLanguage) {
            "Python" -> help.orangeWords
            else -> help.orangeWordsJS
        }
    }
    private val purpleWords = {
        when (currentLanguage) {
            "Python" -> help.purpleWords
            else -> help.purpleWordsJS
        }
    }
    private val yellowColors = {
        when (currentLanguage) {
            "Python" -> arrayOf()
            else -> help.yellowColorsJS
        }
    }

    private fun refreshColorOnStart() {
        val editText = findViewById<EditText>(R.id.functionText)
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

            for (i in txt.indices) {

                if (txt[i] == '#' || (txt[i] == '/')) {
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

            for (words in orangeWords) {
                if (txt.contains(words)) {

                    for (i in 0..txt.length - words.length) {
                        if (i == 0 || words == "def") {
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
                                ) == " $words" && txt[i + words.length] in "([:)]={}:"
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
                            ) == word && txt[i + word.length] in ",([: )]=" && txt[i - 1] in ",([: )]="
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

        } catch (_: Exception) { }
        editText.setText(spannable)
    }


    private fun refreshColors() {
        val editText = findViewById<EditText>(R.id.functionText)
        last = 0
        var txt = editText.text.toString()
        if (txt.last() != ' ')
            txt += " "
        if (txt.length < deletion || (txt.last() != ' ' && txt.last() != '\n')) {
            deletion = txt.length

        } else {
            deletion = txt.length
            val spannable = SpannableString(txt)
            val mousePos = editText.selectionEnd
            val orangeWords = orangeWords()
            val purpleWords = purpleWords()
            val numbers = help.numbers
            val functionNames = arrayListOf<String>()
            var keepGreen = false
            var note = false

            try {
                val mouseLine = Helpers().getCurrentCursorLine(editText)
                val lines = txt.split('\n')
                var spaceCount = 0
                for (c in lines[mouseLine - 1].toCharArray()) {
                    if (c == ' ') {
                        spaceCount++
                    } else break
                }
                val textFieldInputConnection = BaseInputConnection(editText, true)
                if (txt[mousePos - 2] == ':' && txt[mousePos - 1] == '\n') {
                    for (i in 0..spaceCount + 3) {
                        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
                    }
                } else if (txt[mousePos - 1] == '\n')
                    for (i in 0 until spaceCount) {
                        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
                    }


                val strings = txt.split(" ")
                for (i in strings.indices) {
                    if ("def" in strings[i] || "function" in strings[i]) {
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
                                if (txt[v - 1] in numbers || txt[v-1] in " [=:(,.<>+-")
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

                try{
                    for (words in orangeWords) {
                        if (txt.contains(words)) {

                            for (i in 0..txt.length - words.length) {
                                if (txt.substring(i, i + words.length) == words && (txt[i+words.length] in " !)]}" && (i == 0 || txt[i-1] in " !([{") )) {
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
                } catch (_: Exception){}


                for (word in purpleWords) {
                    if (txt.contains(word)) {
                        for (i in 0..txt.length - word.length) {
                            if (txt.substring(i, i + word.length) == word) {
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



                for (word in yellowColors()) {
                    if (txt.contains(word)) {
                        for (i in 0..txt.length - word.length) {
                            if (txt.substring(i, i + word.length) == word && txt[i-1] == '.') {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#ffdd00")),
                                    i,
                                    i + word.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                spannable.setSpan(
                                    android.text.style.StyleSpan(android.graphics.Typeface.NORMAL),
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

                if (txt.contains('"') || txt.contains('\'')) {
                    for (i in txt.indices) {
                        if (txt[i] == '"' || txt[i] == '\'') {
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
                    if (txt[i] == '#' || (txt[i] == '/' && txt[i+1] == '/')) {
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

            editText.text.clear()
            editText.append(spannable)
            stopRefresh = true

            editText.setSelection(mousePos)
        }
    }


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val timer = findViewById<TextView>(R.id.timer)
        val editText = findViewById<EditText>(R.id.functionText)
        val quit = findViewById<ImageView>(R.id.quit)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val userName = findViewById<TextView>(R.id.player2Name)

        seekBar.setOnTouchListener { _, _ -> true }


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

        val profileId = sharedPreference.getString("profileId", "1").toString()
        findViewById<ImageView>(R.id.profile_enemy).setProfileImage(profileId)

        userName.text = username.replace("_", "")
        functionName = intent.getStringExtra("functionName").toString()
        multiArguments = intent.getStringExtra("multiArguments").toString()
        level = intent.getStringExtra("level").toString()
        val tic = intent.getLongExtra("tic", 0)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        py1 = Python.getInstance()
        pyobj1 = py1.getModule("CodingTrivia")
        pyobj3 = py1.getModule("JavaScriptCheck")

        seekBar.alpha = 0F
        var q1 = ""
        var find = false

        for (item in questionListEasy){
            if (functionName == item.split('(')[0]){
                q1 = item
                find = true
            }
        }
        if (!find) {
            for (item1 in questionListMedium){
                if (functionName == item1.split('(')[0]){
                    q1 = item1
                    find = true
                }
            }
        }
        if (!find) {
            for (item2 in questionListHard){
                if (functionName == item2.split('(')[0]){
                    q1 = item2
                    find = true
                }
            }
        }
        if (!find) {
            Log.i("tag123", "The function was not found")
        }
        val question1 = q1.split("& ")[0]
        val funcText1 = question1.split("(")[0]
        val description1 = q1.split("& ")[1]

        val desc = findViewById<TextView>(R.id.question)

        quit.setOnClickListener {
            try {
                stop = true
                val switchActivityIntent = Intent(this, Practice::class.java)
                startActivity(switchActivityIntent)
                finish()

            } catch (_: Exception) {
            }
        }

        val pyToJsArgs = {str: String ->
            val returnValue = if (str.count { it == ':' } == 0){
                str
            }else{
                str.replace(": str", "").replace(": int", "").replace(": float", "").replace(": list", "").replace(": dict", "").replace(": bool", "").replace(": any", "").replace(": set", "")
            }
            returnValue
        }

        pythonVersion = "def $question1:\n    # Write your code here\n    "
        jsVersion = "function ${pyToJsArgs(question1)}{\n    // Write your code here\n    \n}"

        editText.setText(pythonVersion)
        desc.text = "\n$description1\n"
        funcText = funcText1

        refreshColorOnStart()

        editText.addTextChangedListener {
            val txt1 = editText.text.toString()

            if (!stopRefresh){
                when (currentLanguage){
                    "Python" -> pythonText = txt1
                    else -> jsText = txt1
                }


                if (currentLanguage == "Python"){
                    try{
                        val mousePos = editText.selectionEnd
                        val txt = editText.text.toString()
                        val lastLetter = txt[mousePos-1]

                        if (txt.length < deletion) {
                            deletion = txt.length
                            try {
                                val mouseLine =  Helpers().getCurrentCursorLine(editText)
                                var charInTheWay = false
                                val lines = txt.split('\n')
                                var spaceCount = 0
                                for (x in lines[mouseLine].toCharArray()) {
                                    if (x == ' ') {
                                        spaceCount++
                                    } else {
                                        charInTheWay = true
                                        break
                                    }
                                }
                                if (!charInTheWay && (spaceCount+1)%4==0){
                                    val textFieldInputConnection = BaseInputConnection(editText, true)
                                    for (i in 0..2) {
                                        textFieldInputConnection.sendKeyEvent(
                                            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                                        )
                                    }
                                }
                            } catch (_: Exception){}

                        } else if (lastLetter != ' ' && lastLetter != '\n' && lastLetter != '(' && lastLetter != ')' && lastLetter != '"' && lastLetter != '\'' && lastLetter != ','){
                            deletion = txt.length
                        }
                        else {
                            try {
                                val mouseLine = Helpers().getCurrentCursorLine(editText)
                                val lines = txt.split('\n')
                                var spaceCount = 0
                                for (x in lines[mouseLine - 1].toCharArray()) {
                                    if (x == ' ') {
                                        spaceCount++
                                    } else break
                                }
                                val textFieldInputConnection = BaseInputConnection(editText, true)
                                if (txt[mousePos - 2] == ':' && txt[mousePos - 1] == '\n') {
                                    for (i in 0..spaceCount + 3) {
                                        textFieldInputConnection.sendKeyEvent(
                                            KeyEvent(
                                                KeyEvent.ACTION_DOWN,
                                                KeyEvent.KEYCODE_SPACE
                                            )
                                        )
                                    }
                                } else if (txt[mousePos - 1] == '\n') {
                                    for (i in 0 until spaceCount) {
                                        textFieldInputConnection.sendKeyEvent(
                                            KeyEvent(
                                                KeyEvent.ACTION_DOWN,
                                                KeyEvent.KEYCODE_SPACE
                                            )
                                        )
                                    }
                                }
                                else {
                                    stopRefresh = true
                                    refreshColors()
                                    stopRefresh = false
                                }

                            } catch (_: Exception) {}
                        }
                        editText.setSelection(mousePos)
                    } catch (_: Exception){}
                }
                else{
                    try{
                        var brackets = 0

                        val mousePos = editText.selectionEnd
                        val txt = editText.text.toString()
                        val lastLetter = txt[mousePos-1]
                        if (txt.length < deletion) {
                            deletion = txt.length
                            try {
                                val mouseLine =  Helpers().getCurrentCursorLine(editText)
                                var charInTheWay = false
                                val lines = txt.split('\n')
                                var spaceCount = 0
                                for (x in lines[mouseLine].toCharArray()) {
                                    if (x == ' ') {
                                        spaceCount++
                                    } else {
                                        charInTheWay = true
                                        break
                                    }
                                }
                                if (!charInTheWay && (spaceCount+1)%4==0){
                                    val textFieldInputConnection = BaseInputConnection(editText, true)
                                    for (i in 0..2) {
                                        textFieldInputConnection.sendKeyEvent(
                                            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                                        )
                                    }
                                }
                            } catch (_: Exception){}
                        } else if (lastLetter != ' ' && lastLetter != '\n' && lastLetter != '(' && lastLetter != ')' && lastLetter != '"' && lastLetter != '\'' && lastLetter != ','){
                            deletion = txt.length
                        }
                        else {
                            try {
                                val mouseLine =  Helpers().getCurrentCursorLine(editText)
                                val lines = txt.split('\n')
                                var spaceCount = 0
                                for (x in lines[mouseLine - 1].toCharArray()) {
                                    if (x == ' ') {
                                        spaceCount++
                                    } else break
                                }
                                val textFieldInputConnection = BaseInputConnection(editText, true)
                                if (txt[mousePos - 2] == '{' && txt[mousePos - 1] == '\n')  {
                                    if (txt.count{it == '{'} != txt.count{it == '}'}) {
                                        editText.setText(Helpers().curlyBrackets(txt, mousePos-2, spaceCount))
                                        brackets = 4 + spaceCount
                                    }
                                    else{
                                        for (i in 0..spaceCount + 3) {
                                            textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
                                        }
                                    }

                                }
                                else if (txt[mousePos - 1] == '\n') {
                                    for (i in 0 until spaceCount) {
                                        textFieldInputConnection.sendKeyEvent(
                                            KeyEvent(
                                                KeyEvent.ACTION_DOWN,
                                                KeyEvent.KEYCODE_SPACE
                                            )
                                        )
                                    }
                                }
                                else {
                                    stopRefresh = true
                                    refreshColors()
                                    stopRefresh = false
                                }
                            } catch (_: Exception) {}
                        }
                        editText.setSelection(mousePos + brackets)
                        if (brackets != 0) {
                            refreshColors()
                            stopRefresh = false
                        }
                    } catch (_: Exception){}

                }
            }
        }

        Thread {
            while(true) {
                if (stop)
                    break
                Thread.sleep(1000)
                this@MainActivityPractice.runOnUiThread {
                    timer.text = ((System.currentTimeMillis()-tic)/1000).toInt().toString()
                }
            }
        }.start()

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

        if (readWithTimeLimit(this, functionName)){
            solutionUnlock = true
        }
    }

    @SuppressLint("SetTextI18n")
    fun functionCheck(view: View) {
        val editText = findViewById<EditText>(R.id.functionText)
        val textView = findViewById<TextView>(R.id.textView)
        textView.movementMethod = ScrollingMovementMethod()

        var obj = "Time out"

        // close the keyboard
        this.currentFocus?.let { view1 ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view1.windowToken, 0)
        }

        if (currentLanguage == "Python" && this::pyobj1.isInitialized){
            if (lastPythonText != editText.text.toString() && textView.text.toString() != "Processing..."){
                lastPythonText = editText.text.toString()

                val t1 = thread {
                    obj = try {
                        pyobj1.callAttr("main", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }
                }
                t1.join(800)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj
                    if (textView.text.toString() == "True") {
                        textView.text = "You are right"
                        stop = true
                        val sharedPref = getSharedPreferences("CheckBoxList", Context.MODE_PRIVATE)
                        val myEdit = sharedPref.edit()
                        val timer = findViewById<TextView>(R.id.timer)

                        val currentState = read()
                        if (functionName !in currentState){
                            myEdit.putString("CheckBoxList", "$currentState-$functionName")
                            myEdit.apply()
                        }

                        db.collection("practice").document(username).get().addOnSuccessListener {
                            val currentStateFromDb = it["q"]
                            if (functionName !in currentStateFromDb.toString().split('-')) {
                                if (currentStateFromDb == null) {
                                    db.collection("practice").document(username).set(hashMapOf("q" to functionName))
                                } else {
                                    db.collection("practice").document(username).set(hashMapOf("q" to "$currentStateFromDb-$functionName"))
                                }
                            }
                        }
                        val intent = Intent(this, CorrectAnswer::class.java)
                        intent.putExtra("time", timer.text)
                        startActivity(intent)
                        finish()
                    }


                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect"
                }
            }

        } else if (currentLanguage == "Javascript" && this::pyobj3.isInitialized){
            if (lastJavascriptText != editText.text.toString() && textView.text.toString() != "Processing..."){
                lastJavascriptText = editText.text.toString()

                runOnUiThread {
                    textView.visibility = View.VISIBLE
                    textView.text = "Processing..."
                }

                val t1 = thread {
                    obj = try {
                         pyobj3.callAttr("check_js", editText.text.toString(), funcText, inputString, outputString, multiArguments).toString()
                    } catch (_: Exception) {
                        "An error occurred"
                    }
                }
                t1.join(4000)

                try {
                    textView.visibility = View.VISIBLE
                    textView.text = obj
                    if (textView.text.toString() == "True") {
                        textView.text = "You are right"
                        stop = true
                        val sharedPref = getSharedPreferences("CheckBoxList", Context.MODE_PRIVATE)
                        val myEdit = sharedPref.edit()
                        val timer = findViewById<TextView>(R.id.timer)

                        val currentState = read()
                        if (functionName !in currentState){
                            myEdit.putString("CheckBoxList", "$currentState-$functionName")
                            myEdit.apply()
                        }

                        db.collection("practice").document(username).get().addOnSuccessListener {
                            val currentStateFromDb = it["q"]
                            if (functionName !in currentStateFromDb.toString().split('-')) {
                                if (currentStateFromDb == null) {
                                    db.collection("practice").document(username).set(hashMapOf("q" to functionName))
                                } else {
                                    db.collection("practice").document(username).set(hashMapOf("q" to "$currentStateFromDb-$functionName"))
                                }
                            }
                        }
                        val intent = Intent(this, CorrectAnswer::class.java)
                        intent.putExtra("time", timer.text)
                        startActivity(intent)
                        finish()
                    }


                } catch (e: Exception) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Incorrect"
                }

            }
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
            } catch (_: Exception) {}
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

    private fun read(): String {
        val sharedPref = getSharedPreferences("CheckBoxList", Context.MODE_PRIVATE)
        return sharedPref.getString("CheckBoxList", "").toString()
    }

    private fun reportQuestion(name: String, text: String){
        val map = HashMap<String, String>()
        map[username] = text
        val ref = db.collection("reports").document(name)
        ref.update(username, text).addOnFailureListener {
            ref.set(map)
        }
    }

    private var backPressOnce = false
    override fun onBackPressed() {
        if (!backPressOnce) {
            "Click the back button again to exit".show(this)
            backPressOnce = true
        }
        else{
            try {
                stop = true
                val switchActivityIntent = Intent(this, Practice::class.java)
                startActivity(switchActivityIntent)
                finish()
            } catch (_: Exception) { }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.menu = menu
        }
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val solutions = sharedPreference.getString("solutions", "").toString()
        val arrSolution = solutions.split("@")
        var hasSolution = false

        for (i in arrSolution){
            if ("def $functionName" == i.split("(")[0]){
                hasSolution = true
                theSolution = i
                menuInflater.inflate(R.menu.report, menu)
                break
            }
        }
        if (!hasSolution){
            menuInflater.inflate(R.menu.just_report, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.currentFocus?.let { view1 ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view1.windowToken, 0)
        }

        val editText = findViewById<EditText>(R.id.functionText)
        val descriptionText = findViewById<TextView>(R.id.question)

        when {
            item.toString() == "Report a problem" -> {
                try {
                    val metrics: DisplayMetrics = this.resources.displayMetrics
                    val width = metrics.widthPixels
                    val height = metrics.heightPixels
                    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val popupView = inflater.inflate(R.layout.pop_up_report, null)
                    val popupWindow = PopupWindow(popupView, width, height, true)
                    popupWindow.showAtLocation(window.decorView.findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

                    popupWindow.isOutsideTouchable = true
                    popupWindow.isTouchable = true
                    popupWindow.setTouchInterceptor(View.OnTouchListener { view1, motionEvent ->
                        if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                        motionEvent.y < 0 || motionEvent.y > view1.height
                    })
                    val textInput = popupView.findViewById<View>(R.id.editTextTextPersonName) as EditText

                    val submit = popupView.findViewById<View>(R.id.submit) as Button
                    submit.setOnClickListener {
                        if (textInput.text.toString() == "") {
                            "You have not filled all the required fields".show(this)
                            return@setOnClickListener
                        }

                        reportQuestion(functionName, textInput.text.toString())
                        popupWindow.dismiss()
                        "Thank you for helping us to fix and improve our questions".snack(this, Snackbar.LENGTH_LONG)
                    }
                    val close = popupView.findViewById<View>(R.id.cancel) as Button
                    close.setOnClickListener {
                        popupWindow.dismiss()
                    }

                } catch (_: Exception) { }
            }

            item.toString() == "See the solution" -> {
                try {
                    if (currentLanguage == "Javascript") {
                        "The solution is available in python only".snack(this)
                        return true
                    }

                    if (solutionUnlock){
                        editText.setText(theSolution)
                        "The solution has been already unlocked".snack(this)
                        try {
                            Helpers().refreshColorOnce(editText)
                        } catch (_: Exception){}
                    }
                    else{
                        val (width, height) = Helpers().metrics(this)

                        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView = inflater.inflate(R.layout.pop_up_unclock_solution, null)
                        val popupWindow = PopupWindow(popupView, width, height, true)

                        popupWindow.showAtLocation(window.decorView.findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
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

                        val priceDesc = popupView.findViewById<View>(R.id.textView27) as TextView
                        priceDesc.text = when (level){
                            "easy" -> "The price is 30 coins"
                            "medium" -> "The price is 50 coins"
                            else -> "The price is 100 coins"
                        }
                        val coins = when (level) {
                            "easy" -> -30
                            "medium" -> -50
                            else -> -100
                        }

                        val unlockSolution = popupView.findViewById<View>(R.id.button12) as Button
                        unlockSolution.setOnClickListener {
                            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                            val current = sharedPreference.getString("coins", "").toString()
                            if (current.toInt() < coins * -1){
                                "You don't have enough coins to unlock the solution to this question".show(this)
                            }
                            else{
                                DataBase().updateCoins(this, coins)
                                writeWithTimeLimit(this, functionName, 60)

                                solutionUnlock = true
                                editText.setText(theSolution)
                                try {
                                    Helpers().refreshColorOnce(editText)
                                } catch (_: Exception){}

                                popupWindow.dismiss()
                                "The solution has been successfully unlocked".snack(this)
                            }

                        }
                    }

                } catch (_: Exception) { }
            }

            item.toString() == "Python" -> {
                currentLanguage = "Python"

                if (this::menu.isInitialized){
                    menu[0].setIcon(R.drawable.python_f)
                    menu[1].setIcon(R.drawable.js)
                }
                descriptionText.text = descriptionText.text.toString().replace("null", "None").replace("true", "True").replace("false", "False")

                if (pythonText == "") editText.setText(pythonVersion)
                else editText.setText(pythonText)

                refreshColorOnStart()
            }

            item.toString() == "Javascript" -> {
                currentLanguage = "Javascript"

                if (this::menu.isInitialized){
                    menu[0].setIcon(R.drawable.python_svg)
                    menu[1].setIcon(R.drawable.js_f)
                }
                descriptionText.text = descriptionText.text.toString().replace("None", "null").replace("True", "true").replace("False", "false")

                if (jsText == "") editText.setText(jsVersion.replace("var", "value"))
                else editText.setText(jsText)

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

    private fun writeWithTimeLimit(activity: Activity, name: String, minutes: Int){
        Helpers().write(activity, name,"${System.currentTimeMillis()}^*^$minutes")
    }

    private fun readWithTimeLimit(activity: Activity, name: String): Boolean{
        try{
            val txt = Helpers().read(activity, name)
            if (txt == "null"){
                return false
            }
            val arrTxt = txt.split("^*^")
            val timePast = System.currentTimeMillis() - arrTxt[0].toLong()
            if (timePast > 1000*60*arrTxt[1].toLong()){
                return false
            }
            return true
        } catch (_: Exception) {
            return false
        }

    }




}
