plugins {
    id("imperium.base-conventions")
    id("imperium.publishing-conventions")
}

dependencies {
    api(libs.guava)
    api(libs.hoplite.core)
    api(libs.hoplite.yaml)
    api(libs.deepl)
    api(libs.password4j)
    api(libs.rabbitmq.client)
    api(libs.snowflake.id)
    api(libs.okhttp)
    api(libs.caffeine)
    api(libs.minio)

    api(libs.slf4j.api)
    testApi(libs.slf4j.simple)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.jdk8)
    testApi(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.serialization.json)

    testApi(libs.classgraph)

    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)
    api(libs.exposed.json)
    api(libs.hikari)
    runtimeOnly(libs.mariadb)
    runtimeOnly(libs.h2)

    api(libs.prettytime)
    api(libs.time4j.core)
}
