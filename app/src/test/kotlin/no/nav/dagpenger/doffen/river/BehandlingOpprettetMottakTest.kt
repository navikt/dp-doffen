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
import no.nav.dagpenger.doffen.domene.TypeId.BASERTPÅID
import no.nav.dagpenger.doffen.domene.TypeId.BEHANDLINGID
import no.nav.dagpenger.doffen.domene.TypeId.BEHANDLINGSKJEDEID
import no.nav.dagpenger.doffen.domene.TypeId.SØKNADID
import org.junit.jupiter.api.Test

class BehandlingOpprettetMottakTest {
    private val repo = mockk<NodeRepo>(relaxed = true)
    private val rapid =
        TestRapid().apply {
            BehandlingOpprettetMottak(
                repo = repo,
                rapidsConnection = this,
            )
        }

    @Test
    fun `mottar behandling opprettet melding`() {
        val treCapture = slot<Tre>()
        every { repo.lagreTre(capture(treCapture)) } returns Unit
        val json = javaClass.getResource("/meldinger/BehandlingOpprettet.json")!!.readText()
        rapid.sendTestMessage(json)

        treCapture.captured.let {
            it.ident shouldBe "04909296396"
            it.grupper.size shouldBe 1
            it.grupper.first().let { gruppe ->
                gruppe.id shouldBe "290bf8d8-3708-4e4c-8a3d-3c8fe47cd130"
                gruppe.type.name shouldBe "BEHANDLING"
                gruppe.noder.size shouldBe 4
                gruppe.noder shouldContainAll
                    listOf(
                        Node(
                            id = "290bf8d8-3708-4e4c-8a3d-3c8fe47cd130",
                            typeId = BEHANDLINGID,
                        ),
                        Node(
                            id = "733acf0f-46bb-4522-8bd2-b18333c2a97d",
                            typeId = SØKNADID,
                        ),
                        Node(
                            id = "290bf8d8-3708-4e4c-8a3d-3c8fe47cd131",
                            typeId = BEHANDLINGSKJEDEID,
                        ),
                        Node(
                            id = "190bf8d8-3708-4e4c-8a3d-3c8fe47cd138",
                            typeId = BASERTPÅID,
                        ),
                    )
            }
        }
    }
}
