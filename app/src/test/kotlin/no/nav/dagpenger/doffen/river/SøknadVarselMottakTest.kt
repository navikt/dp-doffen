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
import no.nav.dagpenger.doffen.domene.TypeId.SØKNADID
import org.junit.jupiter.api.Test

class SøknadVarselMottakTest {
    private val repo = mockk<NodeRepo>(relaxed = true)
    private val rapid =
        TestRapid().apply {
            SøknadVarselMottak(
                repo = repo,
                rapidsConnection = this,
            )
        }

    @Test
    fun `mottar søknad melding`() {
        val treCapture = slot<Tre>()
        every { repo.lagreTre(capture(treCapture)) } returns Unit
        val json = javaClass.getResource("/meldinger/søknadInnsendtVarsel.json")!!.readText()
        rapid.sendTestMessage(json)

        treCapture.captured.let {
            it.ident shouldBe "04909296396"
            it.grupper.size shouldBe 1
            it.grupper.first().let { gruppe ->
                gruppe.id shouldBe "733acf0f-46bb-4522-8bd2-b18333c2a97d"
                gruppe.type.name shouldBe "SØKNAD"
                gruppe.noder.size shouldBe 1
                gruppe.noder shouldContainAll
                    listOf(
                        Node(
                            id = "733acf0f-46bb-4522-8bd2-b18333c2a97d",
                            typeId = SØKNADID,
                        ),
                    )
            }
        }
    }
}
