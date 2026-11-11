package app.trollfoss.ui.berg

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/** Prefer crisp depth on overlapping sculpt details; retain support for older 16-bit EGL devices. */
internal class BergEglConfig:GLSurfaceView.EGLConfigChooser {
    override fun chooseConfig(egl:EGL10,display:EGLDisplay):EGLConfig {
        for(depth in intArrayOf(24,16)) {
            val attrs=intArrayOf(EGL10.EGL_RED_SIZE,8,EGL10.EGL_GREEN_SIZE,8,EGL10.EGL_BLUE_SIZE,8,
                EGL10.EGL_DEPTH_SIZE,depth,EGL10.EGL_RENDERABLE_TYPE,4,EGL10.EGL_NONE)
            val configs=arrayOfNulls<EGLConfig>(1);val count=IntArray(1)
            if(egl.eglChooseConfig(display,attrs,configs,1,count) && count[0]>0) configs[0]?.let {return it}
        }
        error("No compatible GLES2 surface")
    }
}
