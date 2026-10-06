package app.trollfoss.ui.play

/**
 * Rings on the water: at most [max] at once, each living [life] seconds. Fixed arrays, so nothing is allocated
 * while the scene is drawn; when full, a new ring takes the place of the oldest.
 */
class Ripples(private val max: Int = 12, val life: Float = 1.2f) {
    private val xs = FloatArray(max)
    private val ys = FloatArray(max)
    private val ages = FloatArray(max)
    var count = 0
        private set

    fun x(i: Int) = xs[i]
    fun y(i: Int) = ys[i]
    fun age(i: Int) = ages[i]

    fun add(x: Float, y: Float) {
        val i = if (count < max) count++ else (0 until count).maxBy { ages[it] }
        xs[i] = x; ys[i] = y; ages[i] = 0f
    }

    fun step(dt: Float) {
        var w = 0
        for (r in 0 until count) {
            val a = ages[r] + dt
            if (a >= life) continue
            xs[w] = xs[r]; ys[w] = ys[r]; ages[w] = a; w++
        }
        count = w
    }
}
