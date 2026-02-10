package no.nav.dagpenger.doffen.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.domene.GruppeType
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId
import no.nav.doffen.kontrakt.api.models.BehandlingDTO
import no.nav.doffen.kontrakt.api.models.IdRequestDTO
import no.nav.doffen.kontrakt.api.models.MeldekortDTO
import no.nav.doffen.kontrakt.api.models.SakDTO
import no.nav.doffen.kontrakt.api.models.SoknadDTO
import no.nav.doffen.kontrakt.api.models.TreDTO
import no.nav.doffen.kontrakt.api.models.UtbetalingDTO

internal fun Application.doffenApi(repo: NodeRepo) {
    routing {
        swaggerUI(path = "openapi", swaggerFile = "doffen-open-api.yaml")

        get("/") {
            call.respond(HttpStatusCode.OK)
        }

        authenticate("azureAd") {
            post("tre/hentForId") {
                log.info("Vi har blitt kallt på /tre/hentForId")
                val id = call.receive<IdRequestDTO>()

                val tre = repo.hentTreForIdent(id.id)

                if (tre != null) {
                    log.info("Vi har funnet id som ident")
                    call.respond(HttpStatusCode.OK, tre.toDTO())
                } else {
                    log.info("Fant ikke ident. Vi leter videre om vi finner en id")
                    val ident =
                        repo.hentIdentForId(id.id) ?: return@post call.respond(
                            HttpStatusCode.NotFound,
                            "Fant ikke id eller ident for id ${id.id}",
                        )

                    val tre =
                        repo.hentTreForIdent(ident) ?: return@post call.respond(
                            HttpStatusCode.NotFound,
                            "Fant ikke tre for ident $ident",
                        )
                    call.respond(HttpStatusCode.OK, tre.toDTO())
                }
            }
        }
    }
}

private fun Tre.toDTO() =
    TreDTO(
        ident = this.ident,
        saker = this.grupper.filter { it.type == GruppeType.SAK }.map { it.noder.toSak() },
        søknader = this.grupper.filter { it.type == GruppeType.SØKNAD }.map { it.noder.toSøknad() },
        behandlinger = this.grupper.filter { it.type == GruppeType.BEHANDLING }.map { it.noder.toBehandling() },
        meldekort = this.grupper.filter { it.type == GruppeType.MELDEKORT }.map { it.noder.toMeldekort() },
        utbetalinger = this.grupper.filter { it.type == GruppeType.UTBETALING }.map { it.noder.toUtbetaling() },
    )

private fun List<Node>.toSak(): SakDTO =
    SakDTO(
        sakId = this.firstOrNull { it.typeId == TypeId.SAKID }?.id,
    )

private fun List<Node>.toSøknad(): SoknadDTO =
    SoknadDTO(
        søknadId = this.firstOrNull { it.typeId == TypeId.SØKNADID }?.id,
    )

private fun List<Node>.toBehandling(): BehandlingDTO =
    BehandlingDTO(
        behandlingId = this.firstOrNull { it.typeId == TypeId.BEHANDLINGID }?.id,
        behandlingsKjedeId = this.firstOrNull { it.typeId == TypeId.BEHANDLINGSKJEDEID }?.id,
        søknadId = this.firstOrNull { it.typeId == TypeId.SØKNADID }?.id,
        meldekortId = this.firstOrNull { it.typeId == TypeId.MELDEKORTID }?.id,
        manuellId = this.firstOrNull { it.typeId == TypeId.MANUELLID }?.id,
        omgjøringId = this.firstOrNull { it.typeId == TypeId.OMGJØRINGID }?.id,
        basertPåId = this.firstOrNull { it.typeId == TypeId.BASERTPÅID }?.id,
    )

private fun List<Node>.toMeldekort(): MeldekortDTO =
    MeldekortDTO(
        meldekortId = this.firstOrNull { it.typeId == TypeId.MELDEKORTID }?.id,
    )

private fun List<Node>.toUtbetaling(): UtbetalingDTO =
    UtbetalingDTO(
        sakId = this.firstOrNull { it.typeId == TypeId.SAKID }?.id,
        behandlingId = this.firstOrNull { it.typeId == TypeId.BEHANDLINGID }?.id,
        eksternBehandlingId = this.firstOrNull { it.typeId == TypeId.EKSTERNBEHANDLINGID }?.id,
        eksternSakId = this.firstOrNull { it.typeId == TypeId.EKSTERNSAKID }?.id,
        meldekortId = this.firstOrNull { it.typeId == TypeId.MELDEKORTID }?.id,
    )
