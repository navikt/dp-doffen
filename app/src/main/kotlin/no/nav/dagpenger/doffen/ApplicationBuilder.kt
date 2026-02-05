package no.nav.dagpenger.doffen

import com.github.navikt.tbd_libs.naisful.naisApp
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.doffen.api.authenticationConfig
import no.nav.dagpenger.doffen.api.doffenApi
import no.nav.dagpenger.doffen.db.PostgresConfiguration
import no.nav.dagpenger.doffen.db.PostgresConfiguration.dataSource
import no.nav.dagpenger.doffen.db.repo.NodeRepoImpl
import no.nav.dagpenger.doffen.river.BehandlingOpprettetMottak
import no.nav.dagpenger.doffen.river.BehandlingsresultatMottak
import no.nav.dagpenger.doffen.river.MeldekortInnsendtMottak
import no.nav.dagpenger.doffen.river.SøknadBehandlingsklarMottak
import no.nav.dagpenger.doffen.river.`SøknadVarselMottak`
import no.nav.dagpenger.doffen.river.UtbetalingMottak
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

internal class ApplicationBuilder(
    config: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val repo = NodeRepoImpl(dataSource)

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .create(
                env = config,
                builder = {
                    withKtor { preStopHook, rapid ->
                        naisApp(
                            meterRegistry =
                                PrometheusMeterRegistry(
                                    PrometheusConfig.DEFAULT,
                                    PrometheusRegistry.defaultRegistry,
                                    Clock.SYSTEM,
                                ),
                            objectMapper = objectMapper,
                            applicationLogger = LoggerFactory.getLogger("ApplicationLogger"),
                            callLogger = LoggerFactory.getLogger("CallLogger"),
                            aliveCheck = rapid::isReady,
                            readyCheck = rapid::isReady,
                            preStopHook = preStopHook::handlePreStopRequest,
                        ) {
                            authenticationConfig()
                            doffenApi(repo)
                        }
                    }
                },
            ).apply {
                SøknadVarselMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
                SøknadBehandlingsklarMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
                BehandlingsresultatMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
                BehandlingOpprettetMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
                MeldekortInnsendtMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
                UtbetalingMottak(
                    repo = repo,
                    rapidsConnection = this,
                )
            }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-doffen" }
        PostgresConfiguration.runMigration()
    }
}
