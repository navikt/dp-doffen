package no.nav.dagpenger.doffen.domene

data class Gruppe(
    val type: GruppeType,
    val id: String,
    val noder: MutableList<Node> = mutableListOf(),
) {
    fun leggTilNode(nyNode: Node) {
        noder.add(nyNode)
    }

    fun toJson(): String {
        val noderJson = noder.joinToString(", ") { node -> node.toJson() }
        return """{ $noderJson }"""
    }
}

enum class GruppeType(
    val gruppe: String,
) {
    IDENT("ident"),
    SAK("sak"),
    SØKNAD("søknad"),
    BEHANDLING("behandling"),
    MELDEKORT("meldekort"),
    UTBETALING("utbetaling"),
}
