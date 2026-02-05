package no.nav.dagpenger.doffen

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import kotlin.let

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
