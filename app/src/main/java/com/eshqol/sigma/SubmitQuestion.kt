package com.eshqol.sigma
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ncorti.slidetoact.SlideToActView


class SubmitQuestion : AppCompatActivity() {

    private fun String.title(): String {
        return toString()[0].uppercase() + toString().substring(1)
    }

    private val db = Firebase.firestore

    lateinit var nameAndArgs: EditText
    lateinit var level: EditText
    lateinit var subject: EditText
    lateinit var time: EditText
    lateinit var description: EditText
    lateinit var inputs: EditText
    lateinit var outputs: EditText
    lateinit var solution: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submitquestion)
        title = "Submit new question"
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        nameAndArgs = findViewById(R.id.question3)
        level = findViewById(R.id.editTextTextPersonName2)
        subject = findViewById(R.id.editTextTextPersonName3)
        time = findViewById(R.id.editTextTextPersonName4)
        description = findViewById(R.id.question)
        inputs = findViewById(R.id.question4)
        outputs = findViewById(R.id.question5)
        solution = findViewById(R.id.question2)

        val send = findViewById<SlideToActView>(R.id.slideButton1)
        send.bumpVibration = 50

        solution.doOnTextChanged { _, _, _, _ ->
            try{
                val mousePos = solution.selectionEnd
                val txt = solution.text.toString()
                if (txt.length < deletion) {
                    deletion = txt.length
                    val mouseLine = getCurrentCursorLine(solution)
                    var charInTheWay = false
                    val lines = txt.split('\n')
                    var spaceCount = 0
                    for (c in lines[mouseLine].toCharArray()) {
                        if (c == ' ') {
                            spaceCount++
                        } else {
                            charInTheWay = true
                            break
                        }
                    }
                    if (!charInTheWay && (spaceCount+1)%4==0){
                        val textFieldInputConnection = BaseInputConnection(solution, true)
                        for (i in 0..2) {
                            textFieldInputConnection.sendKeyEvent(
                                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                            )
                        }
                    }

                } else if ((txt.last() != ' ' && txt.last() != '\n')){
                    deletion = txt.length
                }
                else {
                    val mouseLine = getCurrentCursorLine(solution)
                    val lines = txt.split('\n')
                    var spaceCount = 0
                    for (c in lines[mouseLine - 1].toCharArray()) {
                        if (c == ' ') {
                            spaceCount++
                        } else break
                    }
                    val textFieldInputConnection = BaseInputConnection(solution, true)
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
                    else if (txt[mousePos - 1] == ' ')
                        refreshColors(solution)

                }
            } catch (_: Exception){}
        }

        val parentLayout = findViewById<View>(android.R.id.content)

        send.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    if (outputs.text.toString().trim() == "" || nameAndArgs.text.toString().trim() == "" || level.text.toString().trim() == "" || time.text.toString().trim() == "" || subject.text.toString().trim() == "" || description.text.toString().trim() == "" || solution.text.toString().trim() == ""){
                        send.resetSlider()
                        val snack = Snackbar.make(parentLayout, "Please fill all the required fields", Snackbar.LENGTH_SHORT)
                        snack.setTextColor(Color.parseColor("#FF0000"))
                        snack.show()
                    }
                    else{
                        val multiArgs = subject.text.toString()[0] == '$'
                        val result = test(nameAndArgs.text.toString(), inputs.text.toString(), outputs.text.toString(), solution.text.toString(), multiArgs)
                        if (result){
                            sendQuestion(description.text.toString(), level.text.toString(), nameAndArgs.text.toString(), time.text.toString(), outputs.text.toString(), inputs.text.toString(), subject.text.toString())
                        }
                        Handler(mainLooper).postDelayed({
                            send.resetSlider()
                        }, 1000)
                    }
                }
            }
    }

    private var last = 0
    private var deletion = 0

    private fun refreshColors(editText: EditText) {
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
            val orangeWords = Helpers().orangeWords
            val purpleWords = Helpers().purpleWords
            val numbers = Helpers().numbers
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
                } else if (txt[mousePos - 1] == '\n')
                    for (i in 0 until spaceCount) {
                        textFieldInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
                    }


                val strings = txt.split(" ")
                for (i in strings.indices) {
                    if (strings[i] == "def") {
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
                                    ) == " $words" && txt[i + words.length] in "([:)]\n"
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

                if (txt.contains('#')) {
                    for (i in txt.indices) {
                        if (txt[i] == '#') {
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
                }

            } catch (_: Exception) {
            }
            editText.setText(spannable)
            editText.setSelection(mousePos)
        }
    }

    private fun getCurrentCursorLine(editText: EditText): Int {
        val selectionStart = Selection.getSelectionStart(editText.text)
        val layout = editText.layout
        return if (selectionStart != -1) {
            layout.getLineForOffset(selectionStart)
        } else -1
    }

    private fun test(nameAndArgs: String, inputs: String, outputs: String, solution: String, multiArgs: Boolean): Boolean{
        val name = nameAndArgs.split('(')[0]
        val inputs1: String
        val outputs1: String
        if (inputs == ""){
            inputs1 = "{'$name': []}"
            outputs1 = "{'$name': $outputs}"
        }
        else{
            inputs1 = "{'$name': [$inputs]}"
            outputs1 = "{'$name': [$outputs]}"
        }

        val py = Python.getInstance()
        val pyobj = py.getModule("CodingTrivia")
        val obj = if (solution == "pass"){
            "True"
        } else{
            pyobj.callAttr("main", solution, name, inputs1, outputs1, multiArgs.toString()).toString()
        }
        return if (obj == "True"){
            true
        } else{
            Toast.makeText(this, obj, Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun sendQuestion(description: String,  level: String, name_and_args: String, time: String, outputs: String, inputs: String, subject: String) {
        val withoutInput = (inputs == "")

        db.collection("questions").document("python").get().addOnSuccessListener {
            val data = it?.data!!

            var easy1 = data["easy"].toString()
            var medium1 = data["medium"].toString()
            var hard1 = data["hard"].toString()

            when(level){
                "easy" -> easy1 += "@$name_and_args& $description& $time"
                "medium" -> medium1 += "@$name_and_args& $description& $time"
                "hard" -> hard1 += "@$name_and_args& $description& $time"
            }

            var listNames = data["names"]

            listNames = when(level){
                "easy" -> (name_and_args.split("(")[0].replace("_", " ")).title() + "&${subject.title()}&${level.title()}@" + listNames
                "hard" -> listNames.toString() + "@" + (name_and_args.split("(")[0].replace("_", " ")).title() + "&${subject.title()}&${level.title()}"
                else -> {
                    val lst = listNames.toString().split("@").toMutableList()
                    val x = lst.indexOf("See the sun&Loops&Medium")

                    lst.add(x, (name_and_args.split("(")[0].replace("_", " ")).title() + "&${subject.title()}&${level.title()}")
                    lst.joinToString("@")
                }
            }

            var out = data["output"].toString()
            var inp = data["input"].toString()
            out = if (withoutInput){
                out.substring(0, out.length-1) + ",'${name_and_args.split("(")[0]}': ${outputs}}"
            } else{
                out.substring(0, out.length-1) + ",'${name_and_args.split("(")[0]}': [${outputs}]}"
            }
            inp = inp.substring(0, inp.length-1) + ",'${name_and_args.split("(")[0]}': [${inputs}]}"

            val hashmap = hashMapOf(
                "input" to inp,
                "output" to out,
                "names" to listNames,
                "easy" to easy1,
                "medium" to medium1,
                "hard" to hard1,
                "beginner" to data["beginner"],
                "Begginer_names" to data["Begginer_names"]
            )

            db.collection("questions").document("python").set(hashmap)

            val parentLayout = findViewById<View>(android.R.id.content)
            Snackbar.make(parentLayout, "The question was upload successfully!", Snackbar.LENGTH_LONG).show()
        }

        db.collection("questions").document("version").get().addOnSuccessListener{
            val data = it?.data!!
            val currentVersion = data["python"]
            val newVersion = currentVersion.toString().toInt()+1
            val versionMap = hashMapOf("python" to newVersion)
            db.collection("questions").document("version").set(versionMap)
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, HomeScreen::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun resetTextViews(textInputs: Array<EditText>){
        for (textView in textInputs){
            textView.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.clear_all, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        resetTextViews(arrayOf(nameAndArgs, level, subject, time, description, inputs, outputs, solution))
        return super.onOptionsItemSelected(item)
    }
}