package no.nav.dagpenger.doffen.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.doffen.asUUID
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.domene.GruppeType.BEHANDLING
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId
import no.nav.dagpenger.doffen.domene.TypeId.BASERTPÅID
import no.nav.dagpenger.doffen.domene.TypeId.BEHANDLINGID
import no.nav.dagpenger.doffen.domene.TypeId.BEHANDLINGSKJEDEID
import no.nav.dagpenger.doffen.domene.TypeId.MELDEKORTID
import no.nav.dagpenger.doffen.domene.TypeId.SØKNADID
import no.nav.dagpenger.doffen.domene.TypeId.UKJENTID

internal class BehandlingOpprettetMottak(
    private val repo: NodeRepo,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "behandling_opprettet")
                }
                validate {
                    it.requireKey(
                        "ident",
                        "behandlingId",
                        "behandletHendelse",
                        "behandlingskjedeId",
                    )
                }
                validate { it.interestedIn("@id", "@opprettet", "basertPåBehandling") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val ident = packet["ident"].asText()
        val hendelseId = packet["behandletHendelse"]["id"].asText()
        val hendelseType = packet["behandletHendelse"]["type"].asText()
        val behandlingId = packet["behandlingId"].asUUID()
        val behandlingsKjedeId = packet["behandlingskjedeId"].asUUID()
        val basertPåBehandling = packet["basertPåBehandling"].takeIf { it.isTextual }?.asText()

        val tre = Tre(ident = ident)
        val gruppe =
            tre.leggTilGruppe(
                gruppeId = behandlingId.toString(),
                gruppeType = BEHANDLING,
            )

        gruppe.leggTilNode(
            Node(
                id = behandlingId.toString(),
                typeId = BEHANDLINGID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = behandlingsKjedeId.toString(),
                typeId = BEHANDLINGSKJEDEID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = hendelseId,
                typeId =
                    when (hendelseType) {
                        "Søknad" -> SØKNADID
                        "Meldekort" -> MELDEKORTID
                        "Manuell" -> TypeId.MANUELLID
                        "Omgjøring" -> TypeId.OMGJØRINGID
                        else -> UKJENTID
                    },
            ),
        )

        basertPåBehandling?.let {
            gruppe.leggTilNode(
                Node(
                    id = basertPåBehandling,
                    typeId = BASERTPÅID,
                ),
            )
        }

        repo.lagreTre(tre)
    }
}
