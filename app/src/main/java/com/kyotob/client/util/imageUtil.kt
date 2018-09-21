package com.kyotob.client.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import com.kyotob.client.IMAGE_PATH_KEY
import com.kyotob.client.IMAGE_PREFERENCE_KEY
import com.kyotob.client.register.RegisterActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


class ImageDialog: DialogFragment() {

    lateinit var sharedPreferences: SharedPreferences

    companion object {
        // 画像用
        val TAKE_PICTURE = 1
        val SELECT_PICTURE = 2
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        sharedPreferences = activity!!.getSharedPreferences(IMAGE_PREFERENCE_KEY, Context.MODE_PRIVATE)

        val items = arrayOf("写真をとる", "写真をえらぶ", "デフォルトに戻す")
        return AlertDialog.Builder(activity!!)
                .setTitle("ユーザーアイコンの設定")
                .setItems(items, DialogInterface.OnClickListener { _, num ->
                    when(num) {
                        0 -> { dispatchCameraIntent() }
                        1 -> { despatchGallaryIntent() }
                        2 -> {
                            if (activity is RegisterActivity) {
                                (activity as RegisterActivity).setDefaultIcon()
                            }
                        }
            }
        }).create()
    }

    // GallaryActivity
    fun despatchGallaryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activity!!.startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }

    // CameraActivity
    fun dispatchCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(activity!!.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if(photoFile != null) {
                var photoUri = FileProvider.getUriForFile(activity!!,
                        "com.kyotob.client.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                activity!!.startActivityForResult(intent, TAKE_PICTURE)
            }
        }
    }

    fun createImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = timeStamp + "_"
        var storageDir = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageName, ".jpg", storageDir)
        val editor = sharedPreferences.edit()
        editor.putString(IMAGE_PATH_KEY, image.absolutePath)
        editor.apply()
        return image
    }
}

fun createIconUpload(uri: Uri, context: Context): MultipartBody.Part {
    val fileName:String = (getFileNameFromUri(uri, context) ?: "noname.jpg").decapitalize()
    val stream = context.getContentResolver().openInputStream(uri)
    val bmp: Bitmap = BitmapFactory.decodeStream( BufferedInputStream(stream));
    val byteArrayStream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayStream)
    val byteArray = byteArrayStream.toByteArray()
    bmp.recycle()
    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), byteArray)
    val body = MultipartBody.Part.createFormData("file", fileName, requestFile)
    return body
}

fun getFileNameFromUri(uri: Uri, context: Context): String?{

    // get scheme
    val scheme: String = uri.getScheme()!!;
    var fileName: String? = null
    // get file name
    when (scheme) {
        "content" -> {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME )
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                }
                cursor.close();
            }
        }

        "file" -> {
            fileName = File(uri.path).name;
        }
    }

    return fileName
}

fun imageActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity): Uri?  {
    // 写真を撮ったときの挙動
    if(requestCode == ImageDialog.TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
        try {
            val imageSharedPreferences = activity.getSharedPreferences(IMAGE_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val file = File(imageSharedPreferences.getString(IMAGE_PATH_KEY,null))
            return Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // アルバムから画像を選んだときの挙動
    if(requestCode == ImageDialog.SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
        try {
            return  data!!.data!!
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    return null
}
