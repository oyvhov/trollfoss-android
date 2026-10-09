package app.trollfoss.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import app.trollfoss.domain.FigurePose
import app.trollfoss.domain.PersonAnim
import app.trollfoss.domain.Pose
import app.trollfoss.domain.Species

/** The same head transform as Anatomy.at; includes ears and hair, not just the facial features. */
internal fun DrawScope.figureHead(species: Species, a: PersonAnim, h: Float, pose: Pose = a.pose, block: DrawScope.() -> Unit) {
    val head = FigurePose.head(species, a, pose)
    withTransform({ translate(0f, head.dy * h); rotate(head.angle, Offset(0f, head.pivot * h)) }, block)
}

/** Broad cheeks taper into a soft muzzle rather than a perfect round head. */
internal fun DrawScope.petHead(c: Offset, r: Float, color: Color, pen: Pen) {
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        cubicTo(c.x - r*.78f,c.y-r*1.03f,c.x-r*1.05f,c.y-r*.54f,c.x-r,c.y)
        cubicTo(c.x-r*1.1f,c.y+r*.4f,c.x-r*.58f,c.y+r*.88f,c.x,c.y+r*.9f)
        cubicTo(c.x+r*.58f,c.y+r*.88f,c.x+r*1.1f,c.y+r*.4f,c.x+r,c.y)
        cubicTo(c.x+r*1.05f,c.y-r*.54f,c.x+r*.78f,c.y-r*1.03f,c.x,c.y-r)
        close()
    }
    inked(path,color,pen)
}

/** Four familiar face choices, now with shaped cheeks and a chin instead of an ellipse. */
internal fun folkFace(face: Int, h: Float): Path {
    val width = when (face) { 1 -> .226f; 3 -> .253f; else -> .238f }
    val chin = when (face) { 1 -> -.448f; 2 -> -.467f; else -> -.455f }
    return Path().apply {
        moveTo(0f, -.94f * h)
        cubicTo(-width * .76f * h, -.95f * h, -width * 1.03f * h, -.87f * h, -width * h, -.72f * h)
        cubicTo(-width * 1.06f * h, -.62f * h, -width * .94f * h, -.545f * h, -width * .59f * h, -.498f * h)
        quadraticTo(-.07f * h, chin * h, 0f, chin * h)
        quadraticTo(.09f * h, chin * h, width * .62f * h, -.50f * h)
        cubicTo(width * .97f * h, -.55f * h, width * 1.04f * h, -.64f * h, width * h, -.73f * h)
        cubicTo(width * h, -.88f * h, width * .74f * h, -.95f * h, 0f, -.94f * h)
        close()
    }
}
