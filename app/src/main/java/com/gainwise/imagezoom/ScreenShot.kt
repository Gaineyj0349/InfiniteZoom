package com.gainwise.imagezoom

import android.graphics.Bitmap
import android.view.View


class ScreenShot {

    companion object {
        fun takeScreenShot(v: View): Bitmap {
            v.isDrawingCacheEnabled = true
            v.buildDrawingCache(true)
            var b = Bitmap.createBitmap(v.drawingCache)
            v.isDrawingCacheEnabled = false
            return b;
        }


    }


}