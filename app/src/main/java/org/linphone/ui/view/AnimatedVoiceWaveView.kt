/*
 * Copyright (c) 2010-2024 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import org.linphone.R
import org.linphone.core.tools.Log

class AnimatedVoiceWaveView
    @JvmOverloads
    constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val TAG = "[Animated Voice Wave View]"
        private const val NUM_BARS = 5
        private const val ANIMATION_DURATION = 600L
        private const val MIN_BAR_HEIGHT_RATIO = 0.2f
        private const val MAX_BAR_HEIGHT_RATIO = 1.0f
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val barHeights = FloatArray(NUM_BARS) { MIN_BAR_HEIGHT_RATIO }
    private val animators = Array(NUM_BARS) { ValueAnimator() }
    
    private var isPlaying = false
    private var barWidth = 0f
    private var barSpacing = 0f
    private var maxBarHeight = 0f
    private var accentColor = ContextCompat.getColor(context, R.color.bc_white)

    init {
        setupAnimators()
    }

    private fun setupAnimators() {
        for (i in 0 until NUM_BARS) {
            animators[i] = ValueAnimator.ofFloat(MIN_BAR_HEIGHT_RATIO, MAX_BAR_HEIGHT_RATIO).apply {
                duration = ANIMATION_DURATION
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                
                addUpdateListener { animator ->
                    barHeights[i] = animator.animatedValue as Float
                    invalidate()
                }
                
                // Stagger the animations
                startDelay = (i * 80L)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val totalWidth = w.toFloat()
        val totalHeight = h.toFloat()
        
        barWidth = totalWidth / (NUM_BARS * 2 - 1) // Account for spacing
        barSpacing = barWidth
        maxBarHeight = totalHeight * 0.8f // Use 80% of available height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!isPlaying) {
            // Draw static bars when not playing
            drawStaticBars(canvas)
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val totalWidth = (NUM_BARS - 1) * (barWidth + barSpacing)
        val startX = centerX - totalWidth / 2f

        paint.color = accentColor

        for (i in 0 until NUM_BARS) {
            val x = startX + i * (barWidth + barSpacing)
            val barHeight = maxBarHeight * barHeights[i]
            val top = centerY - barHeight / 2f
            val bottom = centerY + barHeight / 2f

            canvas.drawRoundRect(
                x,
                top,
                x + barWidth,
                bottom,
                barWidth / 2f,
                barWidth / 2f,
                paint
            )
        }
    }

    private fun drawStaticBars(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val totalWidth = (NUM_BARS - 1) * (barWidth + barSpacing)
        val startX = centerX - totalWidth / 2f

        paint.color = Color.argb(128, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))

        for (i in 0 until NUM_BARS) {
            val x = startX + i * (barWidth + barSpacing)
            val barHeight = maxBarHeight * MIN_BAR_HEIGHT_RATIO
            val top = centerY - barHeight / 2f
            val bottom = centerY + barHeight / 2f

            canvas.drawRoundRect(
                x,
                top,
                x + barWidth,
                bottom,
                barWidth / 2f,
                barWidth / 2f,
                paint
            )
        }
    }

    fun setPlaying(playing: Boolean) {
        if (isPlaying == playing) return
        
        isPlaying = playing
        Log.i("$TAG Setting playing state to $playing")
        
        if (playing) {
            startAnimation()
        } else {
            stopAnimation()
        }
        invalidate()
    }

    fun setAccentColor(color: Int) {
        accentColor = color
        invalidate()
    }

    private fun startAnimation() {
        animators.forEach { it.start() }
    }

    private fun stopAnimation() {
        animators.forEach { it.cancel() }
        // Reset bar heights to minimum
        for (i in barHeights.indices) {
            barHeights[i] = MIN_BAR_HEIGHT_RATIO
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
