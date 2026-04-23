FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:9f89360c5b23398f6e834e13f82e4936488b8ee2e6c5d53d4946553380368a1f

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY build/install/*/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.doffen.AppKt"]
