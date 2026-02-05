package no.nav.dagpenger.doffen.river

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.doffen.db.repo.NodeRepo
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId
import org.junit.jupiter.api.Test

class UtbetalingMottakTest {
    private val repo = mockk<NodeRepo>(relaxed = true)
    private val rapid =
        TestRapid().apply {
            UtbetalingMottak(
                repo = repo,
                rapidsConnection = this,
            )
        }

    @Test
    fun `mottar utbetaling melding`() {
        val treCapture = slot<Tre>()
        every { repo.lagreTre(capture(treCapture)) } returns Unit
        val json = javaClass.getResource("/meldinger/UtbetalingMottatt.json")!!.readText()
        rapid.sendTestMessage(json)

        treCapture.captured.let {
            it.ident shouldBe "04909296396"
            it.grupper.size shouldBe 1
            it.grupper.first().let { gruppe ->
                gruppe.id shouldBe "290bf8d8-3708-4e4c-8a3d-3c8fe47cd130"
                gruppe.type.name shouldBe "UTBETALING"
                gruppe.noder.size shouldBe 5
                gruppe.noder shouldContainAll
                    listOf(
                        Node(
                            id = "290bf8d8-3708-4e4c-8a3d-3c8fe47cd130",
                            typeId = TypeId.BEHANDLINGID,
                        ),
                        Node(
                            id = "AZv+1HqnenSkcAXfTI6sSQ==",
                            typeId = TypeId.EKSTERNBEHANDLINGID,
                        ),
                        Node(
                            id = "290bf8d8-3708-4e4c-8a3d-3c8fe47cd130",
                            typeId = TypeId.SAKID,
                        ),
                        Node(
                            id = "AZv+ldLefq260d09GEkigA==",
                            typeId = TypeId.EKSTERNSAKID,
                        ),
                        Node(
                            id = "019bfed3-da94-7e73-9c40-03ebcd58655d",
                            typeId = TypeId.MELDEKORTID,
                        ),
                    )
            }
        }
    }
}
