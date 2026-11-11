package app.trollfoss.domain

/** The kettle and the finished tea table live in the save. Gestures only last while visiting. */
enum class BerryPot { EMPTY, BERRIES, STEEPED }

object BergKitchen {
    val patch = BergPoint(5.75f, 11.7f)
    val picking = BergSpot(BergAction.BERRIES, 5.5f, 12.9f, false, 2.1f)
    val hearth = BergSpot(BergAction.KETTLE, -17f, -3.65f, true, 2.1f)
    val serving = BergPoint(-1.47f, -2.08f)
    const val TRAY_X = -1.77f
    const val TRAY_TOP = 1.04f
    const val TRAY_Z = -2.86f

    fun next(state: HighlandsState): BergAction {
        val inside = when {
            BergItem.BERRY_CUP in state.bag -> BergAction.TROLL
            state.pot != BerryPot.EMPTY || BergItem.BERRIES in state.bag -> BergAction.KETTLE
            else -> BergAction.BERRIES
        }
        if (inside == BergAction.BERRIES && state.cave) return BergAction.EXIT
        if (inside != BergAction.BERRIES && !state.cave) return BergAction.ENTER
        return inside
    }

    fun use(state: HighlandsState, action: BergAction): BergEvent {
        if (action == BergAction.BERRIES) {
            if (BergItem.BERRIES in state.bag) return BergEvent.BASKET_FULL
            state.bag.add(BergItem.BERRIES)
            state.held = BergItem.BERRIES
            return BergEvent.BERRIES
        }
        return when (state.pot) {
            BerryPot.EMPTY -> {
                if (!state.bag.remove(BergItem.BERRIES)) return BergEvent.NEED_BERRIES
                if (state.held == BergItem.BERRIES) state.held = BergItem.NONE
                state.pot = BerryPot.BERRIES
                BergEvent.FILL
            }
            BerryPot.BERRIES -> {
                state.pot = BerryPot.STEEPED
                BergEvent.STIR
            }
            BerryPot.STEEPED -> {
                if (BergItem.BERRY_CUP in state.bag) return BergEvent.CUP_FULL
                state.pot = BerryPot.EMPTY
                state.bag.add(BergItem.BERRY_CUP)
                state.held = BergItem.BERRY_CUP
                BergEvent.POUR
            }
        }
    }
}

enum class RumleGesture(val seconds: Float) {
    REST(0f), WAVE(2.55f), DRINK(5.15f), EAT(3.15f), TOAST(3.8f)
}

data class RumleMoment(
    val gesture: RumleGesture = RumleGesture.REST,
    val elapsed: Float = 0f,
    val item: BergItem = BergItem.NONE,
    val guest: Int = -1,
) {
    companion object { val rest = RumleMoment() }
}

/** Transfer the selected serving once. Audio cues occur when the hand reaches the mouth. */
internal class RumleVisit {
    var moment = RumleMoment.rest
        private set

    fun wave() { moment = RumleMoment(RumleGesture.WAVE) }

    fun welcome() {
        if (moment.gesture == RumleGesture.REST) moment = RumleMoment(RumleGesture.WAVE)
    }

    fun clear() { moment = RumleMoment.rest }

    fun start(state: HighlandsState): BergEvent {
        if (moment.gesture !in setOf(RumleGesture.REST, RumleGesture.WAVE)) return BergEvent.NONE
        val item = state.held
        val gesture = when (item) {
            BergItem.PEPSI, BergItem.URGE -> RumleGesture.DRINK
            BergItem.CAKE -> RumleGesture.EAT
            BergItem.BERRY_CUP -> RumleGesture.TOAST
            else -> RumleGesture.WAVE
        }
        if (gesture == RumleGesture.WAVE) {
            moment = RumleMoment(gesture)
            return BergEvent.WAVE
        }
        if (!state.bag.remove(item)) return BergEvent.NONE
        state.held = BergItem.NONE
        if (gesture == RumleGesture.DRINK) state.lastBottle = item
        if (gesture == RumleGesture.TOAST) state.sharedJuice = true
        moment = RumleMoment(gesture, item = item, guest = state.leader)
        return BergEvent.OFFER
    }

    fun step(dt: Float): BergEvent {
        val old = moment
        if (old.gesture == RumleGesture.REST) return BergEvent.NONE
        val age = old.elapsed + dt
        moment = if (age >= old.gesture.seconds) RumleMoment.rest else old.copy(elapsed = age)
        fun crossed(t: Float) = old.elapsed < t && age >= t
        return when (old.gesture) {
            RumleGesture.DRINK -> when {
                crossed(1.62f) -> BergEvent.GULP
                crossed(3.23f) -> BergEvent.BURP
                crossed(4.14f) -> BergEvent.FART
                else -> BergEvent.NONE
            }
            RumleGesture.EAT -> if (crossed(1.62f)) BergEvent.CAKE else BergEvent.NONE
            RumleGesture.TOAST -> when {
                crossed(1.15f) -> BergEvent.TOAST
                crossed(2.05f) -> BergEvent.SIP
                else -> BergEvent.NONE
            }
            else -> BergEvent.NONE
        }
    }
}
