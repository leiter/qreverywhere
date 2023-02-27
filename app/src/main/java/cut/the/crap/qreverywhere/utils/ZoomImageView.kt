package cut.the.crap.qreverywhere.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max


class ZoomImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private val last = PointF()
    private val start = PointF()

    private val customMatrix: Matrix = Matrix()
    private val m = FloatArray(9)
    var origWidth = 0f
    var origHeight: Float = 0f
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mode: Int = NONE
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var saveScale = 1f


    private fun init(context: Context) {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        imageMatrix = customMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener { _, event ->
            mScaleDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX: Float = curr.x - last.x
                    val deltaY: Float = curr.y - last.y
                    val fixTransX: Float = getFixDragTrans(
                        deltaX, viewWidth,
                        origWidth * saveScale
                    )
                    val fixTransY: Float = getFixDragTrans(
                        deltaY, viewHeight,
                        origHeight * saveScale
                    )
                    customMatrix.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = abs(curr.x - start.x).toInt()
                    val yDiff = abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) {
                        performClick()
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = customMatrix
            invalidate()
            true
        }
    }

    private fun fixTrans() {
        customMatrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        val fixTransX: Float = getFixTrans(transX, viewWidth, origWidth * saveScale)
        val fixTransY: Float = getFixTrans(transY, viewHeight, origHeight * saveScale)
        if (fixTransX != 0f || fixTransY != 0f) customMatrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) {
            return -trans + minTrans
        }
        return if (trans > maxTrans) {
            -trans + maxTrans
        } else 0F
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0F
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        if (saveScale == 1f) {
            val scale: Float
            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight
            val scaleX = viewWidth / bmWidth.toFloat()
            val scaleY = viewHeight / bmHeight.toFloat()
            scale = max(scaleX, scaleY)
            customMatrix.setScale(scale, scale)

            // Center the image
            var yGap = viewHeight - scale * bmHeight.toFloat()
            var xGap = viewWidth - scale * bmWidth.toFloat()
            yGap /= 2f
            xGap /= 2f
            customMatrix.postTranslate(xGap, yGap)
            origWidth = viewWidth - 2 * xGap
            origHeight = viewHeight - 2 * yGap
            imageMatrix = customMatrix
        }
        fixTrans()
    }

    inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale: Float = saveScale
            saveScale *= mScaleFactor
            val minScale = 1f
            val maxScale = 3f
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            if (origWidth * saveScale <= viewWidth
                || origHeight * saveScale <= viewHeight
            ) {
                customMatrix.postScale(
                    mScaleFactor, mScaleFactor, viewWidth / 2,
                    viewHeight / 2
                )
            } else {
                customMatrix.postScale(
                    mScaleFactor, mScaleFactor,
                    detector.focusX, detector.focusY
                )
            }
            fixTrans()
            return true
        }
    }

    private companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val CLICK = 3
    }

}