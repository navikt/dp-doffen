package no.nav.dagpenger.doffen.api

import com.github.navikt.tbd_libs.naisful.test.TestContext
import com.github.navikt.tbd_libs.naisful.test.naisfulTestApp
import io.kotest.assertions.json.shouldEqualSpecifiedJson
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.doffen.api.TestApplication.autentisert
import no.nav.dagpenger.doffen.api.TestApplication.withMockAuthServerAndTestApplication
import no.nav.dagpenger.doffen.db.repo.NodeDTO
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.db.repo.lagTre
import no.nav.dagpenger.doffen.domene.GruppeType
import no.nav.dagpenger.doffen.domene.TypeId
import no.nav.dagpenger.doffen.objectMapper
import org.junit.jupiter.api.Test

class DoffenApiTest {
    val repo = mockk<NodeRepo>()

    @Test
    fun `Doffen API loads successfully`() {
        naisfulTestApp(
            testApplicationModule = {
                authenticationConfig()
                doffenApi(repo)
            },
            objectMapper = objectMapper,
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
        ) {
            client.get("/").apply {
                status shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `feilmelding når json ikke er gyldig`() {
        coEvery { repo.hentTreForIdent(any()) } returns null

        medSikretApi(repo) { context ->
            context
                .autentisert(
                    HttpMethod.Post,
                    "/tre/hentForIdent",
                    body =
                        """
                        {
                           "ident" : "1",
                            "ugyldigFelt"
                        }
                        """.trimIndent(),
                ).apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
        }
    }

    @Test
    fun `hent for en ident uten treff returnerer en tom liste`() {
        coEvery { repo.hentTreForIdent(any()) } returns null

        medSikretApi(repo) { context ->
            context
                .autentisert(
                    HttpMethod.Post,
                    "/tre/hentForIdent",
                    body = """{"ident":"1"}""",
                ).apply {
                    status shouldBe HttpStatusCode.NotFound
                }
        }
    }

    @Test
    fun `hent for en id uten treff returnerer en tom liste`() {
        coEvery { repo.hentTreForIdent(any()) } returns null

        medSikretApi(repo) { context ->
            context
                .autentisert(
                    HttpMethod.Post,
                    "/tre/hentForIdent",
                    body = """{"ident":"1"}""",
                ).apply {
                    status shouldBe HttpStatusCode.NotFound
                }
        }
    }

    @Test
    fun `hent for en ident uten ident funker`() {
        val tre =
            listOf(
                NodeDTO(
                    id = "id-1",
                    typeId = TypeId.SØKNADID,
                    gruppeId = "gruppeId",
                    gruppeType = GruppeType.SØKNAD,
                    ident = "1",
                ),
                NodeDTO(
                    id = "id-2",
                    typeId = TypeId.SAKID,
                    gruppeId = "gruppeId",
                    gruppeType = GruppeType.SAK,
                    ident = "1",
                ),
            ).lagTre()

        coEvery { repo.hentTreForIdent(any()) } returns tre

        medSikretApi(repo) { context ->
            context
                .autentisert(
                    HttpMethod.Post,
                    "/tre/hentForIdent",
                    body = """{"ident":"1"}""",
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldEqualSpecifiedJson """
                        {
                          "ident": "1",
                          "saker": [],
                          "søknader": [
                            {
                              "søknadId": "id-1"
                            }
                          ],
                          "behandlinger": [],
                          "meldekort": [],
                          "utbetalinger": []
                        }
                        """
                }
        }
    }

    @Test
    fun `hent for en ident kan returnere en liste`() {
        val tre =
            listOf(
                NodeDTO(
                    id = "12345678901",
                    typeId = TypeId.IDENT,
                    gruppeId = "ident",
                    gruppeType = GruppeType.IDENT,
                    ident = "12345678901",
                ),
                NodeDTO(
                    id = "søknad-id",
                    typeId = TypeId.SØKNADID,
                    gruppeId = "gruppeId",
                    gruppeType = GruppeType.SØKNAD,
                    ident = "12345678901",
                ),
                NodeDTO(
                    id = "sak-id",
                    typeId = TypeId.SAKID,
                    gruppeId = "sakId",
                    gruppeType = GruppeType.SAK,
                    ident = "12345678901",
                ),
            ).lagTre()

        coEvery { repo.hentTreForIdent(any()) } returns tre

        medSikretApi(repo) { context ->
            context
                .autentisert(
                    HttpMethod.Post,
                    "/tre/hentForIdent",
                    body = """{"ident":"1"}""",
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldEqualSpecifiedJson """
                        {
                          "ident": "12345678901",
                          "saker": [
                            {
                              "sakId": "sak-id"
                            }
                          ],
                          "søknader": [
                            {
                              "søknadId": "søknad-id"
                            }
                          ],
                          "behandlinger": [],
                          "meldekort": [],
                          "utbetalinger": []
                        }
                        """
                }
        }
    }

    private fun medSikretApi(
        repo: NodeRepo = mockk(),
        block: suspend (TestContext) -> Unit,
    ) {
        withMockAuthServerAndTestApplication(
            {
                authenticationConfig()
                doffenApi(
                    repo,
                )
            },
        ) { block(this) }
    }
}
