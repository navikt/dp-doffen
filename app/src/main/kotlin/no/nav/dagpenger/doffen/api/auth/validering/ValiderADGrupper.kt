package no.nav.dagpenger.doffen.api.auth.validering

import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.dagpenger.doffen.Configuration

internal fun JWTAuthenticationProvider.Config.autoriserADGrupper() {
//    val utviklerGruppe = Configuration.Grupper.admin

    validate { jwtClaims ->
        jwtClaims.måInneholde(adGrupper = Configuration.utvikler)
        JWTPrincipal(jwtClaims.payload)
    }
}

private fun JWTCredential.måInneholde(adGrupper: List<String>) =
    require(
        this.payload.claims["groups"]
            ?.asList(String::class.java)
            ?.any { it in adGrupper } ?: false,
    ) { "Mangler tilgang" }
