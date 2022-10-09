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
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ncorti.slidetoact.SlideToActView
import com.ncorti.slidetoact.SlideToActView.OnSlideCompleteListener


class NewQuestion : AppCompatActivity() {

    private val username = Helpers().getUsername()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_question)

        val titleQuestion = findViewById<TextView>(R.id.title1)
        val titleSolution = findViewById<TextView>(R.id.textView34)

        val question = findViewById<EditText>(R.id.question)
        val solution = findViewById<EditText>(R.id.question2)

        val send = findViewById<SlideToActView>(R.id.slideButton)
        send.bumpVibration = 50

        val exit = findViewById<ImageView>(R.id.exit)
        exit.setOnClickListener{
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        }
        send.isLocked = true

        question.doOnTextChanged { text, _, _, _ ->
            when {
                text.toString() == "" -> {
                    titleQuestion.alpha = 1f
                    send.isLocked = true
                }
                text.toString() == "eshqol" -> send.isLocked = false
                else -> {
                    titleQuestion.alpha = 0f
                    send.isLocked = (solution.text.toString() == "" || question.text.toString() == "")
                }
            }
        }

        solution.doOnTextChanged { text, _, _, _ ->
            try{
                if (text.toString() == "") {
                    titleSolution.alpha = 1f
                    send.isLocked = true
                }
                else {
                    titleSolution.alpha = 0f
                    send.isLocked = (solution.text.toString() == "" || question.text.toString() == "")
                }
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

        send.onSlideCompleteListener =
            object : OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    sendQuestion(question.text.toString(), solution.text.toString())
                }
            }
    }

    fun sendQuestion(question: String, solution: String) {
        if (question == "eshqol"){
            val intent = Intent(this, SubmitQuestion::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val map = HashMap<String, String>()
            map["question${(0..1000).random()}"] = "$question  @  $solution"
            val ref = db.collection("propose_question").document(username)
            ref.update("question${(0..1000).random()}", "$question  @  $solution").addOnFailureListener {
                ref.set(map)
            }
            Handler(mainLooper).postDelayed( {
                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra("snack", "Thanks for proposing new questions and helping us improve the game.")
                startActivity(intent)
                finish()
            }, 800)
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

    override fun onBackPressed() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
        finish()
    }
}