package com.example.contact_app_recycler_view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView

object AvatarHelper {

    private val avatarColors = listOf(
        0xFF1ABC9C.toInt(), 0xFF2ECC71.toInt(), 0xFF3498DB.toInt(),
        0xFF9B59B6.toInt(), 0xFFE74C3C.toInt(), 0xFFE67E22.toInt(),
        0xFFF39C12.toInt(), 0xFF16A085.toInt(), 0xFF8E44AD.toInt(),
        0xFF2980B9.toInt(), 0xFF27AE60.toInt(), 0xFFD35400.toInt()
    )

    fun getColorForName(name: String): Int {
        val index = if (name.isEmpty()) 0 else Math.abs(name.hashCode()) % avatarColors.size
        return avatarColors[index]
    }

    fun getInitials(name: String): String {
        if (name.isBlank()) return "?"
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            else -> parts[0][0].uppercase()
        }
    }

    /**
     * If imagePath is not null/blank, load that bitmap.
     * Otherwise render a colored initials circle.
     */
    fun setAvatar(imageView: ImageView, name: String, imagePath: String?) {
        if (!imagePath.isNullOrBlank()) {
            try {
                val bmp = BitmapFactory.decodeFile(imagePath)
                if (bmp != null) {
                    imageView.setImageBitmap(circleCrop(bmp))
                    return
                }
            } catch (_: Exception) {}
        }
        // Fallback: initials avatar
        setAvatarInitial(imageView, name)
    }

    fun setAvatarInitial(imageView: ImageView, name: String) {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.color = getColorForName(name)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.WHITE
        textPaint.textSize = size * 0.38f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true

        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(getInitials(name), size / 2f, textY, textPaint)

        imageView.setImageBitmap(bitmap)
    }

    private fun circleCrop(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squared = Bitmap.createBitmap(source, x, y, size, size)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squared, 0f, 0f, paint)
        return output
    }
}