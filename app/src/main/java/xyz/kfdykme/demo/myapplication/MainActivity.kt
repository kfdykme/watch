package xyz.kfdykme.demo.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.net.Uri
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : WearableActivity(),SensorEventListener {

    val TYPE_BREATH_MONITOR = 65550

    var lastUploadStepTime:Long = 0

    var lastUploadHeartRateTime:Long = 0

    //步数上传间隔
    val STEP_UPLOAD_DEVILE_TIME = 60 * 1000 * 5

    //心率上传间隔
    val HEART_RATE_UPLOAD_TIME = 60 * 1000

    val TAG = "PensionWatchApp"


    var phonenumber = 13540163268

    lateinit var mgr : SensorManager

    lateinit var server:PensionServer

    lateinit var context: Context

    val hostname = "http://119.27.187.141:8080/"


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Log.i(TAG,"onAccuracyChanged")
    }

    override fun onSensorChanged(event: SensorEvent?) {

        var type = event!!.sensor.type
        var s = "$type /// "
        for(e in event.values){
            s += "$e,"
        }

        //Log.i(TAG,s)


        when(type){

            Sensor.TYPE_HEART_RATE->{

                val cT =System.currentTimeMillis()

                val hearRate = event.values[0].toInt()
                tvHeart.setText("heart data : $hearRate")


                //如果与上一次获取步数间隔的时间小于十分钟,则不上传
                if(cT-lastUploadHeartRateTime<HEART_RATE_UPLOAD_TIME) return


                Log.i(TAG,hearRate.toString())
                server.uploadHeartRate(System.currentTimeMillis().toString(),hearRate.toString(),"watch","token123")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object :Observer<HttpResult>{
                            override fun onComplete() {
                             }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onNext(t: HttpResult) {
                                Log.i(TAG,t.toString())
                             }

                            override fun onError(e: Throwable) {
                                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                               //e.printStackTrace()
                            }

                        })

                lastUploadHeartRateTime = cT
            }

            Sensor.TYPE_STEP_COUNTER->{
                val cT =System.currentTimeMillis()

                val stepCount = event.values[0].toInt()


                //如果与上一次获取步数间隔的时间小于十分钟,则不上传
                if(cT-lastUploadStepTime<STEP_UPLOAD_DEVILE_TIME) return

                //Log.i(TAG,"Step "+stepCount.toString())

                server.uploadStep(
                        System.currentTimeMillis().toString(),
                        stepCount.toString(),
                        "watch",
                        "token123")

                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object :Observer<HttpResult>{
                            override fun onComplete() {
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onNext(t: HttpResult) {
                                Log.i(TAG,t.toString())
                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                                //e.printStackTrace()
                            }

                        })

                tvStepCount.setText("count data : $stepCount")

                lastUploadStepTime = cT

            }

        }



    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mgr = this.getSystemService(SENSOR_SERVICE) as SensorManager


        context = applicationContext

        var retrofit = Retrofit.Builder()
                .baseUrl(hostname)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()


        server = retrofit.create(PensionServer::class.java)


        val testSensor = mgr.getSensorList(21)[0]
        val septSensor = mgr.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val septCountSensor = mgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        mgr.registerListener(this,testSensor,SensorManager.SENSOR_DELAY_NORMAL)
        var stepD = mgr.registerListener(this,septSensor,SensorManager.SENSOR_DELAY_FASTEST)
        var stepC = mgr.registerListener(this,septCountSensor,SensorManager.SENSOR_DELAY_FASTEST)



        // Enables Always-on
        setAmbientEnabled()
    }


    override fun onPause() {
        super.onPause()
        mgr.unregisterListener(this)

    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i("Watch","{$keyCode}/"+event.toString())

        when(keyCode){
            265->{
                val intent = Intent(Intent.ACTION_CALL)
                val data = Uri.parse("tel:$phonenumber")
                intent.data = data
                try {
                    Toast.makeText(this,"call",Toast.LENGTH_LONG).show()
                    startActivity(intent)
                } catch (e:SecurityException){

                    Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
                }
            }
        }
        return false
    }

}
