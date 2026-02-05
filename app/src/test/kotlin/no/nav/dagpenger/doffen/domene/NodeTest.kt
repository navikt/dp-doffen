package no.nav.dagpenger.doffen.domene

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.doffen.db.repo.NodeDTO
import no.nav.dagpenger.doffen.db.repo.lagTre
import org.junit.jupiter.api.Test

class NodeTest {
    @Test
    fun `legg til node via gruppe`() {
        val tre = Tre("ident")
        val gruppe = tre.leggTilGruppe("gruppe1", GruppeType.SAK)

        gruppe.leggTilNode(
            Node(
                id = "node1",
                typeId = TypeId.SAKID,
            ),
        )
        gruppe.leggTilNode(
            Node(
                id = "node2",
                typeId = TypeId.SAKID,
            ),
        )

        tre.grupper.size shouldBe 1
        tre.grupper
            .first()
            .noder.size shouldBe 2
    }

    @Test
    fun `t est`() {
        val tre = Tre("ident")

        val identNode =
            Node(
                id = "ident",
                typeId = TypeId.IDENT,
            )

        tre.leggTilNode(identNode, gruppeId = identNode.id, gruppeType = GruppeType.IDENT)

        val ident2Node =
            Node(
                id = "ident2",
                typeId = TypeId.IDENT,
            )

        tre.leggTilNode(ident2Node, gruppeId = identNode.id, gruppeType = GruppeType.IDENT)

        val fagsakNode =
            Node(
                id = "fagsakId",
                typeId = TypeId.SAKID,
            )

        tre.leggTilNode(fagsakNode, gruppeId = fagsakNode.id, gruppeType = GruppeType.SAK)

        val søknadNode =
            Node(
                id = "søknadId",
                typeId = TypeId.BEHANDLINGID,
            )

        tre.leggTilNode(søknadNode, gruppeId = søknadNode.id, gruppeType = GruppeType.SØKNAD)

        val behandlingNode =
            Node(
                id = "behandling1Id",
                typeId = TypeId.BEHANDLINGID,
            )

        tre.leggTilNode(behandlingNode, gruppeId = behandlingNode.id, gruppeType = GruppeType.BEHANDLING)

        val basertPåNode =
            Node(
                id = "behandling2Id",
                typeId = TypeId.BASERTPÅID,
            )
        tre.leggTilNode(basertPåNode, gruppeId = behandlingNode.id, gruppeType = GruppeType.BEHANDLING)

        val behandlingNode2 =
            Node(
                id = "behandling2Id",
                typeId = TypeId.BEHANDLINGID,
            )

        tre.leggTilNode(behandlingNode2, gruppeId = behandlingNode2.id, gruppeType = GruppeType.BEHANDLING)

        val tt = emptyList<NodeDTO>().lagTre()

        println(tt)
//        behandlingNode.leggTilBarn(basertPåNode)
//        identNode.leggTilBarn(behandlingNode)

//        data class Tre(
//            val rot: Node,
//            val barn: List<Node> = emptyList(),
//        )
//
//        val noder = listOf(identNode, behandlingNode, basertPåNode, søknadNode)
//
//        Tre(
//            rot = identNode,
//            barn = listOf(søknadNode, behandlingNode),
//        )
//
//
//        fun byggTre(noder: List<Node>): Tre {
//            fun byggUnderTre(forelder: Node): Tre {
//                val barn = noder.filter { it.forelder == forelder.id }
//                return Tre(
//                    rot = forelder,
//                    barn = barn.flatMap { listOf(it) + byggUnderTre(it).barn },
//                )
//            }
//
//            val rot = noder.first { it.forelder == null }
//            return byggUnderTre(rot)
//        }
//
//        val tre = byggTre(noder)
        println(tre.toJson())
    }
}
