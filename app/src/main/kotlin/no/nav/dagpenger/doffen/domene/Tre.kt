package no.nav.dagpenger.doffen.domene

data class Tre(
    val ident: String,
    val grupper: MutableList<Gruppe> = mutableListOf(),
) {
    fun leggTilNode(
        node: Node,
        gruppeId: String,
        gruppeType: GruppeType,
    ) {
        val gruppe = leggTilGruppe(gruppeId, gruppeType)
        gruppe.leggTilNode(node)
    }

    fun leggTilNode(
        node: Node,
        gruppe: Gruppe,
    ) {
        gruppe.leggTilNode(node)
    }

    fun leggTilGruppe(
        gruppeId: String,
        gruppeType: GruppeType,
    ): Gruppe {
        val gruppe =
            grupper.find { it.id == gruppeId && it.type == gruppeType } ?: Gruppe(
                type = gruppeType,
                id = gruppeId,
            ).also { grupper.add(it) }

        return gruppe
    }

    fun toJson(): String {
        val grupperJson =
            grupper
                .filterNot { it.type == GruppeType.IDENT }
                .groupBy { it.type }
                .entries
                .joinToString(", ") { (gruppeType, grupperListe) ->
                    """"${gruppeType.gruppe}": [ ${grupperListe.joinToString(", ") { it.toJson() }} ]"""
                }
        val ident =
            grupper
                .firstOrNull { it.type == GruppeType.IDENT }
                ?.noder
                ?.firstOrNull()
                ?.id ?: "ukjent"

        return if (grupper.isEmpty()) {
            """{}"""
        } else {
            """{ "ident": "$ident", $grupperJson }"""
        }
    }
}
