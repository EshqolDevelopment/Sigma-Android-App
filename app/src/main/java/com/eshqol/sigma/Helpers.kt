package com.eshqol.sigma

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class Helpers {
    val orangeWords = arrayOf(
        "False",
        "None",
        "True",
        "and",
        "as",
        "assert",
        "async",
        "await",
        "break",
        "class",
        "continue",
        "def",
        "del",
        "elif",
        "else",
        "except",
        "finally",
        "for",
        "from",
        "global",
        "if",
        "import",
        "in",
        "is",
        "lambda",
        "nonlocal",
        "not",
        "or",
        "pass",
        "raise",
        "return",
        "try",
        "while",
        "with",
        "yield"
    )
    val purpleWords = arrayOf(
        "self",
        "__init__",
        "abs",
        "all",
        "any",
        "ascii",
        "bin",
        "bool",
        "bytearray",
        "bytes",
        "callable",
        "chr",
        "classmethod",
        "compile",
        "complex",
        "delattr",
        "dict",
        "dir",
        "divmod",
        "enumerate",
        "eval",
        "exec",
        "filter",
        "float",
        "format",
        "frozenset",
        "getattr",
        "globals",
        "hasattr",
        "hash",
        "help",
        "hex",
        "id",
        "input",
        "int",
        "isinstance",
        "issubclass",
        "iter",
        "len",
        "list",
        "locals",
        "map",
        "max",
        "memoryview",
        "min",
        "next",
        "object",
        "oct",
        "open",
        "ord",
        "pow",
        "print",
        "property",
        "range",
        "repr",
        "reversed",
        "round",
        "set",
        "setattr",
        "slice",
        "sorted",
        "staticmethod",
        "str",
        "sum",
        "super",
        "tuple",
        "type",
        "vars",
        "zip",
        "breakpoint",
        "str",
        "int",
        "float",
        "list",
        "sort"
    )

    val orangeWordsJS = arrayOf("abstract", "arguments", "await*", "boolean" ,"break", "byte", "case", "catch", "char", "class*", "const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum*", "eval", "export*", "extends*", "false", "final" ,"finally", "float", "for", "function", "goto", "if", "implements", "import*", "in", "instanceof", "int", "interface", "let*", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super*", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield")
    val purpleWordsJS = arrayOf(
        "console",
        "length",
        "Math"
    )
    val yellowColorsJS = arrayOf("toExponential", "toFixed", "toLocaleString", "toPrecision", "toString", "valueOf", "toSource", "toString", "valueOf", "charAt", "charCodeAt", "concat", "indexOf", "lastIndexOf", "localeCompare", "match", "replace", "replaceAll", "search", "slice", "split", "substr", "substring", "toLocaleLowerCase", "toLocaleUpperCase", "toLowerCase", "toString", "toUpperCase", "valueOf", "anchor", "big", "blink", "bold", "fixed", "fontcolor", "fontsize", "italics", "link", "small", "strike", "sub", "sup", "concat", "every", "filter", "forEach", "indexOf", "join", "lastIndexOf", "map", "pop", "push", "reduce", "reduceRight", "reverse", "shift", "slice", "some", "toSource", "sort", "splice", "toString", "unshift", "Date", "getDate", "getDay", "getFullYear", "getHours", "getMilliseconds", "getMinutes", "getMonth", "getSeconds", "getTime", "getTimezoneOffset", "getUTCDate", "getUTCDay", "getUTCFullYear", "getUTCHours", "getUTCMilliseconds", "getUTCMinutes", "getUTCMonth", "getUTCSeconds", "getYear", "setDate", "setFullYear", "setHours", "setMilliseconds", "setMinutes", "setMonth", "setSeconds", "setTime", "setUTCDate", "setUTCFullYear", "setUTCHours", "setUTCMilliseconds", "setUTCMinutes", "setUTCMonth", "setUTCSeconds", "setYear", "toDateString", "toGMTString", "toLocaleDateString", "toLocaleFormat", "toLocaleString", "toLocaleTimeString", "toSource", "toString", "toTimeString", "toUTCString", "valueOf", "Date.parse", "Date.UTC", "abs", "acos", "asin", "atan", "atan2", "ceil", "cos", "exp", "floor", "log", "max", "min", "pow", "random", "round", "sin", "sqrt", "tan", "toSource", "exec", "test", "toSource", "toString")

    val numbers = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    val addPhoto = "ca-app-pub-7535260219295739/3244749471"
    val addVideo = "ca-app-pub-7535260219295739/7431406498"

    val autoSendTime = 50L

    fun getUsername(): String {
        //'.', '#', '$', '[', or ']'
        val email = Firebase.auth.currentUser?.email.toString()
        val (username, provider) = email.split("@")
        if (provider == "sigma.com") {
            return username.replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
        }
        return "${username}_".replace(".", "").replace("#", "").replace("$", "").replace("[", "").replace("]", "")
    }

    fun generateRandomGen(activity: Activity, level: String, number: Int): String{
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val lastQuestionName = when (level) {
            "easy" -> "lastQuestionsEasy"
            "medium" -> "lastQuestionsMedium"
            else -> "lastQuestionsHard"
        }

        val lastQuestionsArr = sharedPreference.getString(lastQuestionName, "@-1").toString().substring(1).split("@")
        val last20 = if (lastQuestionsArr.size > 20) lastQuestionsArr.takeLast(20) else lastQuestionsArr

        last20.toString().log()
        val last20Numbers = arrayListOf<Int>()
        for (i in last20){
            last20Numbers.add(i.toInt())
        }

        val questionListNameString = when(level){
            "easy" -> "questionListEasyString"
            "medium" -> "questionListMediumString"
            else -> "questionListHardString"
        }

        val max = sharedPreference.getString(questionListNameString, "")!!
        val count = max.split('@').size - 1

        val randList = arrayListOf<Int>()

        while (randList.size != number){
            val rand = (0..count).random()
            if ((rand !in randList) && (rand !in last20Numbers)){
                randList.add(rand)
            }
        }
        return randList.joinToString(",")
    }

    fun addQuestionToLast20(activity: Activity, currentQuestionNumber: String, level: String){
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

        val lastQuestionName = when (level) {
            "easy" -> "lastQuestionsEasy"
            "medium" -> "lastQuestionsMedium"
            else -> "lastQuestionsHard"
        }

        val fileEditor = sharedPreference.edit()
        val lastQuestions = sharedPreference.getString(lastQuestionName, "@-1")
        fileEditor.putString(lastQuestionName, "$lastQuestions@$currentQuestionNumber")
        fileEditor.apply()
    }


    private var last = 0
    private var deletion = 0
    private var stopRefresh = false

    var currentLanguage = "Python"
    var pythonText = ""
    var jsText = ""

    private val orangeWords1 = {
        when (currentLanguage) {
            "Python" -> orangeWords
            else -> orangeWordsJS
        }
    }
    private val purpleWords1 = {
        when (currentLanguage) {
            "Python" -> purpleWords
            else -> purpleWordsJS
        }
    }

    private val yellowColors1 = {
        when (currentLanguage) {
            "Python" -> arrayOf()
            else -> yellowColorsJS
        }
    }


    fun refreshColorOnce(editText: EditText) {
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
            val orangeWords = orangeWords1()
            val purpleWords = purpleWords1()
            val numbers = numbers
            val functionNames = arrayListOf<String>()
            var keepGreen = false
            var note = false

            try {
                val mouseLine = getCurrentCursorLine(editText)
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
                }
                else if (txt[mousePos - 1] == '\n')
                    for (i in 0 until spaceCount) {
                        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
                    }


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
                                if ((txt[v - 1] in numbers) || (txt[v - 1] in " .[=:(,+-*/<>%\n"))
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

                        for (i in 0 until txt.length - words.length) {

                            if (txt.substring(i, i+words.length) == words && txt[i+words.length] in "+-*/=,([: )]\n" && (i == 0 || txt[i-1] in "+-*/=,([: )]\n")){
                                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#cb6b2e")), i, i + words.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }

                        }
                    }
                }

                for (word in purpleWords) {
                    if (txt.contains(word)) {
                        for (i in 0 until txt.length - word.length) {
                            if (txt.substring(
                                    i,
                                    i + word.length
                                ) == word && txt[i + word.length] in ".+-*/,([: )]=<>\n" && (i == 0 || txt[i - 1] in ".+-*/,([: )]=<>\n")
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

                for (word in yellowColors1()) {
                    if (txt.contains(word)) {
                        for (i in 1..txt.length - word.length) {
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

    fun refreshColors(editText: EditText){
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
                                val mouseLine = getCurrentCursorLine(editText)
                                var charInTheWay = false
                                val lines = txt.split('\n')

                                var spaceCount = 0
                                val currLine = lines[mouseLine]
                                if (currLine.trim().isEmpty() && txt[mousePos-1] != '\n' && mouseLine!=0){
                                    var lastL = txt[mousePos-1]
                                    while (lastL != '\n'){
                                        spaceCount++
                                        lastL = txt[mousePos-spaceCount]
                                    }
                                }else{
                                    charInTheWay = true
                                }

                                if (!charInTheWay && (spaceCount)%4==0){
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

                                val mouseLine = getCurrentCursorLine(editText)
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
                                    refreshColorOnce(editText)
                                    stopRefresh = false
                                }

                            } catch (_: Exception) {}
                        }
                        editText.setSelection(mousePos)
                    } catch (_: Exception) {}
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
                                    refreshColorOnce(editText)
                                    stopRefresh = false
                                }
                            } catch (_: Exception) {}
                        }
                        editText.setSelection(mousePos + brackets)
                        if (brackets != 0) {
                            refreshColorOnce(editText)
                            stopRefresh = false
                        }
                    } catch (_: Exception){}

                }

            }
        }
    }

    fun getCurrentCursorLine(editText: EditText): Int {
        val selectionStart = Selection.getSelectionStart(editText.text)
        val layout = editText.layout
        return if (selectionStart != -1) {
            layout.getLineForOffset(selectionStart)
        } else -1
    }

    fun metrics(activity: Activity): Pair<Int, Int>{
        val metrics: DisplayMetrics = activity.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return Pair(width, height)
    }

    fun read(activity: Activity, name: String, default: String = "null"): String {
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        return sharedPreference.getString(name, default).toString()
    }

    fun read(activity: Activity, nameAndContent: Array<String>): HashMap<String, String> {
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val hashMap = hashMapOf<String, String>()
        for (i in nameAndContent){
            hashMap[i] = sharedPreference.getString(i, "null").toString()
        }
        return hashMap
    }

    fun write(activity: Activity, name: String, content: String) {
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        editor.putString(name, content)
        editor.apply()
    }

    fun write(activity: Activity, nameAndContent: HashMap<String, String>){
        val sharedPreference = activity.getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        for (i in nameAndContent) {
            editor.putString(i.key, i.value)
        }
        editor.apply()
    }

    private fun popUpMetrics(activity: Activity): Pair<Int, Int>{
        val (width, height) = metrics(activity)
        val popWidth = (width.toFloat()*(1000f/1080f)).toInt()
        val popHeight = (height.toFloat()*(800f/2120f)).toInt()
        return Pair(popWidth, popHeight)
    }

    private var lastPopUp = 0L

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    fun popUp(activity: Activity, layout: Int, view: View = activity.findViewById(R.id.content)): Pair<View, PopupWindow>? {
        if (System.nanoTime()-lastPopUp < 1000000000L) {
            return null
        }
        lastPopUp = System.nanoTime()
        val inflater = activity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(layout, null)
        val (popWidth, popHeight) = popUpMetrics(activity)
        val popupWindow = PopupWindow(popupView, popWidth, popHeight, true)
        popupWindow.animationStyle = R.style.Animation

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.setTouchInterceptor(View.OnTouchListener { view1, motionEvent ->
            if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
            motionEvent.y < 0 || motionEvent.y > view1.height
        })
        return Pair(popupView, popupWindow)
    }

    fun closeKeyboard(activity: Activity){
        activity.currentFocus?.let { view1 ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view1.windowToken, 0)
        }
    }

    fun curlyBrackets(text: String, mousePos: Int, spaceCount: Int): String {
        val y = (1..spaceCount).joinToString("") { " " }
        return "${text.substring(0, mousePos + 1)}\n$y    \n$y}${text.substring(mousePos + 2)}"
    }


}
