plugins {
    id("common")
    application
}

private val ktorVersion = libs.versions.ktor.get()

dependencies {
    implementation(project(":dp-doffen-admin-kontrakt"))

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation("no.nav.dagpenger:oauth2-klient:2025.12.19-08.15.2e150cd55270")
    implementation(libs.bundles.postgres)

    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    implementation("com.github.navikt.tbd-libs:naisful-app:2025.11.04-10.54-c831038e")
    // Fjernet eksplisitt tbd-libs:kafka dependency - rapids-and-rivers kommer med sin egen versjon
    // som ikke aktiverer Kafka transaksjoner (som krever Cluster Authorization)
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation(libs.mockk)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation("com.github.navikt.tbd-libs:naisful-test-app:2025.11.04-10.54-c831038e")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.bundles.postgres.test)
}

application {
    mainClass = "no.nav.dagpenger.doffen.AppKt"
}
