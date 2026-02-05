package no.nav.dagpenger.doffen

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import kotlin.String

object Configuration {
    const val APP_NAME = "dp-doffen"

    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "RAPID_APP_NAME" to "dp-doffen",
                "KAFKA_CONSUMER_GROUP_ID" to "dp-doffen-v1",
                "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
                "KAFKA_RESET_POLICY" to "EARLIEST",
            ),
        )

    val utvikler = "7e7a9ef8-d9ba-445b-bb91-d2b3c10a0c13"

    internal val properties =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }

//    private val azureAdClient: CachedOauth2Client by lazy {
//        val azureAdConfig = OAuth2Config.AzureAd(config)
//        CachedOauth2Client(
//            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
//            authType = azureAdConfig.clientSecret(),
//        )
//    }
//
//    private fun azureAdTokenSupplier(scope: String): () -> String =
//        {
//            runBlocking { azureAdClient.clientCredentials(scope).access_token }
//                ?: throw RuntimeException("Kunne ikke hente 'access_token' fra Azure AD for scope $scope")
//        }
}
