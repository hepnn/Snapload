package com.snapchat.kit.snapload

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.snap.creativekit.SnapCreative
import com.snap.creativekit.exceptions.SnapMediaSizeException
import com.snap.creativekit.exceptions.SnapStickerSizeException
import com.snap.creativekit.exceptions.SnapVideoLengthException
import com.snap.creativekit.models.SnapContent
import com.snap.creativekit.models.SnapLiveCameraContent
import com.snap.creativekit.models.SnapPhotoContent
import com.snap.creativekit.models.SnapVideoContent
import com.theartofdev.edmodo.cropper.CropImage
import java.io.*


class MainActivity : AppCompatActivity() {
    enum class SnapState(val optionText: String) {
        NO_SNAP("Clear Snap"), CAMERA("Take Photo with Stock Camera SOON"), IMAGE("Select Image"), VIDEO(
            "Select Video"
        );

        fun getRequestCode(): Int {
            return ordinal
        }
    }

    private var mSnapState = SnapState.NO_SNAP
    private var mSnapFile: File? = null
    private var mStickerFile: File? = null
    private lateinit var mPreviewImage: ImageView
    private var mPreviewVideo: VideoView? = null
    private var mCaptionField: EditText? = null
    private var mUrlField: EditText? = null
    private var mSendStickerOption: CompoundButton? = null
    private var button: Button? = null
    private var dialogButton: Button? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val snapCreativeKitApi = SnapCreative.getApi(this)
        val snapMediaFactory = SnapCreative.getMediaFactory(this)

        mSnapFile = File(cacheDir, SNAP_NAME)
        mStickerFile = File(cacheDir, STICKER_NAME)
        mPreviewImage = findViewById(R.id.image_preview)
        mPreviewVideo = findViewById(R.id.video_preview)
        mCaptionField = findViewById(R.id.caption_field)
        mUrlField = findViewById(R.id.url_field)
        dialogButton = findViewById(R.id.dialog_button)
        mSendStickerOption = findViewById(R.id.send_sticker_options)
        mPreviewVideo?.setOnPreparedListener { mp -> mp.isLooping = true }
        if (!mStickerFile!!.exists()) {
            try {
                assets.open("sticker.png").use { inputStream ->
                    copyFile(
                        inputStream,
                        mStickerFile
                    )
                }
            } catch (e: IOException) {
                mSendStickerOption?.isEnabled = false
                Toast.makeText(this, "Failed to copy sticker asset", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<View>(R.id.dialog_button).setOnClickListener { openMediaSelectDialog() }


        findViewById<View>(R.id.share_button).setOnClickListener { view ->
            try {
                val content: SnapContent = when (mSnapState) {
                    SnapState.IMAGE -> SnapPhotoContent(
                        snapMediaFactory.getSnapPhotoFromFile(
                            mSnapFile
                        )
                    )
                    SnapState.VIDEO -> SnapVideoContent(
                        snapMediaFactory.getSnapVideoFromFile(
                            mSnapFile
                        )
                    )
                    SnapState.NO_SNAP -> SnapLiveCameraContent()
                    else -> SnapLiveCameraContent()
                }
                if (!TextUtils.isEmpty(mCaptionField?.text)) {
                    content.captionText = mCaptionField?.text.toString()
                }
                if (!TextUtils.isEmpty(mUrlField?.text)) {
                    content.attachmentUrl = mUrlField?.text.toString()
                }
                if (mSendStickerOption?.isChecked == true) {
                    val snapSticker = snapMediaFactory.getSnapStickerFromFile(mStickerFile)
                    snapSticker.setHeight(300f)
                    snapSticker.setWidth(300f)
                    snapSticker.setPosX(0.2f)
                    snapSticker.setPosY(0.8f)
                    snapSticker.setRotationDegreesClockwise(345.0f)
                    content.snapSticker = snapSticker
                }
                snapCreativeKitApi.send(content)
            } catch (e: SnapMediaSizeException) {
                Toast.makeText(
                    view.context,
                    "Media too large to share. Trim to 10sec & compress below 10mb",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: SnapVideoLengthException) {
                Toast.makeText(
                    view.context,
                    "Media too large to share. Trim to 10sec & compress below 10mb",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: SnapStickerSizeException) {
                Toast.makeText(
                    view.context,
                    "Media too large to share. Trim to 10sec & compress below 10mb",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        button = findViewById<View>(R.id.button) as Button
        button?.setOnClickListener { openActivity2() }
    }

    fun openActivity2() {
        val intent = Intent(this@MainActivity, Activity2::class.java)
        startActivity(intent)
    }

    public override fun onResume() {
        super.onResume()
        if (mSnapState == SnapState.VIDEO) {
            mPreviewVideo?.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            SnapState.IMAGE.getRequestCode() -> {
                if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                    handleImageSelect(intent)
                }
            }
            SnapState.VIDEO.getRequestCode() -> {
                if (resultCode == RESULT_OK && intent != null && intent.data != null) {
                    handleVideoSelect(intent)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(intent)
                if (resultCode == RESULT_OK) {
                    val resultUri = result.uri
                    // Update your app to use the cropped image
                    mSnapFile = File(resultUri.path)
                    val bitmap = BitmapFactory.decodeStream(FileInputStream(mSnapFile))
                    mPreviewImage?.setImageBitmap(bitmap)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    // Handle error here
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, intent)
            }
        }
    }

    private fun openMediaSelectDialog() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item)
        for (state in SnapState.values()) {
            adapter.add(state.optionText)
        }
        AlertDialog.Builder(this)
            .setAdapter(adapter) { _, which ->
                when (SnapState.values()[which]) {
                    SnapState.IMAGE -> selectMediaFromGallery(
                        "image/*",
                        SnapState.IMAGE.getRequestCode()
                    )
                    SnapState.CAMERA -> dispatchTakePictureIntent(
                        "camera/*",
                        SnapState.CAMERA.getRequestCode()
                    )
                    SnapState.VIDEO -> selectMediaFromGallery(
                        "video/*",
                        SnapState.VIDEO.getRequestCode()
                    )
                    SnapState.NO_SNAP -> reset()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent(mimeType: String, resultCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (mimeType === "camera/*") if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun selectMediaFromGallery(mimeType: String, resultCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        if (mimeType === "image/*") {
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mimeType)
        } else {
            intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mimeType)
        }
        startActivityForResult(intent, resultCode)
    }

    private fun handleImageSelect(intent: Intent?) {
        if (saveContentLocally(intent)) {
            reset()
            try {
                CropImage.activity(mSnapFile?.toUri())
                    .setAspectRatio(9, 16).setInitialCropWindowPaddingRatio(0f)
                    .start(this)
                val bitmap = BitmapFactory.decodeStream(FileInputStream(mSnapFile))
                mPreviewImage?.setImageBitmap(bitmap)

                mSnapState = SnapState.IMAGE
            } catch (e: FileNotFoundException) {
                throw IllegalStateException("Saved the image file, but it doesn't exist!")
            }
        }
    }



    private fun handleVideoSelect(intent: Intent?) {
        if (saveContentLocally(intent) || Uri.fromFile(mSnapFile) != null) {
            reset()
            mPreviewVideo?.setVideoURI(Uri.fromFile(mSnapFile))
            mPreviewVideo?.start()
            mPreviewVideo?.visibility = View.VISIBLE
            mSnapState = SnapState.VIDEO
        }
    }



    private fun reset() {
        mPreviewImage.setImageDrawable(null)
        mPreviewVideo?.visibility = View.GONE
        mPreviewVideo?.setVideoURI(null)
        mSnapState = SnapState.NO_SNAP
    }

    /**
     * Saves the file from the ACTION_PICK Intent locally to [.mSnapFile] to be accessed by our FileProvider
     */
    private fun saveContentLocally(intent: Intent?): Boolean {
        if (intent == null || intent.data == null) {
            return false
        }
        val inputStream: InputStream? = try {
            contentResolver.openInputStream(intent.data!!)
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "Could not open file", Toast.LENGTH_SHORT).show()
            return false
        }
        if (inputStream == null) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            copyFile(inputStream, mSnapFile)
        } catch (e: IOException) {
            Toast.makeText(this, "Failed save file locally", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {

        private const val SNAP_NAME = "snap"
        private const val STICKER_NAME = "sticker"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val SELECT_STICKER_REQUEST_CODE = 100
        @Throws(IOException::class)
        private fun copyFile(inputStream: InputStream, file: File?) {
            val buffer = ByteArray(1024)
            var length: Int
            FileOutputStream(file).use { outputStream ->
                while (inputStream.read(buffer).also {
                        length = it
                    } != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
    }
}
