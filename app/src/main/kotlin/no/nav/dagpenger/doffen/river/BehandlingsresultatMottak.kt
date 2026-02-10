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

internal class BehandlingsresultatMottak(
    private val repo: NodeRepo,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "behandlingsresultat")
                }
                validate {
                    it.requireKey(
                        "ident",
                        "behandlingId",
                        "behandlingskjedeId",
                        "behandletHendelse",
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
        val behandlingId = packet["behandlingId"].asUUID()
        val behandlingsKjedeId = packet["behandlingskjedeId"].asUUID()
        val basertPåBehandling = packet["basertPåBehandling"].takeIf { it.isTextual }?.asText()
        val hendelseId = packet["behandletHendelse"]["id"].asText()
        val hendelseType = packet["behandletHendelse"]["type"].asText()
        val tre = Tre(ident = ident)
        val gruppe =
            tre.leggTilGruppe(
                gruppeId = behandlingId.toString(),
                gruppeType = GruppeType.BEHANDLING,
            )

        gruppe.leggTilNode(
            Node(
                id = behandlingId.toString(),
                typeId = TypeId.BEHANDLINGID,
            ),
        )
        gruppe.leggTilNode(
            Node(
                id = behandlingsKjedeId.toString(),
                typeId = TypeId.BEHANDLINGSKJEDEID,
            ),
        )

        gruppe.leggTilNode(
            Node(
                id = hendelseId,
                typeId =
                    when (hendelseType) {
                        "Søknad" -> TypeId.SØKNADID
                        "Meldekort" -> TypeId.MELDEKORTID
                        "Manuell" -> TypeId.MANUELLID
                        "Omgjøring" -> TypeId.OMGJØRINGID
                        else -> TypeId.UKJENTID
                    },
            ),
        )

        if (basertPåBehandling != null) {
            gruppe.leggTilNode(
                Node(
                    id = basertPåBehandling,
                    typeId = TypeId.BASERTPÅID,
                ),
            )
        }

        repo.lagreTre(tre)
    }
}
