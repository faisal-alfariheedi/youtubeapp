package com.example.youtubeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer



class RVAdapter(private val rv: ArrayList<vid>, private val player: YouTubePlayer): RecyclerView.Adapter<RVAdapter.ItemViewHolder>()  {
    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVAdapter.ItemViewHolder {
        return RVAdapter.ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rvlist,parent,false )
        )
    }

    override fun onBindViewHolder(holder: RVAdapter.ItemViewHolder, position: Int) {
        val rvv = rv[position].items!![0].snippet?.title
        holder.itemView.apply {
            var rvlisting = findViewById<CardView>(R.id.rvlisting)
            var ct = findViewById<TextView>(R.id.cardtitle)
            var title = findViewById<TextView>(R.id.tvtitle)
            ct.text = rvv.toString()
            rvlisting.setOnClickListener {
                player.loadVideo(rv[position].items!![0].id!!, 0f)
            }


        }
    }

    override fun getItemCount() = rv.size
}