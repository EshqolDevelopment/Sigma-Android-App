package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread


class FreeCoding : AppCompatActivity() {

    private var last = 0

    private fun refreshColorOnStart() {
        val editText = findViewById<EditText>(R.id.functionText)
        last = 0
        var txt = editText.text.toString()
        if (txt.last() != ' ')
            txt += " "

        val spannable = SpannableString(txt)
        val orangeWords = Helpers().orangeWords
        val purpleWords = Helpers().purpleWords
        val numbers = Helpers().numbers
        val functionNames = arrayListOf<String>()
        var keepGreen = false
        var keepGreen1 = false

        try {
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
                        if (txt.substring(i, i + words.length) == words) {
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

            if (txt.contains('\'')) {
                for (i in txt.indices) {
                    if (txt[i] == '\'') {
                        keepGreen1 = !keepGreen1
                    }
                    if (keepGreen1) {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.parseColor("#00FF00")),
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
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_coding)

        val editText = findViewById<EditText>(R.id.functionText)
        val quit = findViewById<ImageView>(R.id.quit)
        this.title = "Editor / interpreter"

        val textView = findViewById<TextView>(R.id.textView)
        registerForContextMenu(textView)

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

        quit.setOnClickListener {
            try {
                val switchActivityIntent = Intent(this, Practice::class.java)
                startActivity(switchActivityIntent)
                finish()

            } catch (_: Exception) { }
        }

        loadCode()

        refreshColorOnStart()

        val help = Helpers()
        help.currentLanguage = "Python"
        help.refreshColors(editText)


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

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
    }

    @SuppressLint("SetTextI18n")
    fun functionCheck(view: View) {
        try {
            val editText = findViewById<EditText>(R.id.functionText)
            val textView = findViewById<TextView>(R.id.textView)
            textView.movementMethod = ScrollingMovementMethod()

            Helpers().closeKeyboard(this)

            textView.text = "processing..."

            thread {
                val py = Python.getInstance()
                val pyobj = py.getModule("FreeCode")

                val obj = try {
                    pyobj.callAttr("run", editText.text.toString()).toString()
                } catch (_: Exception){
                    "An error occurred"
                }
                runOnUiThread {
                    textView.visibility = View.VISIBLE
                    textView.text = "$obj\nProcess finished"
                }
            }
        }catch (_: Exception){}

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

    private var backPressOnce = false
    override fun onBackPressed() {
        if (!backPressOnce){
            "Click the back button again to exit".show(this)
            backPressOnce = true
        }
        else{
            try {
                val switchActivityIntent = Intent(this, Practice::class.java)
                startActivity(switchActivityIntent)
                finish()
            } catch (_: Exception) { }
        }
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

    @Suppress("DEPRECATION")
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(20)

        val yourTextView = v as TextView
        if (yourTextView.text.length < 5000){
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copy", yourTextView.text)
            clipboard.setPrimaryClip(clip)
            Snackbar.make(v, "Copied successfully", Snackbar.LENGTH_SHORT).show()
        }else{
            Snackbar.make(v, "The text is too long for copying", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveCode() {
        val editText = findViewById<EditText>(R.id.functionText)
        val sharedPreferences = getSharedPreferences("code", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("code", editText.text.toString())
        editor.apply()

        Helpers().closeKeyboard(this)

        // show a snackbar to notify the user that the code is saved
        "Code saved successfully".snack()
    }

    private fun loadCode() {
        val editText = findViewById<EditText>(R.id.functionText)
        val sharedPreferences = getSharedPreferences("code", Context.MODE_PRIVATE)
        val code = sharedPreferences.getString("code", "print(\"Hello World\")")
        editText.setText(code)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.free_code, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.toString()){
            "Save" -> {
                saveCode()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}