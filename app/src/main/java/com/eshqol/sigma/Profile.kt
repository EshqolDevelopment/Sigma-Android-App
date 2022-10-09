package com.eshqol.sigma
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Profile : AppCompatActivity() {
    private val db = Firebase.firestore
    private var enemy = false

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val coins = findViewById<TextView>(R.id.coins)
        val win = findViewById<TextView>(R.id.victories)
        val lose = findViewById<TextView>(R.id.defeats)
        val winRate = findViewById<TextView>(R.id.rate3)
        val proImg = findViewById<ImageView>(R.id.imgProfile)
        val point1 = findViewById<TextView>(R.id.points1)
        val exitProfile = findViewById<ImageView>(R.id.exitProfile)

        var wins = 0
        var total = 0
        val userName = intent.getStringExtra("username").toString()
        val profileId = intent.getStringExtra("id").toString()
        enemy = intent.getBooleanExtra("enemy", false)
        if (enemy) this.title = userName.replace("_", "")
        else this.title = "Profile"

        if (enemy){
            val root = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            root.setBackgroundResource(R.color.Brown_Bear)

            val pen = findViewById<ImageView>(R.id.imageView36)
            pen.alpha = 0f
        }

        proImg.setProfileImage(profileId)

        db.collection("root").document(userName).get(Source.CACHE).addOnSuccessListener { document ->
            val data = document.data

            coins.text = data!!["0"].toString()
            point1.text = data["a"].toString()

            val x = data["1"].toString()
            win.text = x
            wins += x.toInt()
            total += x.toInt()
            val y = data["2"].toString()
            lose.text = y
            total += y.toInt()

            total += data["3"].toString().toInt()
            if (total != 0){
                winRate.text = ((wins.toFloat()/total.toFloat())*100).toInt().toString() + " %"
            }
            else{
                winRate.text = "0 %"
            }
        }

        db.collection("root").document(userName).get().addOnSuccessListener { document ->
            val data = document.data

            coins.text = data!!["0"].toString()
            point1.text = data["a"].toString()

            val x = data["1"].toString()
            win.text = x
            wins += x.toInt()
            total += x.toInt()
            val y = data["2"].toString()
            lose.text = y
            total += y.toInt()

            total += data["3"].toString().toInt()
            if (total != 0){
                winRate.text = ((wins.toFloat()/total.toFloat())*100).toInt().toString() + " %"
            }
            else{
                winRate.text = "0 %"
            }
        }

        proImg.setOnClickListener{
            if (!enemy){
                val intent = Intent(this, ImagePicker1::class.java)
                intent.putExtra("username", userName)
                intent.putExtra("id", profileId)
                intent.putExtra("coins", coins.text)
                intent.putExtra("wins", win.text)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            }
            else{
                Toast.makeText(this, userName.replace("_", ""), Toast.LENGTH_SHORT).show()
            }
        }

        exitProfile.setOnClickListener{
            if (!enemy){
                val intent = Intent(this, HomeScreen::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            } else {
                val intent = Intent(this, Leaderboard::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            }
        }

        if (!enemy){
            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

            val bestEasyTime = sharedPreference.getFloat("bestEasyTime1", -1F)
            val bestMediumTime = sharedPreference.getFloat("bestMediumTime1", -1F)
            val bestHardTime = sharedPreference.getFloat("bestHardTime1", -1F)

            val easyText = if (bestEasyTime != -1F) "$bestEasyTime seconds" else "No recording yet"
            val mediumText = if (bestMediumTime != -1F) "$bestMediumTime seconds" else "No recording yet"
            val hardText = if (bestHardTime != -1F) "$bestHardTime seconds" else "No recording yet"

            val help = Helpers()

            val records = findViewById<ImageView>(R.id.imageView39)
            records.setOnClickListener {
                val popUpInfo = help.popUp(this, R.layout.pop_up_records, it)
                if (popUpInfo != null){
                    val (popupView, popupWindow) = popUpInfo

                    val close = popupView.findViewById<View>(R.id.imageView9) as ImageView
                    val easyTime = popupView.findViewById<View>(R.id.textView57) as TextView
                    val mediumTime = popupView.findViewById<View>(R.id.textView58) as TextView
                    val hardTime = popupView.findViewById<View>(R.id.textView56) as TextView

                    easyTime.text = easyText
                    mediumTime.text = mediumText
                    hardTime.text = hardText

                    close.setOnClickListener {
                        popupWindow.dismiss()
                    }
                }
            }
        }else{
            val records = findViewById<ImageView>(R.id.imageView39)
            records.visibility = View.INVISIBLE
        }
    }


    override fun onBackPressed() {
        try {
            if (!enemy){
                val intent = Intent(this, HomeScreen::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            } else {
                val intent = Intent(this, Leaderboard::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
                finish()
            }
        } catch (_: Exception) { }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!enemy){
            val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)

            menuInflater.inflate(R.menu.edit_buttons, menu)
            menu?.getItem(0)?.setIcon(getIconPreference(sharedPreference.getString("1", "tab").toString()))
            menu?.getItem(1)?.setIcon(getIconPreference(sharedPreference.getString("2", "equal").toString()))
            menu?.getItem(2)?.setIcon(getIconPreference(sharedPreference.getString("3", "plus").toString()))
            menu?.getItem(3)?.setIcon(getIconPreference(sharedPreference.getString("4", "minus").toString()))
            menu?.getItem(4)?.setIcon(getIconPreference(sharedPreference.getString("5", "squareBrackets").toString()))
        }
        return super.onCreateOptionsMenu(menu)
    }

    private var lastPopUp = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (System.nanoTime()-lastPopUp < 1000000000L) {
            return true
        }
        lastPopUp = System.nanoTime()

        val metrics: DisplayMetrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val popWidth = (width.toFloat()*(1000f/1080f)).toInt()
        val popHeight = (height.toFloat()*(800f/2120f)).toInt()

        val view = findViewById<View>(android.R.id.content)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.pop_up_choose_new_shortcut, null)
        val popupWindow = PopupWindow(popupView,  popWidth, popHeight, true)
        popupWindow.animationStyle = R.style.Animation

        val index = item.toString()

        try {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            popupWindow.isOutsideTouchable = true
            popupWindow.isTouchable = true
            popupWindow.setTouchInterceptor(View.OnTouchListener { view1, motionEvent ->
                if (motionEvent.x < 0 || motionEvent.x > view1.width) return@OnTouchListener true
                motionEvent.y < 0 || motionEvent.y > view1.height
            })

            val close = popupView.findViewById<View>(R.id.imageView9) as ImageView

            val semiColon = popupView.findViewById<View>(R.id.semiColon) as ImageView
            val para = popupView.findViewById<View>(R.id.para) as ImageView
            val merchaot = popupView.findViewById<View>(R.id.merchaot) as ImageView
            val forLoop = popupView.findViewById<View>(R.id.forLoop) as ImageView
            val plus = popupView.findViewById<View>(R.id.plus) as ImageView
            val minus = popupView.findViewById<View>(R.id.minus) as ImageView
            val equal = popupView.findViewById<View>(R.id.equal) as ImageView
            val squareBrackets = popupView.findViewById<View>(R.id.squareBrackets) as ImageView
            val tab = popupView.findViewById<View>(R.id.tab) as ImageView
            val returnIco = popupView.findViewById<View>(R.id.returnIco) as ImageView

            semiColon.setOnClickListener {
                updateButton("semiColon", index)

                item.setIcon(getIconPreference("semiColon"))
                popupWindow.dismiss()
            }
            para.setOnClickListener {
                  updateButton("para", index)
                item.setIcon(getIconPreference("para"))
                popupWindow.dismiss()
            }
            merchaot.setOnClickListener {
                updateButton("merchaot", index)
                item.setIcon(getIconPreference("merchaot"))
                popupWindow.dismiss()

            }
            forLoop.setOnClickListener {
                updateButton("forLoop", index)
                item.setIcon(getIconPreference("forLoop"))
                popupWindow.dismiss()
            }
            plus.setOnClickListener {
                updateButton("plus", index)
                item.setIcon(getIconPreference("plus"))
                popupWindow.dismiss()

            }
            minus.setOnClickListener {
                updateButton("minus", index)
                item.setIcon(getIconPreference("minus"))
                popupWindow.dismiss()

            }
            squareBrackets.setOnClickListener {
                updateButton("squareBrackets", index)
                item.setIcon(getIconPreference("squareBrackets"))
                popupWindow.dismiss()

            }
            tab.setOnClickListener {
                updateButton("tab", index)
                item.setIcon(getIconPreference("tab"))
                popupWindow.dismiss()

            }
            returnIco.setOnClickListener {
                updateButton("returnIco", index)
                item.setIcon(getIconPreference("returnIco"))
                popupWindow.dismiss()
            }
            equal.setOnClickListener {
                updateButton("equal", index)
                item.setIcon(getIconPreference("equal"))
                popupWindow.dismiss()
            }
            
            close.setOnClickListener {
                popupWindow.dismiss()
            }


        }catch (_: Exception){}
        return super.onOptionsItemSelected(item)
    }

    private fun updateButton(button: String, index: String){
        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        editor.putString(index, button)
        editor.apply()
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

}