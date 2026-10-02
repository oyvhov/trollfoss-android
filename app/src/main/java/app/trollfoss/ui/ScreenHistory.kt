package app.trollfoss.ui

/** Menus return to the screen that opened them; a fresh app always opens the village map. */
sealed interface Screen {
    object Play : Screen
    object Map : Screen
    object Players : Screen
    class Creator(val editId: Int?) : Screen
    object Book : Screen
    object Tasks : Screen
    object ParentGate : Screen
    object Parent : Screen
}

internal class ScreenHistory {
    var current: Screen = Screen.Map
        private set
    private val previous = mutableListOf<Screen>()

    fun previousIsPlayers(): Boolean = previous.lastOrNull() == Screen.Players

    fun open(target: Screen): Screen {
        if (target == current) return current
        // Solving the gate opens its contents, without leaving the puzzle in the back path.
        if (current != Screen.ParentGate || target != Screen.Parent) previous.add(current)
        current = target
        return current
    }

    fun back(): Screen {
        current = if (previous.isNotEmpty()) previous.removeAt(previous.lastIndex) else Screen.Play
        return current
    }

    fun arrive(target: Screen): Screen {
        previous.clear()
        current = target
        return current
    }
}
