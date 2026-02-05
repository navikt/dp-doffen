package no.nav.dagpenger.doffen.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.doffen.asUUID
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.domene.GruppeType
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId

internal class SøknadVarselMottak(
    private val repo: NodeRepo,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "søknad_innsendt_varsel")
                }
                validate {
                    it.requireKey(
                        "søknadId",
                        "ident",
                        "søknadstidspunkt",
                    )
                }
                validate { it.interestedIn("@id", "@opprettet") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val ident = packet["ident"].asText()
        val søknadId = packet["søknadId"].asUUID()

        val tre =
            Tre(ident = ident).also { tre ->
                tre.leggTilNode(
                    node =
                        Node(
                            id = søknadId.toString(),
                            typeId = TypeId.SØKNADID,
                        ),
                    gruppeId = søknadId.toString(),
                    gruppeType = GruppeType.SØKNAD,
                )
            }

        repo.lagreTre(tre)
    }
}
