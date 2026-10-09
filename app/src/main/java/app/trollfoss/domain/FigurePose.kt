package app.trollfoss.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Drawing and attachment points share this small, deterministic rig, in fractions of body height. */
object FigurePose {
    data class Point(val x: Float, val y: Float)
    data class Head(val angle: Float, val dy: Float, val pivot: Float) {
        fun at(x: Float, y: Float): Point {
            val r = angle * PI.toFloat() / 180f
            return Point(x * cos(r) - (y - pivot) * sin(r), pivot + x * sin(r) + (y - pivot) * cos(r) + dy)
        }
    }

    fun bob(a: PersonAnim, pose: Pose = a.pose): Float = if (a.motion && pose != Pose.LIE) sin(a.figureTime * 2.6f) * .0035f else 0f

    fun head(species: Species, a: PersonAnim, pose: Pose = a.pose): Head {
        val pivot = when (species) {
            Species.FOLK, Species.GHOST -> -.70f
            Species.ROBOT -> -.725f
            Species.PUFFIN -> -.72f
            Species.CHICKEN -> -.70f
            else -> -.62f
        }
        val angle = when {
            !a.motion || pose == Pose.LIE || a.face == Face.SLEEP -> 0f
            pose == Pose.HELD -> sin(a.figureTime * 5f) * 5f
            a.dance > 0f -> sin(a.dance) * 7f
            a.face == Face.DIZZY -> sin(a.figureTime * 9f) * 5f
            else -> a.lookX.coerceIn(-1f, 1f) * 6f + (if (species == Species.FOLK && a.wave > 0f) -3f else 0f)
        }
        val dip = if (species == Species.ROBOT) FigurarPose.dip(pose, a.wave) else 0f
        val peck = if (species == Species.CHICKEN && a.motion && (a.talk > 0f || a.chew > 0f)) kotlin.math.abs(sin(a.figureTime * 14f)) * .03f else 0f
        return Head(angle, bob(a, pose) + dip + peck, pivot)
    }

    /** The occupied hand stays on its activity anchor, including when the other hand waves. */
    fun hand(a: PersonAnim, right: Boolean, holding: Boolean, pose: Pose = a.pose): Point {
        if (right) PersonPlay.hand(a.activity)?.let { return Point(it[0], it[1]) }
        if (right && (holding || a.catching)) return Point(.245f, -.245f)
        val side = if (right) 1f else -1f
        val t = if (a.motion) a.figureTime else 0f
        val active = pose != Pose.LIE && a.face != Face.SLEEP
        return when {
            pose == Pose.HELD -> Point(side * .28f, -.63f + if (a.motion) side * sin(t * 11f) * .018f else 0f)
            pose == Pose.FLOAT -> Point(side * .32f, -.44f + if (a.motion) side * sin(t * 3f) * .025f else 0f)
            pose == Pose.SWIM -> Point(side * .29f, -.4f + if (a.motion) side * sin(t * 6f) * .04f else 0f)
            active && a.dance > 0f && a.motion -> Point(side * .28f, -.43f - side * sin(a.dance) * .14f)
            active && a.wave > 0f && (right || holding) -> Point(side * (.29f + if (a.motion) sin(t * 12f) * .025f else 0f), -.65f)
            !right && (a.activity == PersonPlay.READ || a.activity == PersonPlay.HUG) -> Point(-.14f, -.35f)
            active && a.face == Face.WOW -> Point(side * .27f, -.43f)
            else -> Point(side * .24f, -.25f + if (a.motion && !a.walkTo.isNaN()) side * sin(a.walkPhase * PI.toFloat()) * .025f else 0f)
        }
    }

    fun posed(species: Species, pose: Pose, p: Point): Point = if (!Anatomy.upright(species)) p else when (pose) {
        Pose.SIT -> Point(p.x, p.y + Anatomy.HIPS)
        Pose.LIE -> Point(p.y + .5f, -p.x - .22f)
        else -> p
    }

    fun headPart(part: Part): Boolean = part == Part.HAT || part == Part.GLASSES || part == Part.MOUTH || part == Part.HAIR || part == Part.HEAD

    fun attachmentAngle(species: Species, a: PersonAnim, slot: Slot): Float {
        val followsHead = slot != Slot.HAND || !Anatomy.upright(species) || species == Species.GHOST
        return (if (followsHead) head(species, a).angle else 0f) +
            (if (Anatomy.upright(species) && a.pose == Pose.LIE) -90f else 0f)
    }
}
