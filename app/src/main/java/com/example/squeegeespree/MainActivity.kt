package com.example.squeegeespree

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val t = Toast.makeText(this@MainActivity, "Placeholder Button", Toast.LENGTH_SHORT)

        findViewById<Button>(R.id.playButton).setOnClickListener{
            startActivity(Intent(this@MainActivity, gamePlay::class.java))
        }
        findViewById<Button>(R.id.upGradeMenu).setOnClickListener {
            //Needs implementation
            t.show()
        }
        findViewById<Button>(R.id.statisticsMenu).setOnClickListener{
            //Needs implementation
            t.show()
        }
    }
}