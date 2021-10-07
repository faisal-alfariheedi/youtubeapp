package com.example.youtubeapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Color.parseColor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.*
import java.net.URL
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import kotlinx.coroutines.Dispatchers.IO


class MainActivity : AppCompatActivity() {
    // the variables
    lateinit var ytvp: YouTubePlayerView
    lateinit var rv: RecyclerView
    var vdidlist: ArrayList<vid> = arrayListOf() // video list
    lateinit var player: YouTubePlayer
    lateinit var add: FloatingActionButton // floating button to add video to the list
    private lateinit var sp: SharedPreferences // this feature won`t be enabled because of a bug so add videos from user won`t be saved
    var a =this
    var cur:String="dQw4w9WgXcQ" //default video start for the app
    var time:Float=0f // this doesn`t work wasn`t able to find a way to get the time
    var f:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv=findViewById(R.id.rvmain)
        ci()
        ytvpsetup()

        // i hade problems with coroutine so the video loading is running on main thread
        val policy = ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)

        add.setOnClickListener{
            showAlertDialog("enter video`s url")
        }
        if(f) {
            saverestore(true)
        }
    }

    fun ytvpsetup(){
        add=findViewById(R.id.add)
        ytvp = findViewById(R.id.ytpv)
        lifecycle.addObserver(ytvp)
        ytvp.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = cur
                player=youTubePlayer
                f=CoroutineScope(Dispatchers.IO).launch{
                    player.loadVideo(cur, time)
                }.isCompleted
                findViewById<TextView>(R.id.tvtitle).text = getvidname("dQw4w9WgXcQ").items!![0].snippet?.title.toString()
                rvinit()
            }
            override fun onVideoId(youTubePlayer: YouTubePlayer,  videoId: String) {
                for (i in vdidlist){
                    if(i.items!![0].id==videoId)
                        findViewById<TextView>(R.id.tvtitle).text=i.items!![0].snippet?.title.toString()
                }
            }
            override fun onCurrentSecond(youTube:YouTubePlayer, seconds:Float){
                time=seconds
            }

        })
    }
    fun rvinit(){

        if (vdidlist.isEmpty()) {
            vdidlist.add(getvidname("dQw4w9WgXcQ"))
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter=RVAdapter(vdidlist,player)

    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ytvp.enterFullScreen()
            if (supportActionBar != null) {
                supportActionBar?.hide()
            }
            add.isVisible=false
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ytvp.exitFullScreen()
                if (supportActionBar != null) {
                    supportActionBar?.show()
                }
            add.isVisible = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(vdidlist.isNotEmpty()){
        for (i in vdidlist){
            if(findViewById<TextView>(R.id.tvtitle).text.equals(i.items!![0].snippet?.title))
                cur= i.items!![0].id.toString()
        }}
        outState.putString("currentVideo", cur)
        outState.putFloat("timeStamp", time)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        cur = savedInstanceState.getString("currentVideo").toString()
        time = savedInstanceState.getFloat("timeStamp", 0f)
        ytvpsetup()

    }

     fun getvidname(id:String): vid {
         var d: vid
         d=vid("dQw4w9WgXcQ","rick")

             var jasonread: GsonBuilder = GsonBuilder()
             jasonread.setLenient()
             var a = jasonread.create()
             val data = a.fromJson(URL("https://www.googleapis.com/youtube/v3/videos?id=${id}&key=AIzaSyCf-Af-SNAsqCjRSFMo9N_I5h8GHtwN0BM&fields=items(id,snippet(channelId,title,categoryId),statistics)&part=snippet,statistics").readText(Charsets.UTF_8), vid::class.java)
             d = data
         return d
     }




    private fun ci(){
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

        if(!(activeNetwork?.isConnectedOrConnecting==true)){
            AlertDialog.Builder(this)
                .setTitle("Internet Connection Not Found")
                .setPositiveButton("RETRY"){_, _ -> ci()}
                .show()
        }
    }


    fun showAlertDialog(title: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val input= EditText(this)

        dialogBuilder.setCancelable(false)
        dialogBuilder.setPositiveButton("ADD", DialogInterface.OnClickListener { dialog, id ->
            if(input.text.isNotBlank()) {
                CoroutineScope(Dispatchers.Main).launch {
                    async {
                        vdidlist.add(urlconverter(input.text.toString()))
                        saverestore(false)
                    }.await()


                }
                rv.adapter?.notifyDataSetChanged()
            }

        }
        )
            // negative button text and action
            .setNegativeButton("No", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("add video URL")
        alert.setView(input)
        alert.show()
    }
    fun urlconverter(url:String): vid {
        var urll:String=""
        var boo:Boolean=false
        for (i in url){
            if(boo){
                urll+=i
            }
            if(i == '='){
                boo=true
            }


        }
        return getvidname(urll)
    }


    private fun saverestore(s:Boolean) {
        var size: Int
        sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        size = sp.getInt("size", 0)
        if (s) {
            if (size != 0) {
                for (i in 0..size) {
                    var idd=sp.getString("vd $i 1", "NN")
                    var tit=sp.getString("vd $i 2", "LL")
                    var obj=vid(idd!!,tit!!)
                    obj.items!![0].snippet?.title=tit
                    vdidlist.add(obj)

                }

            }
            rv.adapter?.notifyDataSetChanged()
        } else {
            if (vdidlist.isNotEmpty()) {
                for (i in 0 until vdidlist.size) {
                    with(sp.edit()) {
                        Log.d("items size", "${vdidlist[i].items?.size}")
                        putString("vd $i 2", vdidlist[i].items!![0].snippet?.title)
                        putString("vd $i 1", vdidlist[i].items!![0].id)
                        apply()
                    }
                }
            }
            with(sp.edit()) {
                putInt("size", vdidlist.size - 1)
                apply()
            }
        }
    }


}