package com.example.squeegeespree

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.sqrt

class gamePlay : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer : Sensor ?= null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                findViewById<TextView>(R.id.touchTrack).text = "Finger Position:\n X: "+event.x.toString()+"\nY: "+event.y.toString()
            }
        }
        return true
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var vSize: Float
        var vSizeOld = 10.0F
        var diff: Float
        var t = Toast.makeText(this@gamePlay, "Jerk Detected", Toast.LENGTH_SHORT)
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                vSize = (event.values[0]*event.values[0])+(event.values[1]*event.values[1])+(event.values[2]*event.values[2])
                vSize = sqrt(vSize)
                findViewById<TextView>(R.id.AccelRead).text = "Accelerometer reads: \n" + "x: "+event.values[0].toString()+"\ny: " + event.values[1] + "\nz: " + event.values[2]+"\nVector size: "+vSize.toString()
                diff = vSize-vSizeOld
                if (abs(diff) > 2 ) {
                    t.show()
                }
                vSizeOld=vSize
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_play)

        //Setup Accelerometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
}