package xyz.kfdykme.demo.myapplication

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PensionServer{

    @FormUrlEncoded
    @POST("elder/step")
    fun uploadStep(@Field("time") time:String,
               @Field("stepcount") stepcount:String,
               @Field("postby") postby:String,
               @Field("token") token:String): Observable<HttpResult>

    @FormUrlEncoded
    @POST("elder/pulse")
    fun uploadHeartRate(@Field("time") time:String,
                        @Field("pulsecount") pulsecount:String,
                        @Field("postby") postby:String,
                        @Field("token") token:String)
    :Observable<HttpResult>
}