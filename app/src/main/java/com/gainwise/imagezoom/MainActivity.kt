package com.gainwise.imagezoom

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.gainwise.jumpstartlib.PermissionsHandler
import kotlinx.android.synthetic.main.activity_main.*
import spencerstudios.com.fab_toast.FabToast
import java.io.File
import java.io.FileOutputStream








class MainActivity : AppCompatActivity() {

    lateinit var image: Bitmap
    var ready = false

    //this classwide pHandler variable will be used to actually call/check/retrieve permissions transactions and requests
    var pHandler = PermissionsHandler(PermissionsHelper())

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        //initialize views
        selectButton.setOnClickListener{imagePick()}
        saveButton.setOnClickListener{screenshotAndSave()}
        rotateButton.setOnClickListener{rotateImage()}

        //sets the zoom min and max for the imageview
        imageview.maxZoom = 100000f
        imageview.minZoom -100f

        val intent = intent
        val action = intent.action
        val type = intent.type


        if (Intent.ACTION_SEND == action) {
            if (type != null) {
                val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (imageUri != null) {
                    imageview.setImageURI(imageUri)
                } else {

                }
            } else {

            }
        }
    }


    //method to launch gallery to pick image
    fun imagePick(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 0)
    }


    //method to take the screenshot and save it
    fun screenshotAndSave(){
        if(ready) {
            image = ScreenShot.takeScreenShot(imageviewwrapper)
            imageview.setImageBitmap(image)
            imageview.resetZoom()

            try {
                if (pHandler.needPermissions(pHandler.getPermissions())) {
                    pHandler.requestPermissions();
                } else {
                    savePicture(image)
                }
                FabToast.makeText(this, "Success, You can find the image in the gallery now!", FabToast.LENGTH_LONG,
                        FabToast.SUCCESS, FabToast.POSITION_DEFAULT).show()
            } catch (e: Exception) {
                FabToast.makeText(this, "Something went wrong", FabToast.LENGTH_LONG,
                        FabToast.ERROR, FabToast.POSITION_DEFAULT).show()
            }
        }
        else{
            FabToast.makeText(this, "Nothing to Save?", FabToast.LENGTH_LONG,
                    FabToast.INFORMATION, FabToast.POSITION_CENTER).show()
        }

    }

    //method to rotate image 90 degrees
    fun rotateImage(){
        imageview.rotation += 90f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(packageName, data?.data.toString());
        //Picasso.get().load(data?.data).into(imageview)
        FabToast.makeText(this, "Pinch to Zoom!", FabToast.LENGTH_SHORT, FabToast.INFORMATION,
                FabToast.POSITION_CENTER).show()
        imageview.setImageURI(data?.data)
        imageview.setZoom(1f)
        ready = true

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        pHandler.handleResult(requestCode, permissions, grantResults)
    }

    fun savePicture(image: Bitmap){

        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myrootDir = File(root)
        if (!myrootDir.exists()) {
            Log.i("Infinity_Zoom", "doesn't exist1")
            myrootDir.mkdir()
        }

        val myDir = File(root + "/Zoom")
        if (!myDir.exists()) {
            Log.i("Infinity_Zoom", "doesn't exist2")
            myDir.mkdir();
        }
        val n = System.currentTimeMillis()
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        try {
            val out = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            Log.i( "Infinity_Zoom", e.message)

        }
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        sendBroadcast(intent)
    }

    inner class PermissionsHelper : PermissionsHandler.PermissionsDirective{
        override fun executeOnPermissionDenied() {
           FabToast.makeText(Outer@ this@MainActivity, "The app can not save without your permission", FabToast.LENGTH_LONG,
                   FabToast.WARNING, FabToast.POSITION_DEFAULT).show()
        }

        override fun requestCode(): Int {
            return 152
        }

        override fun withActivity(): Activity {
            return Outer@ this@MainActivity
        }

        override fun permissionsToRequest(): Array<String> {
       return arrayOf("android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_EXTERNAL_STORAGE")
        }

        override fun executeOnPermissionGranted() {
            savePicture(image)
            FabToast.makeText(Outer@ this@MainActivity, "Success, You can find the image in the gallery now!", FabToast.LENGTH_LONG,
                    FabToast.SUCCESS, FabToast.POSITION_DEFAULT).show()
        }

    }
}
