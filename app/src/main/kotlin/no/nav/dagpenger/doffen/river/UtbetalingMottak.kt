package no.nav.dagpenger.doffen.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.doffen.asUUID
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.domene.GruppeType.UTBETALING
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId

internal class UtbetalingMottak(
    private val repo: NodeRepo,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "utbetaling_mottatt")
                }
                validate {
                    it.requireKey(
                        "ident",
                        "behandlingId",
                        "eksternBehandlingId",
                        "sakId",
                        "eksternSakId",
                        "meldekortId",
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
        val behandlingId = packet["behandlingId"].asUUID()
        val eksternBehandlingId = packet["eksternBehandlingId"].asText()
        val sakId = packet["sakId"].asText()
        val eksternSakId = packet["eksternSakId"].asText()
        val meldekortId = packet["meldekortId"].asUUID()

        val tre = Tre(ident = ident)
        val gruppe =
            tre.leggTilGruppe(
                gruppeId = behandlingId.toString(),
                gruppeType = UTBETALING,
            )

        gruppe.leggTilNode(
            Node(
                id = behandlingId.toString(),
                typeId = TypeId.BEHANDLINGID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = eksternBehandlingId,
                typeId = TypeId.EKSTERNBEHANDLINGID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = sakId,
                typeId = TypeId.SAKID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = eksternSakId,
                typeId = TypeId.EKSTERNSAKID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = meldekortId.toString(),
                typeId = TypeId.MELDEKORTID,
            ),
        )

        repo.lagreTre(tre)
    }
}
