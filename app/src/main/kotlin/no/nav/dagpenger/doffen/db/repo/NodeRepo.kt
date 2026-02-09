package no.nav.dagpenger.doffen.db.repo

import no.nav.dagpenger.doffen.domene.Tre

interface NodeRepo {
    fun lagre(node: NodeDTO)

    fun lagre(liste: List<NodeDTO>)

    fun lagreTre(tre: Tre)

    fun hentTreForIdent(ident: String): Tre?

    fun hentIdentForId(id: String): String?
}
