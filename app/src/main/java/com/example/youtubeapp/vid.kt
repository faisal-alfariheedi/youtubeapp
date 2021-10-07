package com.example.youtubeapp

import com.google.gson.annotations.SerializedName


//      the model for the json to read video title and id
class vid(id: String, title: String) {
    @SerializedName("items")
    var items:Array<d>?=null
    class d(id: String, snippet: String){
        @SerializedName("id")
        var id:String=""
        @SerializedName("snippet")
        var snippet: Snip? =null

        init {
            this.id = id
            Snip(snippet)
        }

        class Snip(title: String){
            @SerializedName("title")
            var title:String = ""

            init {
                this.title = title
            }

        }
    }


    init {
        var a=d(id,title)
        items=arrayOf(a)
    }
}