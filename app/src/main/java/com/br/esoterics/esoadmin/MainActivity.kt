package com.br.esoterics.esoadmin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        startButton.setOnClickListener {
            var intent2 = Intent(this, MapActivity::class.java)
            startActivity(intent2)
        }

    }



}
