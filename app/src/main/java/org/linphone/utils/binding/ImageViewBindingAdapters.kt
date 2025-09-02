package org.linphone.utils.binding

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.linphone.R
import android.os.Build
import android.media.ExifInterface
import java.io.File

object ImageViewBindingAdapters {
    @JvmStatic
    @BindingAdapter("imageFilePath")
    fun setImageFilePath(imageView: ImageView, path: String?) {
        if (path.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.user_circle)
            imageView.imageTintList = imageView.context.getColorStateList(android.R.color.white)
            return
        }

        try {
            val cleanPath = path.substringBefore("?")
            val file = File(cleanPath)
            if (file.exists()) {
                // Clear previous drawable to force refresh
                imageView.setImageDrawable(null)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val rotated = applyExifOrientation(bitmap, file)
                    imageView.setImageBitmap(rotated)
                    imageView.imageTintList = null
                    imageView.invalidate()
                } else {
                    imageView.setImageResource(R.drawable.user_circle)
                    imageView.imageTintList = imageView.context.getColorStateList(android.R.color.white)
                }
            } else {
                imageView.setImageResource(R.drawable.user_circle)
                imageView.imageTintList = imageView.context.getColorStateList(android.R.color.white)
            }
        } catch (_: Exception) {
            imageView.setImageResource(R.drawable.user_circle)
            imageView.imageTintList = imageView.context.getColorStateList(android.R.color.white)
        }
    }

    private fun applyExifOrientation(source: Bitmap, file: File): Bitmap {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val exif = ExifInterface(file.absolutePath)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(source, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(source, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(source, 270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(source, horizontal = true, vertical = false)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(source, horizontal = false, vertical = true)
                    ExifInterface.ORIENTATION_TRANSPOSE -> rotateAndFlip(source, 90f, true)
                    ExifInterface.ORIENTATION_TRANSVERSE -> rotateAndFlip(source, 270f, true)
                    else -> source
                }
            } else {
                source
            }
        } catch (_: Exception) {
            source
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun flipBitmap(source: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix().apply { postScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun rotateAndFlip(source: Bitmap, angle: Float, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            postRotate(angle)
            postScale(if (horizontal) -1f else 1f, 1f)
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
