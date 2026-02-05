package no.nav.dagpenger.doffen.domene

data class Node(
    val id: String,
    val typeId: TypeId,
) {
    fun toJson(): String = """"${typeId.id}": "$id""""
}

enum class TypeId(
    val id: String,
) {
    IDENT("ident"),
    SØKNADID("søknadId"),
    SAKID("sakId"),
    EKSTERNSAKID("eksternSakId"),
    BEHANDLINGID("behandlingId"),
    EKSTERNBEHANDLINGID("eksternBehandlingId"),
    BEHANDLINGSKJEDEID("behandlingskjedeId"),
    BASERTPÅID("basertPåId"),
    MELDEKORTID("meldekortId"),
    MANUELLID("manuellId"),
    OMGJØRINGID("omgjøringId"),
    UKJENTID("ukjentId"),
}
