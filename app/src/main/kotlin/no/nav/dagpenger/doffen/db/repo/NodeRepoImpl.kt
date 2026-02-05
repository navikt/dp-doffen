package no.nav.dagpenger.doffen.db.repo

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.doffen.domene.GruppeType
import no.nav.dagpenger.doffen.domene.Node
import no.nav.dagpenger.doffen.domene.Tre
import no.nav.dagpenger.doffen.domene.TypeId
import javax.sql.DataSource

class NodeRepoImpl(
    private val dataSource: DataSource,
) : NodeRepo {
    override fun lagreTre(tre: Tre) {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tre.grupper.forEach { gruppe ->
                    gruppe.noder.forEach { node ->
                        val nodeDto =
                            NodeDTO(
                                gruppeId = gruppe.id,
                                gruppeType = gruppe.type,
                                id = node.id,
                                ident = tre.ident,
                                typeId = node.typeId,
                            )
//                        if (!sjekkOmIdentFinnes(nodeDto.ident, tx)) {
//                            val identNode =
//                                NodeDTO(
//                                    gruppeId = nodeDto.ident,
//                                    gruppeType = GruppeType.IDENT,
//                                    id = nodeDto.ident,
//                                    ident = nodeDto.ident,
//                                    typeId = TypeId.IDENT,
//                                )
//                            lagreNode(identNode, tx)
//                        }
                        lagreNode(nodeDto, tx)
                    }
                }
            }
        }
    }

    override fun lagre(liste: List<NodeDTO>) {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                liste.distinctBy { it.ident }.forEach { ident ->
                    if (!sjekkOmIdentFinnes(ident.ident, tx)) {
                        val identNode =
                            NodeDTO(
                                gruppeId = ident.ident,
                                gruppeType = GruppeType.IDENT,
                                id = ident.ident,
                                ident = ident.ident,
                                typeId = TypeId.IDENT,
                            )
                        lagreNode(identNode, tx)
                    }
                }
                liste.forEach { node ->
                    lagreNode(node, tx)
                }
            }
        }
    }

    override fun lagre(node: NodeDTO) {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                if (!sjekkOmIdentFinnes(node.ident, tx)) {
                    val ident =
                        NodeDTO(
                            gruppeId = node.ident,
                            gruppeType = GruppeType.IDENT,
                            id = node.ident,
                            ident = node.ident,
                            typeId = TypeId.IDENT,
                        )
                    lagreNode(ident, tx)
                }
                lagreNode(node, tx)
            }
        }
    }

    private fun lagreNode(
        node: NodeDTO,
        tx: TransactionalSession,
    ) {
        tx.run(
            queryOf(
                """
                INSERT INTO node (
                   gruppe_id,
                   gruppe_type,
                   ident, 
                   id, 
                   type_id
                ) VALUES (
                   :gruppeId,
                   :gruppeType,
                   :ident, 
                   :id, 
                   :typeId
                ) ON CONFLICT DO NOTHING
                """.trimIndent(),
                mapOf(
                    "gruppeId" to node.gruppeId,
                    "gruppeType" to node.gruppeType.name,
                    "ident" to node.ident,
                    "id" to node.id,
                    "typeId" to node.typeId.name,
                ),
            ).asUpdate,
        )
    }

    private fun sjekkOmIdentFinnes(
        ident: String,
        tx: TransactionalSession,
    ): Boolean =
        tx.run(
            queryOf(
                """
                SELECT COUNT(*) AS count
                FROM node
                WHERE ident = :ident
                  and gruppe_type = 'IDENT'
                """.trimIndent(),
                mapOf("ident" to ident),
            ).map { row -> row.int("count") > 0 }.asSingle,
        ) ?: false

    override fun hentTreForIdent(ident: String): Tre? =
        sessionOf(dataSource)
            .use { session ->
                session.run(
                    queryOf(
                        """
                        SELECT *
                        FROM node
                        WHERE ident = :ident
                        """.trimIndent(),
                        mapOf("ident" to ident),
                    ).map { row ->
                        row.toNode()
                    }.asList,
                )
            }.lagTre()

    override fun hentTreForId(id: String): Tre? =
        sessionOf(dataSource)
            .use { session ->
                session.run(
                    queryOf(
                        """
                        SELECT *
                        FROM node
                        WHERE id = :id
                        """.trimIndent(),
                        mapOf("id" to id),
                    ).map { row ->
                        row.toNode()
                    }.asList,
                )
            }.lagTre()

    private fun Row.toNode(): NodeDTO =
        NodeDTO(
            ident = this.string("ident"),
            gruppeId = this.string("gruppe_id"),
            gruppeType = GruppeType.valueOf(this.string("gruppe_type")),
            id = this.string("id"),
            typeId = TypeId.valueOf(this.string("type_id")),
        )
}

data class NodeDTO(
    val gruppeId: String,
    val gruppeType: GruppeType,
    val id: String,
    val ident: String,
    val typeId: TypeId,
)

fun List<NodeDTO>.lagTre(): Tre? {
    if (this.isEmpty()) return null
    val tre = Tre(this.first().ident)
    this.forEach { nodeDto ->
        val node =
            Node(
                id = nodeDto.id,
                typeId = nodeDto.typeId,
            )
        tre.leggTilNode(
            node = node,
            gruppeId = nodeDto.gruppeId,
            gruppeType = nodeDto.gruppeType,
        )
    }
    return tre
}
