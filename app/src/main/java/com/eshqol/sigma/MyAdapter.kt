package com.eshqol.sigma
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MyAdapter(var context: Context, val data: ArrayList<String>, private val data1: ArrayList<String>, private val data3: ArrayList<String>, private val country: ArrayList<String>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.activity_table, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pos = (position+1).toString()
        when (position){
            0 -> holder.medal.setImageResource(R.drawable.gold_medal)
            1 -> holder.medal.setImageResource(R.drawable.silver_medal)
            2 -> holder.medal.setImageResource(R.drawable.bronze_medal)
            else -> {
                holder.medal.setImageResource(R.drawable.trnasperent)
            }
        }

        if (data[position] == Helpers().getUsername()) {
            holder.player.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            holder.itemView.setBackgroundColor(Color.parseColor("#654321"))
        }
        else{
            holder.player.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            holder.itemView.setBackgroundColor(Color.parseColor("#835C3B"))
        }


        holder.player.text = data[position].replace("_", "")

        holder.pos.text = "#$pos"
        holder.score.text = data1[position]
        holder.profileImg.setProfileImage(data3[position])

        holder.countryImage.setFlag(country[position].lowercase())

        holder.profileImg.setOnClickListener {
            openEnemyProfile(position)
        }
        holder.player.setOnClickListener {
            openEnemyProfile(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun openEnemyProfile(position: Int){
        val playerName = data[position]
        val profileId = data3[position]
        val intent = Intent(context, Profile::class.java)
        intent.putExtra("username", playerName)
        intent.putExtra("x", "0")
        intent.putExtra("id", profileId)
        intent.putExtra("enemy", true)
        intent.putExtra("position", position)
        context.startActivity(intent)
        (context as Activity).overridePendingTransition(R.anim.activity_show, R.anim.activity_hide)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val player: TextView = itemView.findViewById(R.id.textView123)
        val pos: TextView = itemView.findViewById(R.id.pos_rank)
        val score: TextView = itemView.findViewById(R.id.score_rank)
        val profileImg: ImageView = itemView.findViewById(R.id.profile_image4)
        val medal: ImageView = itemView.findViewById(R.id.medals)
        val countryImage: ImageView = itemView.findViewById(R.id.county_flag)
    }
}