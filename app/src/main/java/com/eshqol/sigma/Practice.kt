package com.eshqol.sigma
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView


class Practice : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)
        this.title = "Practice"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.practice

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val switchActivityIntent = Intent(this, HomeScreen::class.java)
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

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val namesString = sharedPreference.getString("namesString", "0")

        val names1 = namesString?.split("@")?.toTypedArray()!!
        val names = arrayListOf("0")

        if (namesString != "0"){
            for (i in names1){
                names.add(i)
            }
        }

        val recycle = findViewById<RecyclerView>(R.id.recyclerView)
        val myAdapter = QuetsionAdapter(this, names.toTypedArray(), read(), false)
        recycle.adapter = myAdapter
        recycle.layoutManager = LinearLayoutManager(this)
    }

    private fun read(): String {
        val sharedPref = getSharedPreferences("CheckBoxList", Context.MODE_PRIVATE)
        return sharedPref.getString("CheckBoxList", "").toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        val recycle = findViewById<RecyclerView>(R.id.recyclerView)

        val search = menu?.findItem(R.id.search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search by level, subject or name"
        searchView.maxWidth = Integer.MAX_VALUE

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                val newList = arrayListOf<String>()
                val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
                val names = sharedPreference.getString("namesString", "0")?.split("@")?.toTypedArray()!!

                for (item123 in names) if (p0.toString().lowercase() in item123.lowercase()){
                    newList.add(item123)
                }
                var search1 = false
                if (newList.size != names.size)
                    search1 = true

                val myAdapter1 = QuetsionAdapter(this@Practice, newList.toTypedArray(), read(), search1)
                recycle.adapter = myAdapter1
                recycle.layoutManager = LinearLayoutManager(this@Practice)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        try {
            val switchActivityIntent = Intent(this, HomeScreen::class.java)
            startActivity(switchActivityIntent)
            overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
            finish()
        }
        catch (_: Exception) { }
    }
}