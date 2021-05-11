package com.example.squeegeespree

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.sqrt

class gamePlay : AppCompatActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private lateinit var gLView: MyGLSurfaceView
    private var mAccelerometer : Sensor ?= null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                //findViewById<TextView>(R.id.touchTrack).text = "Finger Position:\n X: "+event.x.toString()+"\nY: "+event.y.toString()
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
        var xVal: Int
        var yVal: Int
        var t = Toast.makeText(this@gamePlay, "Jerk Detected", Toast.LENGTH_SHORT)
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                Log.i("gamePlay", "Accelerometer activity detected")
                /*
                vSize = (event.values[0]*event.values[0])+(event.values[1]*event.values[1])+(event.values[2]*event.values[2])
                vSize = sqrt(vSize)
                findViewById<TextView>(R.id.AccelRead).text = "Accelerometer reads: \n" + "x: "+event.values[0].toString()+"\ny: " + event.values[1] + "\nz: " + event.values[2]+"\nVector size: "+vSize.toString()
                diff = vSize-vSizeOld
                if (abs(diff) > 2 ) {
                    t.show()
                }
                vSizeOld=vSize
                */

                if (event.values[0] > 0) xVal = -1
                else xVal = 1
                if (event.values[1] > 0) yVal = -1
                else yVal = 1
                gLView.upDate(xVal, yVal)

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
        //setContentView(R.layout.activity_game_play)

        //Setup Accelerometer
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //Setup GLSurfaceView
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)



    }
}
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    fun upDate(x:Int, y:Int) {
        renderer.upDateSquare(x,y)
        requestRender()
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var mySquare: Square
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mySquare = Square()
    }
    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        mySquare.draw()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()

        //Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
    fun upDateSquare(x:Int, y:Int) {
        mySquare.upDatePos(x, y)
    }
}
class Square {
    val color = floatArrayOf(0.0f, 0.25f, 0.75f, 1.0f)
    private val drawOrder = shortArrayOf(0,1,2,0,2,3)
    private var squareCoords=floatArrayOf(
            -0.25f,  0.25f, 0.0f,
            -0.25f, -0.25f, 0.0f,
             0.25f, -0.25f, 0.0f,
             0.25f,  0.25f, 0.0f,
    )
    private var vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(squareCoords.size * 4).run{
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply{
                    put(squareCoords)
                    position(0)
                }
            }
    private val drawListBuffer: ShortBuffer =
            ByteBuffer.allocateDirect(drawOrder.size*2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply{
                    put(drawOrder)
                    position(0)

                }

            }
    private val vertexShaderCode = "attribute vec4 vPosition;" + "void main() {" + " gl_Position =vPosition;" + "}"
    private val fragmentShaderCode = "precision medium float;" + "uniform vec4 vColor;" + "void main() {" + " gl_FragColor = vColor;" + "}"
    private var mProgram:Int

    init {
        val myRenderer = MyGLRenderer()
        val vertexShader: Int = myRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = myRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also{
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

        }

    }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = squareCoords.size / 3
    private val vertexStride: Int = 12
    private var vPMatrixHandle: Int = 0

    fun draw() {
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                    it,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    vertexStride,
                    vertexBuffer
            )
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle->
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }
            //vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrx")
            //GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
            GLES20.glDisableVertexAttribArray(it)

        }
    }
    fun upDatePos(x:Int, y:Int) {
        Log.i("SquareUpdate", "Square Update Function running")
        squareCoords[0] = squareCoords[0]+(x*.01f)
        Log.i("SquareUpdate", squareCoords[0].toString())
        squareCoords[1] = squareCoords[1]+(y*.01f)
        squareCoords[3] = squareCoords[3]+(x*.01f)
        squareCoords[4] = squareCoords[4]+(y*.01f)
        squareCoords[6] = squareCoords[6]+(x*.01f)
        squareCoords[7] = squareCoords[7]+(y*.01f)
        squareCoords[9] = squareCoords[9]+(x*.01f)
        squareCoords[10] = squareCoords[10]+(y*.01f)
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.size * 4).run{
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply{
                put(squareCoords)
                position(0)
            }
        }
    }
}
