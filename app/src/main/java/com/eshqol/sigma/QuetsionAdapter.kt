package com.eshqol.sigma
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class QuetsionAdapter(var context: Context, val data: Array<String>, check: String, private val search: Boolean) :
    RecyclerView.Adapter<QuetsionAdapter.MyViewHolder>() {
    private var storage = check

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.activity_quetsions, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position==0 && !search){
            holder.free.text = "Free Code - Editor / interpreter"
            holder.name.text = ""
            holder.level.text = ""
            holder.subject.text = ""
            holder.image.visibility = View.INVISIBLE

            holder.itemView.setOnClickListener {
                val intent = Intent(context, FreeCoding::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }

        } else {
            holder.image.visibility = View.VISIBLE

            holder.free.text = ""
            val dat = data[position].split('&')
            val name = dat[0]
            holder.name.text = name.replace("_", " ")

            val multiArguments = "$" in dat[1]

            holder.subject.text = dat[1].replace("$", "")
            val level = dat[2]
            holder.level.text = level
            val functionName = name.replace(' ', '_').lowercase()
            when (level) {
                "Beginner" -> holder.level.setTextColor(Color.parseColor("#00AAFF"))
                "Easy" -> holder.level.setTextColor(Color.parseColor("#00DE00"))
                "Medium" -> holder.level.setTextColor(Color.parseColor("#FF8040"))
                "Hard" -> holder.level.setTextColor(Color.parseColor("#E42217"))
            }


            if (level == "Beginner"){
                holder.image.setImageResource(R.drawable.trivia)
            }
            else{
                if (functionName in storage.split('-')) {
                    holder.image.setImageResource(R.drawable.ic_baseline_check_box_24)
                } else {
                    holder.image.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
                }
            }
            holder.itemView.setOnClickListener {
                startPractice(functionName, multiArguments, level)
            }

        }

    }

    override fun getItemCount() : Int {
        return data.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.question_name)
        val free: TextView = itemView.findViewById(R.id.freeCode)
        val level: TextView = itemView.findViewById(R.id.question_level)
        val subject: TextView = itemView.findViewById(R.id.question_subject)
        val image: ImageView = itemView.findViewById(R.id.check)
    }

    private fun startPractice(functionName: String, multiArguments: Boolean, level: String){
        if (level != "Beginner") {
            val intent = Intent(context, MainActivityPractice::class.java)
            intent.putExtra("level", level.lowercase())
            intent.putExtra("tic", System.currentTimeMillis())
            intent.putExtra("functionName", functionName)
            intent.putExtra("multiArguments", multiArguments.toString())
            context.startActivity(intent)
            (context as Activity).finish()
        } else {
            val intent = Intent(context, Beginner::class.java)
            intent.putExtra("functionName", functionName.replace("_", " "))
            context.startActivity(intent)
            (context as Activity).finish()
        }
    }
}