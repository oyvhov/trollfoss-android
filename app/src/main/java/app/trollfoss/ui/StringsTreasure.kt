package app.trollfoss.ui

import app.trollfoss.domain.Txt

object ST {
    val title = Txt("Rumle sine skattar", "Rumles skatter")
    val invitation = Txt("Tre biletspor. Kva har Rumle gøymt?", "Tre bildespor. Hva har Rumle gjemt?")
    val go = Txt("Finn neste spor", "Finn neste spor")
    val bag = Txt("Sjå i sekken", "Se i sekken")
    val found = Txt("Funne", "Funnet")
    val lantern = Txt("Trollykt", "Trollykt")
    val findLantern = Txt("Finn lykta mi", "Finn lykta mi")
    val done = Txt("Skatten er din! Trykk på lykta for lysbilete.", "Skatten er din! Trykk på lykta for lysbilder.")
    val hint = Txt("Finn biletkarta ved ugla, sandslottet og krystallane.", "Finn bildekartene ved ugla, sandslottet og krystallene.")
    fun clue(index: Int) = when(index) {
        0 -> Txt("Uglesporet", "Uglesporet")
        1 -> Txt("Skjelsporet", "Skjellsporet")
        else -> Txt("Krystallsporet", "Krystallsporet")
    }
}
