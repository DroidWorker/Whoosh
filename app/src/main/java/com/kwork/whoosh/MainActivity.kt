package com.kwork.whoosh

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import android.view.Display
import android.widget.ImageView
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import android.view.WindowManager
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.*
import android.renderscript.Float3
import android.util.Log
import android.util.TypedValue
import android.view.animation.*
import androidx.annotation.Dimension
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Flow


class MainActivity : FragmentActivity() {
    val colors : List<Int> = listOf(Color.rgb(229,28,35),
        Color.rgb(233,30,99),
        Color.rgb(156,39,176),
        Color.rgb(103,58,183),
        Color.rgb(63,81,181),
        Color.rgb(86,119,252),
        Color.rgb(3,169,244),
        Color.rgb(0,188,212),
        Color.rgb(0,150,136),
        Color.rgb(37,155,36),
        Color.rgb(139,195,74),
        Color.rgb(205,220,57),
        Color.rgb(255,235,59),
        Color.rgb(255,193,7),
        Color.rgb(255,152,0),
        Color.rgb(255,87,34),
        Color.rgb(121,85,72),
        Color.rgb(158,158,158),
        Color.rgb(96,125,139),
        Color.rgb(255,255,255),
        Color.rgb(0,0,0)
    )

    var coords : MutableList<PointF> = mutableListOf()
    var currentPosID = 2700

    var attendToNegativeID = 0
    var blinkID = 0
    var doDeepInspirationID = 0
    var setPointNegativeID = 0
    var setResourcePointID = 0
    var sudID = 0
    var wooshID = 0

    var currentColorID = 20
    var prevColorID = 20
    var colorSettingActive : Boolean = false
    var resourcePointColor : Int? = null

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
    var negativeAngle = 0f//0-360
    var negativePoint : PointF = PointF(0f,0f)

    var setResourcePointActive : Boolean = false
    var resourceAngle = 0f//0-360
    var resourcePoint : PointF = PointF(0f,0f)

    var partTwoActive : Boolean = false
    var deep : Int = 0

    var sudMenuLargeActive : Boolean = false

    var gratitude : Boolean = false
    var selectedGratitude = 2

    var backMenuActive : Boolean = false
    var selectedBackMenu = 6;
    var changeMin : Boolean = false
    var changeMax : Boolean = false
    var minValue = 5
    var maxValue = 12
    lateinit var ctx : Context

    var stopAll : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ctx = this
        currentColorID = intent.getIntExtra("color", 20)
        val rl: RelativeLayout = findViewById(R.id.root)
        rl.setBackgroundColor(colors[currentColorID])

        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y

        var clock : ImageView = findViewById(R.id.imageView)
        var centerPointX = width/2
        var centerPointY = height/2-50
        var radius = (height/2)-100
        var x = centerPointX+(radius*cos(Math.toRadians(-90.0)))
        var y = centerPointY+(radius*sin(Math.toRadians(-90.0)))

        clock.x = x.toFloat()
        clock.y=y.toFloat()

        val sudLL : LinearLayout = findViewById(R.id.sudMenu)
        sudLL.visibility = View.VISIBLE
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 100)
        soundPool!!.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener{
            override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
                if(sampleId==6) {
                    streamID = soundPool!!.play(sudID, 100f, 100f, 1, 0, 1f)
                    var tvLoading : TextView = findViewById(R.id.loading)
                    var angle = 0f
                    for (i in 0..3600){
                        angle+=0.1f
                        var xaar =
                            (centerPointX + (radius * cos(Math.toRadians(angle.toDouble())))).toFloat()
                        var yaar =
                            (centerPointY + (radius * sin(Math.toRadians(angle.toDouble())))).toFloat()
                        coords.add(i, PointF(xaar, yaar))
                    }
                    tvLoading.visibility=View.GONE
                }
            }
        })
        attendToNegativeID=soundPool!!.load(this, R.raw.attend_to_negative, 1)
        blinkID=soundPool!!.load(this, R.raw.blink, 1)
        doDeepInspirationID=soundPool!!.load(this, R.raw.do_deep_inspiration, 1)
        setPointNegativeID=soundPool!!.load(this, R.raw.set_point_negative, 1)
        setResourcePointID=soundPool!!.load(this, R.raw.set_resource_point, 1)
        sudID=soundPool!!.load(this, R.raw.sud, 1)
        wooshID=soundPool!!.load(this, R.raw.wooosh, 1)
    }
    override fun onDestroy(){
        soundPool?.autoPause()
        super.onDestroy()
    }

    var animator = ValueAnimator()
    var currentPosOnCircle = -90
    fun moveImageInCircle(isStart : Boolean, isLeft : Boolean, oneDeg : Boolean) {
        var clock : ImageView = findViewById(R.id.imageView)
        var center : PointF = PointF(0f,0f)
        var radius = 0f
        var curPos : PointF = PointF(0f, 0f)
        center.x=(width/2).toFloat()
        center.y=(height/2-50).toFloat()
        radius = ((height/2)-100).toFloat()
        if (isStart) {
            val p = Path()
            val circle = RectF(
                (center.x - radius),
                (center.y-radius),
                (center.x + radius),
                (center.y+radius)
            )
            if (isLeft) {
                if (!oneDeg){
                        p.arcTo(circle, currentPosOnCircle.toFloat(), -360f-(currentPosOnCircle%360))
                        p.arcTo(circle, -1f, currentPosOnCircle.toFloat()/*-360f*/)
                    }
                else{
                    p.arcTo(circle, currentPosOnCircle.toFloat(), 1f)
                    currentPosOnCircle+=1
                    Log.i("pos", currentPosOnCircle.toString())
                }
            }
            else{
                if (!oneDeg){
                        p.arcTo(circle, currentPosOnCircle.toFloat(), +360f-(currentPosOnCircle%360))
                        p.arcTo(circle, 1f, 360-Math.abs(currentPosOnCircle.toFloat())/*+360f*/)
                    }
                else{
                    p.arcTo(circle, currentPosOnCircle.toFloat(), -1f)
                    currentPosOnCircle-=1
                    Log.i("pos", currentPosOnCircle.toString())
                }
            }
            val pm = PathMeasure(p, false)
            val pos = FloatArray(2)
            animator = ValueAnimator.ofFloat(0f, pm.length)
            animator.addUpdateListener { animation ->
                val v = animation.animatedValue as Float
                pm.getPosTan(v, pos, null)
                if(!oneDeg) {
                    var currentPosOnCircleRad =
                        (Math.atan(((pos[0] - center.x) / (pos[1] - center.y)).toDouble()))
                    currentPosOnCircle = (90 + (Math.toDegrees(currentPosOnCircleRad).toInt())) * -1
                    if (pos[1] > center.y) {
                        currentPosOnCircle -= 180
                    }
                }
                currentPosOnCircle = currentPosOnCircle%360
                clock.setTranslationX(pos[0])
                clock.setTranslationY(pos[1])
            }
            if (!oneDeg) {
                animator.duration = 15000
                animator.repeatMode = ValueAnimator.RESTART
                animator.repeatCount = ValueAnimator.INFINITE
            }
            else
                animator.duration = 100
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
        else{
            animator.cancel()
            animator = ValueAnimator()
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var clock : ImageView = findViewById(R.id.imageView)
        if (!keypressed) {
            val rl: RelativeLayout = findViewById(R.id.root)
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    changeMax=false
                    changeMin=false
                    if (sudMenuActive&&!backMenuActive&&!colorSettingActive) {
                        currSUDmenuItemID--
                        if (currSUDmenuItemID < 1)
                            currSUDmenuItemID = 10
                        sudItemChange()
                    }
                    else if (setNegativePointActive&&!backMenuActive&&!colorSettingActive){
                        //moveImageInCircle(true, true, false)
                        currentColorID--
                        if (currentColorID<0)currentColorID=20
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                    else if (setResourcePointActive&&!backMenuActive&&!colorSettingActive){
                        //moveImageInCircle(true, false, false)
                        if(resourcePointColor==null)
                            resourcePointColor = currentColorID
                        resourcePointColor = resourcePointColor!!-1
                        if (resourcePointColor!! <0)resourcePointColor=20
                        rl.setBackgroundColor(colors[resourcePointColor!!])
                    }
                    else if (sudMenuLargeActive&&!backMenuActive&&!colorSettingActive){
                        currSUDmenuItemID--
                        if (currSUDmenuItemID < 0)
                            currSUDmenuItemID = 10
                        sudItemLargeChange()
                    }
                    else if(gratitude&&!backMenuActive&&!colorSettingActive){
                        var tvExit : TextView = findViewById(R.id.exit)
                        var tvRestart : TextView = findViewById(R.id.restart)
                        if (selectedGratitude==2) {
                            selectedGratitude=1
                            tvExit.setBackgroundColor(Color.rgb(62,62,62))
                            tvRestart.setBackgroundColor(Color.WHITE)
                        }
                        else {
                            selectedGratitude=2
                            tvExit.setBackgroundColor(Color.WHITE)
                            tvRestart.setBackgroundColor(Color.rgb(62,62,62))
                        }
                    }
                    else if(backMenuActive&&!colorSettingActive){
                        selectedBackMenu--
                        if (selectedBackMenu<1) selectedBackMenu=8
                        backItemChange()
                    }
                    else if (colorSettingActive){
                        currentColorID--
                        if (currentColorID<0)currentColorID=20
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    changeMax=false
                    changeMin=false
                    if (sudMenuActive&&!backMenuActive&&!colorSettingActive) {
                        currSUDmenuItemID++
                        if (currSUDmenuItemID > 10)
                            currSUDmenuItemID = 1
                        sudItemChange()
                    }
                    else if (setNegativePointActive&&!backMenuActive&&!colorSettingActive){
                        //moveImageInCircle(true, true, false)
                        currentColorID++
                        if (currentColorID>20)currentColorID=0
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                    else if (setResourcePointActive&&!backMenuActive&&!colorSettingActive){
                        //moveImageInCircle(true, false, false)
                        if(resourcePointColor==null)
                            resourcePointColor = currentColorID
                        resourcePointColor = resourcePointColor!!+1
                        if (resourcePointColor!! >20)resourcePointColor=0
                        rl.setBackgroundColor(colors[resourcePointColor!!])
                    }
                    else if (sudMenuLargeActive&&!backMenuActive&&!colorSettingActive){
                        currSUDmenuItemID++
                        if (currSUDmenuItemID > 10)
                            currSUDmenuItemID = 0
                        sudItemLargeChange()
                    }
                    else if(gratitude&&!backMenuActive&&!colorSettingActive){
                        var tvExit : TextView = findViewById(R.id.exit)
                        var tvRestart : TextView = findViewById(R.id.restart)
                        if (selectedGratitude==2) {
                            selectedGratitude=1
                            tvExit.setBackgroundColor(Color.rgb(62,62,62))
                            tvRestart.setBackgroundColor(Color.WHITE)
                        }
                        else {
                            selectedGratitude=2
                            tvExit.setBackgroundColor(Color.WHITE)
                            tvRestart.setBackgroundColor(Color.rgb(62,62,62))
                        }
                    }
                    else if(backMenuActive&&!colorSettingActive){
                        selectedBackMenu++
                        if (selectedBackMenu>8) selectedBackMenu=1
                        backItemChange()
                    }
                    else if (colorSettingActive){
                        currentColorID++
                        if (currentColorID>20)currentColorID=0
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (setNegativePointActive&&!colorSettingActive&&!changeMax&&!changeMin){
                        //GlobalScope.launch { translateLeft(0f) }
                        moveImageInCircle(true, true, false)
                    }
                    else if (setResourcePointActive&&!colorSettingActive&&!changeMax&&!changeMin){
                        //GlobalScope.launch { translateRight() }
                        moveImageInCircle(true, false, false)
                    }
                    else if (colorSettingActive&&!changeMax&&!changeMin){
                        currentColorID++
                        if (currentColorID>20)currentColorID=0
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                    else if(changeMin){
                        minValue++
                        if (minValue>=maxValue)
                            minValue--
                        updateMinValue()

                    }
                    else if(changeMax){
                        maxValue++
                        updateMaxValue()
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (setNegativePointActive&&!colorSettingActive&&!changeMax&&!changeMin){
                        moveImageInCircle(true, true, true)
                    }
                    else if (setResourcePointActive&&!colorSettingActive&&!changeMax&&!changeMin){
                        moveImageInCircle(true, false, true)
                    }
                    else if (colorSettingActive&&!changeMax&&!changeMin){
                        currentColorID--
                        if (currentColorID<0)currentColorID=20
                        rl.setBackgroundColor(colors[currentColorID])
                    }
                    else if(changeMin){
                        minValue--
                        if (minValue<1)
                            minValue++
                        updateMinValue()
                    }
                    else if(changeMax){
                        maxValue--
                        if (maxValue<=minValue)
                            maxValue++
                        updateMaxValue()
                    }
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A -> {
                    if (sudMenuActive&&!backMenuActive&&!colorSettingActive) {
                        selectedSUDnum = currSUDmenuItemID
                        if (soundPool!=null&&streamID!=null)
                            soundPool?.stop(streamID!!)
                        val sudLL : LinearLayout = findViewById(R.id.sudMenu)
                        sudLL.visibility = View.GONE
                        sudMenuActive = false
                        setNegative()
                    }
                    else if(setNegativePointActive&&!backMenuActive&&!colorSettingActive){
                        setNegativePointActive = false
                        negativeAngle=currentPosOnCircle.toFloat()
                        negativePoint.set(clock.x, clock.y)
                        if (resourcePointColor!=null)
                            rl.setBackgroundColor(colors[resourcePointColor!!])
                        setResource()
                    }
                    else if(setResourcePointActive&&!backMenuActive&&!colorSettingActive) {
                        setResourcePointActive = false
                        resourceAngle=currentPosOnCircle.toFloat()
                        rotateAngle =resourceAngle
                        resourcePoint.set(clock.x, clock.y)
                        rl.setBackgroundColor(colors[currentColorID])
                        partTwo()
                    }
                    else if(sudMenuLargeActive&&!backMenuActive&&!colorSettingActive){
                        sudMenuLargeActive = false
                        val sudLargeL : LinearLayout = findViewById(R.id.sudMenuLarge)
                        sudLargeL.visibility = View.GONE
                        if (currSUDmenuItemID!=0) setNegative()
                        else{
                            var gratitudeLayout : LinearLayout = findViewById(R.id.gratitude)
                            gratitudeLayout.visibility = View.VISIBLE
                            gratitude = true
                        }
                    }
                    else if(gratitude&&!backMenuActive&&!colorSettingActive){
                        when(selectedGratitude){
                            1->{
                                finishAndRemoveTask()
                            }
                            2->{
                                val intent = intent
                                finish()
                                startActivity(intent)
                            }
                        }
                    }
                    else if(backMenuActive&&!colorSettingActive) {
                        when(selectedBackMenu){
                            1->{
                                android.os.Process.killProcess(android.os.Process.myPid())
                                //finishAndRemoveTask()
                            }
                            2->{
                                colorSettingActive = true
                                var tvColorSetting : TextView = findViewById(R.id.colorSetting)
                                tvColorSetting.visibility = View.VISIBLE
                            }
                            3->{
                                val intent = intent
                                //intent.putExtra("color", currentColorID)
                                finish()
                                stopAll = true
                                startActivity(intent)
                            }
                        }
                        backMenuActive=false
                        val backLayout : LinearLayout = findViewById(R.id.backMenu)
                        backLayout.visibility = View.GONE
                    }
                    else if(colorSettingActive){
                        colorSettingActive=false
                        var tvColorSetting : TextView = findViewById(R.id.colorSetting)
                        tvColorSetting.visibility = View.GONE
                        if(setResourcePointActive) {
                            resourcePointColor = currentColorID
                            currentColorID = prevColorID
                        }
                    }

                }
                KeyEvent.KEYCODE_BACK -> {
                    if (colorSettingActive){
                        colorSettingActive=false
                        var tvColorSetting : TextView = findViewById(R.id.colorSetting)
                        tvColorSetting.visibility = View.GONE
                        rl.setBackgroundColor(colors[prevColorID])
                        currentColorID = prevColorID
                        return true
                    }
                    val backMenu: LinearLayout = findViewById(R.id.backMenu)
                    if(!backMenuActive) {
                        backMenu.visibility = View.VISIBLE
                        backMenuActive = true
                        return true
                    }
                    else{
                        backMenu.visibility = View.GONE
                        backMenuActive = false
                        return true
                    }
                }
            }
        }
        keypressed = true
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        keypressed = false
        moveImageInCircle(false, true, false)
        return super.onKeyUp(keyCode, event)
    }

    private fun updateMaxValue(){
        var tvmax : TextView = findViewById(R.id.backMAX)
        tvmax.text = maxValue.toString()
    }
    private fun updateMinValue(){
        var tvmin : TextView = findViewById(R.id.backMIN)
        tvmin.text = minValue.toString()
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

    private fun sudItemLargeChange(){
        val tv0 : TextView = findViewById(R.id.textView0)
        val tv1 : TextView = findViewById(R.id.ltextView1)
        val tv2 : TextView = findViewById(R.id.ltextView2)
        val tv3 : TextView = findViewById(R.id.ltextView3)
        val tv4 : TextView = findViewById(R.id.ltextView4)
        val tv5 : TextView = findViewById(R.id.ltextView5)
        val tv6 : TextView = findViewById(R.id.ltextView6)
        val tv7 : TextView = findViewById(R.id.ltextView7)
        val tv8 : TextView = findViewById(R.id.ltextView8)
        val tv9 : TextView = findViewById(R.id.ltextView9)
        val tv10 : TextView = findViewById(R.id.ltextView10)
        when(currSUDmenuItemID){
            0->{
                tv10.setBackgroundColor(Color.WHITE)
                tv0.setBackgroundColor(Color.rgb(62,62,62))
                tv1.setBackgroundColor(Color.WHITE)
            }
            1->{
                tv0.setBackgroundColor(Color.WHITE)
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
                tv0.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun backItemChange(){
        var tvExit : TextView = findViewById(R.id.backExit)
        var tvRestart : TextView = findViewById(R.id.backRestart)
        var tvls : TextView = findViewById(R.id.backBls)
        var tvInterval : TextView = findViewById(R.id.backInterval)
        var tvReverce : TextView = findViewById(R.id.backReverce)
        var tvCancel : TextView = findViewById(R.id.backCancel)
        var tvBackMin : TextView = findViewById(R.id.backMIN)
        var tvBackMax : TextView = findViewById(R.id.backMAX)
        when(selectedBackMenu){
            1->{
                tvBackMax.setBackgroundColor(Color.WHITE)
                tvExit.setBackgroundColor(Color.rgb(62,62,62))
                tvRestart.setBackgroundColor(Color.WHITE)
            }
            2->{
                tvExit.setBackgroundColor(Color.WHITE)
                tvRestart.setBackgroundColor(Color.rgb(62,62,62))
                tvls.setBackgroundColor(Color.WHITE)
            }
            3->{
                tvRestart.setBackgroundColor(Color.WHITE)
                tvls.setBackgroundColor(Color.rgb(62,62,62))
                tvInterval.setBackgroundColor(Color.WHITE)
            }
            4->{
                tvls.setBackgroundColor(Color.WHITE)
                tvInterval.setBackgroundColor(Color.rgb(62,62,62))
                tvReverce.setBackgroundColor(Color.WHITE)
            }
            5->{
                tvInterval.setBackgroundColor(Color.WHITE)
                tvReverce.setBackgroundColor(Color.rgb(62,62,62))
                tvCancel.setBackgroundColor(Color.WHITE)
            }
            6->{
                tvReverce.setBackgroundColor(Color.WHITE)
                tvCancel.setBackgroundColor(Color.rgb(62,62,62))
                tvBackMin.setBackgroundColor(Color.WHITE)
            }
            7->{
                changeMin = true
                tvCancel.setBackgroundColor(Color.WHITE)
                tvBackMin.setBackgroundColor(Color.rgb(62,62,62))
                tvBackMax.setBackgroundColor(Color.WHITE)
            }
            8->{
                changeMax = true
                tvBackMin.setBackgroundColor(Color.WHITE)
                tvBackMax.setBackgroundColor(Color.rgb(62,62,62))
                tvExit.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun sudLarge(){
        if (stopAll) {
            stopAll = false
            return
        }
        deep=0
        streamID = soundPool!!.play(sudID, 100f, 100f, 1, 0, 1f)
        var sudMenuLarge : LinearLayout = findViewById(R.id.sudMenuLarge)
        this@MainActivity.runOnUiThread(java.lang.Runnable {
            sudMenuLarge.visibility = View.VISIBLE
            sudItemLargeChange()
        })
        sudMenuLargeActive = true
    }

    private fun setNegative(){
        val clock : ImageView = findViewById(R.id.imageView)
        clock.visibility = View.VISIBLE
        setNegativePointActive = true
        soundPool!!.stop(streamID!!)
        streamID = soundPool!!.play(setPointNegativeID, 100f, 100f, 1, 0, 1f)
    }

    private fun setResource(){
        setResourcePointActive = true
        soundPool!!.stop(streamID!!)
        streamID = soundPool!!.play(setResourcePointID, 100f, 100f, 1, 0, 1f)
    }

    private fun partTwo(){
        if (deep==0)
            GlobalScope.launch { moveToPositive(ctx) }
        else {
            while (rotateAngle != negativeAngle) {
                Thread.sleep(50)
            }
            Thread.sleep(500)
            if (stopAll) {
                stopAll = false
                return
            }
            soundPool!!.stop(streamID!!)
            streamID = soundPool!!.play(attendToNegativeID, 100f, 100f, 1, 0, 1f)
            var ctx: Context = this
            GlobalScope.launch { moveToPositive(ctx) }
        }
        partTwoActive = true
        deep++
    }

    suspend fun moveToPositive(ctx : Context) = coroutineScope {
        launch {
            if (stopAll) {
                stopAll = false
                return@launch
            }
            val clock: ImageView = findViewById(R.id.imageView)
            if (deep>1) {
                Thread.sleep(2600)
                var rand = Random()
                var delay = 1000 * (minValue + rand.nextInt(maxValue - minValue))
                Thread.sleep(delay.toLong())
                var negativeX: Float
                var negativeY: Float
                rotateAngle = resourceAngle
                var deltaX: Float =
                    (Math.abs(Math.abs(negativePoint.x) - Math.abs(resourcePoint.x))) / 500
                var deltaY: Float =
                    (Math.abs(Math.abs(negativePoint.y) - Math.abs(resourcePoint.y))) / 500
                if (negativePoint.x > resourcePoint.x) deltaX *= -1
                if (negativePoint.y > resourcePoint.y) deltaY *= -1
                val rl:RelativeLayout = findViewById(R.id.root)
                for (i in 1..500) {
                    clock.x = clock.x + deltaX
                    clock.y = clock.y + deltaY
                    Thread.sleep(1)
                }
                if (stopAll) {
                    stopAll = false
                    return@launch
                }
                soundPool!!.stop(streamID!!)
                streamID = soundPool!!.play(wooshID, 500f, 500f, 1, 0, 1f)
                if (resourcePointColor!=null)
                    rl.setBackgroundColor(colors[resourcePointColor!!])
                lastPoint.set(resourcePoint.x, resourcePoint.y)
                Thread.sleep(500)
                if (stopAll) {
                    stopAll = false
                    return@launch
                }
                soundPool!!.stop(streamID!!)
                streamID = soundPool!!.play(blinkID, 100f, 100f, 2, 0, 1f)
                //Thread.sleep(2100)
                delay(3000)
                if(deep>3) Thread.sleep(200)
            }
            if (deep>3) {
                if (stopAll) {
                    stopAll = false
                    return@launch
                }
                val rl:RelativeLayout = findViewById(R.id.root)
                if (resourcePointColor!=null)
                    rl.setBackgroundColor(colors[currentColorID])
                soundPool!!.stop(streamID!!)
                streamID = soundPool!!.play(doDeepInspirationID, 100f, 100f, 1, 0, 1f)
                //Thread.sleep(4750)
                delay(5750L)
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    clock.visibility = View.GONE
                })
                partTwoActive = false
                sudLarge()
            }
            else{
                if (resourcePointColor!=null){
                    val rl:RelativeLayout = findViewById(R.id.root)
                    rl.setBackgroundColor(colors[currentColorID])
                    }
                translateLeft(negativeAngle)
                partTwo()
            }
        }
    }
    suspend fun translateLeft(endAngle : Float) = coroutineScope {
        launch {
            if (stopAll) this.cancel()
            val clock : ImageView = findViewById(R.id.imageView)
            if (endAngle==0f) {
                while (keypressed) {
                    rotateAngle -= 0.1f
                    if (rotateAngle < -360) rotateAngle = -0.1f
                    currentPosID-=1
                    if (currentPosID<0) currentPosID=3600
                    clock.x=coords[currentPosID].x
                    clock.y=coords[currentPosID].y
                    Thread.sleep(2)
                }
                lastPoint.x = coords[currentPosID].x
                lastPoint.y = coords[currentPosID].y
                negativePoint.set(clock.x, clock.y)
                negativeAngle = rotateAngle
            }
            else{
                if(rotateAngle<0) rotateAngle = (360+rotateAngle)
                while (rotateAngle!=endAngle&&(coords[(rotateAngle*10).toInt()])!=(coords[((360+endAngle)*10).toInt()])) {
                    rotateAngle -= 0.1f
                    if (rotateAngle < 0) rotateAngle = 360f
                    var x:Float
                    var y:Float
                    if (rotateAngle<0) {
                         x = coords[((360f + rotateAngle) * 10f).toInt()].x
                         y = coords[((360f + rotateAngle) * 10f).toInt()].y
                    }
                    else{
                         x = coords[((rotateAngle) * 10f).toInt()].x
                         y = coords[((rotateAngle) * 10f).toInt()].y
                    }
                    clock.x=x
                    clock.y=y
                    Thread.sleep(2)
                    lastPoint.x = x
                    lastPoint.y = y
                }
                rotateAngle=endAngle
                delay(500)
            }
        }
    }
    suspend fun translateRight() = coroutineScope {
        launch {
            val clock : ImageView = findViewById(R.id.imageView)
            var animation : TranslateAnimation
            while (keypressed) {
                rotateAngle+=0.1f
                if (rotateAngle>360) rotateAngle = 0.1f
                currentPosID++
                if (currentPosID>3600) currentPosID=0
                clock.x=coords[currentPosID].x
                clock.y=coords[currentPosID].y
                Thread.sleep(2)
            }
            lastPoint.x = coords[currentPosID].x
            lastPoint.y = coords[currentPosID].y
            resourcePoint.set(clock.x, clock.y)
            resourceAngle = rotateAngle
        }
    }
}