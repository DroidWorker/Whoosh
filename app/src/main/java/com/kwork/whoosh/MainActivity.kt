package com.kwork.whoosh

import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import android.graphics.Point
import android.graphics.PointF
import android.util.Log
import android.view.Display
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {
    var width: Int = 0
    var height: Int = 0
    var keypressed : Boolean = false

    var lastPoint : PointF = PointF(0f,0f)

    var sudMenuActive : Boolean = true
    var soundPool : SoundPool? = null
    var streamID : Int? = null
    var currSUDmenuItemID : Int = 5
    var selectedSUDnum = 0

    var setNegativePointActive : Boolean = false
    var rotateAngle = -90f
    var negativeValue = 0f//0-360

    var setResourcePointActive : Boolean = false
    var resourceValue = 0f//0-360

    var partTwoActive : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y

        val sudLL : LinearLayout = findViewById(R.id.sudMenu)
        sudLL.visibility = View.VISIBLE
        soundPool = SoundPool(7, AudioManager.STREAM_MUSIC, 100)


    }

    override fun onResume() {
        soundPool!!.load(this, R.raw.sud, 1)
        streamID = soundPool!!.play(1, 100f, 100f, 1, 0, 1f)
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!keypressed) {
            val rl: RelativeLayout = findViewById(R.id.root)
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (sudMenuActive) {
                        currSUDmenuItemID--
                        if (currSUDmenuItemID < 1)
                            currSUDmenuItemID = 10
                        sudItemChange()
                    }
                    else if (setNegativePointActive){
                        GlobalScope.launch { translateLeft() }
                    }
                    else if (setResourcePointActive){
                        GlobalScope.launch { translateRight() }
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (sudMenuActive) {
                        currSUDmenuItemID++
                        if (currSUDmenuItemID > 10)
                            currSUDmenuItemID = 1
                        sudItemChange()
                    }
                    else if (setNegativePointActive){
                        GlobalScope.launch { translateLeft() }
                    }
                    else if (setResourcePointActive){
                        GlobalScope.launch { translateRight() }
                    }
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (setNegativePointActive){
                        GlobalScope.launch { translateLeft() }
                    }
                    else if (setResourcePointActive){
                        GlobalScope.launch { translateRight() }
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (setNegativePointActive){
                        GlobalScope.launch { translateLeft() }
                    }
                    else if (setResourcePointActive){
                        GlobalScope.launch { translateRight() }
                    }
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A -> {
                    if (sudMenuActive) {
                        selectedSUDnum = currSUDmenuItemID
                        soundPool?.stop(streamID!!)
                        val sudLL : LinearLayout = findViewById(R.id.sudMenu)
                        sudLL.visibility = View.GONE
                        sudMenuActive = false
                        setNegative()
                    }
                    else if(setNegativePointActive){
                        negativeValue = rotateAngle
                        setNegativePointActive = false
                        setResource()
                    }
                    else if(setResourcePointActive) {
                        resourceValue = rotateAngle
                        val clock: ImageView = findViewById(R.id.imageView)
                        var duration =
                            (1000 * (Math.abs(resourceValue) + Math.abs(negativeValue))) / 45
                        var animation = RotateAnimation(
                            rotateAngle,
                            negativeValue,
                            (clock.width / 2).toFloat(),
                            (clock.height / 2).toFloat()
                        )
                        animation.fillAfter = true
                        animation.duration = (duration).toLong()
                        animation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationRepeat(animation: Animation?) {}
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                partTwo()
                                setResourcePointActive = false
                            }
                        })
                        clock.startAnimation(animation)
                    }
                }
                KeyEvent.KEYCODE_BACK -> {

                }
            }
        }
        keypressed = true
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        keypressed = false
        return super.onKeyUp(keyCode, event)
    }

    private fun sudItemChange(){
        val tv1 : TextView = findViewById(R.id.textView)
        val tv2 : TextView = findViewById(R.id.textView2)
        val tv3 : TextView = findViewById(R.id.textView3)
        val tv4 : TextView = findViewById(R.id.textView4)
        val tv5 : TextView = findViewById(R.id.textView5)
        val tv6 : TextView = findViewById(R.id.textView6)
        val tv7 : TextView = findViewById(R.id.textView7)
        val tv8 : TextView = findViewById(R.id.textView8)
        val tv9 : TextView = findViewById(R.id.textView9)
        val tv10 : TextView = findViewById(R.id.textView10)
        when(currSUDmenuItemID){
            1->{
                tv10.setBackgroundColor(Color.WHITE)
                tv1.setBackgroundColor(Color.rgb(62,62,62))
                tv2.setBackgroundColor(Color.WHITE)
            }
            2->{
                tv1.setBackgroundColor(Color.WHITE)
                tv2.setBackgroundColor(Color.rgb(62,62,62))
                tv3.setBackgroundColor(Color.WHITE)
            }
            3->{
                tv2.setBackgroundColor(Color.WHITE)
                tv3.setBackgroundColor(Color.rgb(62,62,62))
                tv4.setBackgroundColor(Color.WHITE)
            }
            4->{
                tv3.setBackgroundColor(Color.WHITE)
                tv4.setBackgroundColor(Color.rgb(62,62,62))
                tv5.setBackgroundColor(Color.WHITE)
            }
            5->{
                tv4.setBackgroundColor(Color.WHITE)
                tv5.setBackgroundColor(Color.rgb(62,62,62))
                tv6.setBackgroundColor(Color.WHITE)
            }
            6->{
                tv5.setBackgroundColor(Color.WHITE)
                tv6.setBackgroundColor(Color.rgb(62,62,62))
                tv7.setBackgroundColor(Color.WHITE)
            }
            7->{
                tv6.setBackgroundColor(Color.WHITE)
                tv7.setBackgroundColor(Color.rgb(62,62,62))
                tv8.setBackgroundColor(Color.WHITE)
            }
            8->{
                tv7.setBackgroundColor(Color.WHITE)
                tv8.setBackgroundColor(Color.rgb(62,62,62))
                tv9.setBackgroundColor(Color.WHITE)
            }
            9->{
                tv8.setBackgroundColor(Color.WHITE)
                tv9.setBackgroundColor(Color.rgb(62,62,62))
                tv10.setBackgroundColor(Color.WHITE)
            }
            10->{
                tv9.setBackgroundColor(Color.WHITE)
                tv10.setBackgroundColor(Color.rgb(62,62,62))
                tv1.setBackgroundColor(Color.WHITE)
            }
        }
    }

    fun setNegative(){
        val clock : ImageView = findViewById(R.id.imageView)
        var centerPointX = width/2
        var centerPointY = height/2
        var radius = (height/2)-100
        var x = centerPointX+(radius*cos(Math.toRadians(-90.0)))
        var y = centerPointY+(radius*sin(Math.toRadians(-90.0)))
        clock.startAnimation(TranslateAnimation(0f, x.toFloat(),0f, y.toFloat()))
        clock.visibility = View.VISIBLE
        setNegativePointActive = true
        soundPool!!.stop(streamID!!)
        soundPool!!.load(this, R.raw.set_point_negative, 2)
        streamID = soundPool!!.play(2, 100f, 100f, 1, 0, 1f)
    }

    fun setResource(){
        setResourcePointActive = true
        soundPool!!.stop(streamID!!)
        soundPool!!.load(this, R.raw.set_resource_point, 3)
        streamID = soundPool!!.play(3, 100f, 100f, 1, 0, 1f)
    }

    fun partTwo(){
        partTwoActive = true
        soundPool!!.stop(streamID!!)
        soundPool!!.load(this, R.raw.attend_to_negative, 4)
        streamID = soundPool!!.play(4, 100f, 100f, 1, 0, 1f)
        var rand = Random()
        var delay = 5+rand.nextInt(7)
        Thread.sleep(delay.toLong())
    }

    /*suspend fun rotateLeft(width: Int, height: Int) = coroutineScope{
        launch{
            val clock : ImageView = findViewById(R.id.imageView)
            while (keypressed){
                var animation = RotateAnimation(rotateAngle, rotateAngle-0.5f, (clock.width/2).toFloat(), (clock.height/2).toFloat())
                animation.fillAfter = true
                rotateAngle-=0.5f
                if (rotateAngle<-360) rotateAngle=0f
                clock.startAnimation(animation)
                Thread.sleep(50)
            }
        }
    }
    suspend fun rotateRight(width: Int, height: Int) = coroutineScope{
        launch{
            val clock : ImageView = findViewById(R.id.imageView)
            while (keypressed){
                var animation = RotateAnimation(rotateAngle, rotateAngle+0.5f, (clock.width/2).toFloat(), (clock.height/2).toFloat())
                animation.fillAfter = true
                rotateAngle+=0.5f
                if (rotateAngle>360) rotateAngle=0f
                clock.startAnimation(animation)
                Thread.sleep(50)
            }
        }
    }*/
    suspend fun translateLeft() = coroutineScope {
        launch {
            var centerPointX = width/2
            var centerPointY = height/2
            var radius = (height/2)-100
            val clock : ImageView = findViewById(R.id.imageView)
        while (keypressed) {
                rotateAngle-=0.5f
                if (rotateAngle<-360) rotateAngle = -0.5f
                var xaar = centerPointX+(radius*cos(Math.toRadians(rotateAngle.toDouble())))
                var yaar = centerPointY+(radius*sin(Math.toRadians(rotateAngle.toDouble())))
                var animation = TranslateAnimation(lastPoint.x, xaar.toFloat(), lastPoint.y, yaar.toFloat())
                lastPoint.x= xaar.toFloat()
                lastPoint.y = yaar.toFloat()
                animation.fillAfter = true
                clock.startAnimation(animation)
                Thread.sleep(25)
            }
        }
    }
    suspend fun translateRight() = coroutineScope {
        launch {
            var centerPointX = width/2
            var centerPointY = height/2
            var radius = (height/2)-100
            val clock : ImageView = findViewById(R.id.imageView)
            while (keypressed) {
                rotateAngle+=0.5f
                if (rotateAngle>360) rotateAngle = 0.5f
                var xaar = centerPointX+(radius*cos(Math.toRadians(rotateAngle.toDouble())))
                var yaar = centerPointY+(radius*sin(Math.toRadians(rotateAngle.toDouble())))
                var animation = TranslateAnimation(lastPoint.x, xaar.toFloat(), lastPoint.y, yaar.toFloat())
                lastPoint.x= xaar.toFloat()
                lastPoint.y = yaar.toFloat()
                animation.fillAfter = true
                clock.startAnimation(animation)
                Thread.sleep(25)
            }
        }
    }
}