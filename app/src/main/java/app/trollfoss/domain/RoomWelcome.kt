package app.trollfoss.domain

import kotlin.math.abs

object RoomWelcome {
    /**
     * Everyone close enough waves at the new room. The one standing nearest gets sawdust up the nose and
     * is returned, so the builder can play the sneeze. Nobody moves and no hat flies off: it is only dust.
     */
    fun greet(world: World, place: PlaceId, center: Float): Person? {
        val guests = world.people().filter { p ->
            p.place == place && p.species == Species.FOLK && !p.held && p.mode == Mode.FREE &&
                p.anim.face != Face.SLEEP && p.anim.pose == Pose.STAND && abs(p.x-center) <= 2.5f
        }
        for (p in guests) {
            p.anim.face = Face.GRIN
            p.anim.faceTime = 2.4f
            p.anim.wave = 1.5f
            p.anim.cheer = 1.8f
            p.anim.lookX = (center-p.x).coerceIn(-1f,1f)
        }
        return guests.minByOrNull { abs(it.x-center) }?.also {
            it.anim.face = Face.OOH
            it.anim.faceTime = SNEEZE_AFTER
        }
    }

    /** Seconds of «ah … ah …» before the sawdust wins. Matches the delay in the sound effect. */
    const val SNEEZE_AFTER = 0.9f
}
