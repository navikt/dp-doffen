package no.nav.dagpenger.doffen.api

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.jwt
import no.nav.dagpenger.doffen.api.auth.AuthFactory.azureAd

internal fun Application.authenticationConfig(
    auth: AuthenticationConfig.() -> Unit = {
        jwt("azureAd") { azureAd() }
    },
) {
    install(Authentication) {
        auth()
    }
}
