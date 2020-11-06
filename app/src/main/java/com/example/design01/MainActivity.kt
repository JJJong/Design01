package com.example.design01

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    var pic : String? = null           // 이
    var URI : Uri? = null
    var bitmap : Bitmap? = null
    var fileName : String? = null
    var mimeType : String? = null

    val CAMERA_PERMISSION = arrayOf(android.Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val FLAG_PERM_CAMERA = 98
//    val FLAG_PERM_STRAGE = 99

    val FLAG_REQ_CAMERA = 101
    val FlAG_REQ_GALLERY = 102

    /***
     * 권한 가져오기 함수
     */
    fun isPermitted(permissions: Array<String>):Boolean{
        for(permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }
    /***
     * 카메라 오픈 함수
     */
    fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, FLAG_REQ_CAMERA)
    }
    /***
     * 갤러리 오픈 함수
     */
    fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, FlAG_REQ_GALLERY)
    }
    /***
     * 이미지 저장 함수
     */
    fun saveImageFile(filename: String, mimeType: String, bitmap: Bitmap) : Uri?{
        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            values.put(MediaStore.Images.Media.IS_PENDING, 1) // 현재 사용하고 있으면 사용금지
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) // 내가 저장할 사진의 주소값을 생성 (uri로)
        try {
            if (uri != null) {
                var descriptor = contentResolver.openFileDescriptor(uri, "w")

                if (descriptor != null) {
                    val fos = FileOutputStream(descriptor.fileDescriptor) // fileoutputstream 쓸때에는 try catch문을 써줘야한다.
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 100, fos
                    ) // 비트맵을 jpeg 로 압축하는데 손실률 100프로
                    fos.close()
                    return uri
                }
            }
        } catch (e: Exception){
            Log.e("Camera", "${e.localizedMessage}")
        }
        return null
    }
    /***
     * 파일 생성 함수
     */
    fun newFileName() : String{
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return filename
    }
    /***
     * 비트맵을 String으로 변환해주는 함수
     */
//    fun BitmapToString(bitmap: Bitmap): String? {
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
//        val bytes: ByteArray = baos.toByteArray()
//        return Base64.encodeToString(bytes, Base64.DEFAULT)
//    }

//    private fun initMyAPI(baseUrl: String) {
//        val clientBuilder = OkHttpClient.Builder()
//        val loggingInterceptor = HttpLoggingInterceptor()
//        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        clientBuilder.addInterceptor(loggingInterceptor)
//        Log.d(" ", "initMyAPI : $baseUrl")
//        val retrofit = Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(clientBuilder.build())
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        mMyAPI = retrofit.create(JsonPlaceHOlderApiKt::class.java)
//    }
    /***
     * 생명주기? 시작
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://69311547c680.ngrok.io/") // 디장고 서버 url
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val Api = retrofit.create(
            JsonPlaceHOlderApiKt::class.java
        )
        /***
         * 분석하기 버튼 눌렀을 때
         */
        btnResult.setOnClickListener{
            Log.d("", "POST")
                    val item = PostItemKt(test = pic)
                    Api.post_posts(item)?.enqueue(object : Callback<PostItemKt> {
                        override fun onFailure(call: Call<PostItemKt>, t: Throwable) {
                            Log.d("post", "")
                            var dialog = AlertDialog.Builder(this@MainActivity)
                            dialog.setTitle("Error")
                            dialog.setMessage("분석 실패")
                            dialog.show()
                        }
                        override fun onResponse(
                            call: Call<PostItemKt>,
                            response: Response<PostItemKt>
                        ) {
                            Log.d("post", "성공")

                            val getCall: Call<List<GetItemKt?>?>? = Api.get_result()
                            if (getCall != null) {
                                getCall.enqueue(object : Callback<List<GetItemKt?>?> {
                                    override fun onResponse(
                                        call: Call<List<GetItemKt?>?>,
                                        response: Response<List<GetItemKt?>?>
                                    ) {
                                        if (response.isSuccessful) {
                                            val mList: List<GetItemKt> =
                                                response.body() as List<GetItemKt>
                                            var result : Int? = null
                                            for (item in mList) {
                                                result = item.getResult()
                                            }
                                            Log.d("값", ":${result}")
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<List<GetItemKt?>?>,
                                        t: Throwable
                                    ) {

                                    }
                                })
                            }
                        }
                    })
        }
        /***
         * 카메라버튼 눌렀을 때
         */
        btnCamera.setOnClickListener {
            if (isPermitted(CAMERA_PERMISSION)) {
                openCamera()
            }
            else {
                ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, FLAG_PERM_CAMERA)
            }
        }
        /***
         * 갤러리 버튼 눌렀을 때
         */
        btnGallery.setOnClickListener{
            if(isPermitted(STORAGE_PERMISSION)){
                openGallery()
            }else{
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, FLAG_PERM_CAMERA)
            }
        }
    }
    /***
     * 카메라,갤러리 작업 끝났을 때
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri: Uri?
            if(resultCode == Activity.RESULT_OK){
                when(requestCode){
                    FLAG_REQ_CAMERA -> {
                        if (data?.extras?.get("data") != null) {
                            Log.d("data", "${data?.extras?.get("data")}")
                            bitmap = data?.extras?.get("data") as Bitmap
                            fileName = newFileName()
                            mimeType = "image/jpeg"
                            uri = saveImageFile(fileName!!, mimeType!!, bitmap!!)
                            URI = uri
                            imgView.setImageURI(uri)
                            val bitmap: Bitmap =
                                (imgView.getDrawable() as BitmapDrawable).getBitmap()
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            intent.putExtra("image",byteArray)
                            pic = Base64.encodeToString(byteArray, 0)
                            Log.d("형식", "${byteArray}")
                            Log.d("형식 2", "${pic}")
                        }
                    }
                    FlAG_REQ_GALLERY -> {
                        uri = data?.data
                        URI = uri
                        imgView.setImageURI(uri)
                        Log.d("값 :", "Uri = ${uri}")
                        Log.d("값 :", "Uri2 = ${URI}")
                    }
                        }
                    }

    }
    /***
     * 권한여부 체크
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            FLAG_PERM_CAMERA -> {
                var checked = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        checked = false
                        break
                    }
                }
                if (checked) {
                    openCamera()
                }
            }
        }
    }
}