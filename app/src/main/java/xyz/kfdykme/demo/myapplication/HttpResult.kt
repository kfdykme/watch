package xyz.kfdykme.demo.myapplication

class HttpResult{

    var errcode:Int = 0
    var errmsg:String  =""
    override fun toString(): String {
        return "HttpResult(errcode=$errcode, errmsg='$errmsg')"
    }

}