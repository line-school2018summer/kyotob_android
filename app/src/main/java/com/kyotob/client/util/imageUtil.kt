package com.kyotob.client.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.kyotob.client.IMAGE_PATH_KEY
import com.kyotob.client.IMAGE_PREFERENCE_KEY
import com.kyotob.client.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
import com.kyotob.client.UriToFile
import com.kyotob.client.register.RegisterActivity
import com.kyotob.client.setting.SettingFragment
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

        if (ContextCompat.checkSelfPermission(activity!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 以前、パーミッションを要求したことがある場合、
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // パーミッションが断られた場合
                Toast.makeText(activity!!.applicationContext, "Please accept STORAGE permission", Toast.LENGTH_LONG).show()
                dismiss()
            } else { // 初めて要求する場合、
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // パーミッションが断られた場合
                    Toast.makeText(activity!!.applicationContext, "Please accept STORAGE permission", Toast.LENGTH_LONG).show()
                    dismiss()
                }
            }
        }
        val items = arrayOf("写真をとる", "写真をえらぶ", "デフォルトに戻す")
        return AlertDialog.Builder(activity!!)
                .setTitle("ユーザーアイコンの設定")
                .setItems(items, DialogInterface.OnClickListener { _, num ->
                    when (num) {
                        0 -> {
                            dispatchCameraIntent()
                        }
                        1 -> {
                            despatchGallaryIntent()
                        }
                        2 -> {
                            if (activity is RegisterActivity) {
                                (activity as RegisterActivity).setDefaultIcon()
                            } else if (targetFragment is SettingFragment) {
                                (targetFragment as SettingFragment).setDefaultIcon()
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
        if (targetFragment != null) {
            targetFragment!!.startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
        } else {
            activity!!.startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
        }
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
                if (targetFragment != null) {
                    targetFragment!!.startActivityForResult(intent, TAKE_PICTURE)
                } else {
                    activity!!.startActivityForResult(intent, TAKE_PICTURE)
                }
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
    val scheme: String = uri.scheme!!
    var fileName: String? = null
    // get file name
    when (scheme) {
        "content" -> {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME )
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                }
                cursor.close()
            }
        }

        "file" -> {
            fileName = File(uri.path).name
        }
    }

    return fileName
}

fun imageActivityResult(requestCode: Int, resultCode: Int, data: Intent?, context: Context): Uri?  {
    // 写真を撮ったときの挙動
    if(requestCode == ImageDialog.TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
        try {
            val imageSharedPreferences = context.getSharedPreferences(IMAGE_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val file = File(imageSharedPreferences.getString(IMAGE_PATH_KEY,null))
            return Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // アルバムから画像を選んだときの挙動
    if(requestCode == ImageDialog.SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
        try {
            val file = File(UriToFile().getPathFromUri(context, data!!.data))
            val uri = Uri.fromFile(file)
            Log.d("URI", uri.toString())
            return uri
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    return null
}
