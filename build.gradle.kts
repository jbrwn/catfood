import org.jooq.meta.jaxb.Logging

group = "dog.catfood"

plugins {
    kotlin("jvm") version "1.8.0"
    id("io.ktor.plugin") version "2.2.3"
    id("org.flywaydb.flyway") version "9.14.1"
    id("nu.studer.jooq") version "8.1"
}

application {
//    mainClass.set("io.ktor.server.netty.EngineMain")
    mainClass.set("dog.catfood.ApplicationKt")
}

repositories {
    mavenCentral()
}

val flywayMigration = configurations.create("flywayMigration")

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.2.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.3")
    implementation("io.ktor:ktor-server-config-yaml:2.2.3")
    implementation("io.ktor:ktor-server-call-logging:2.2.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.3")
    implementation("io.ktor:ktor-server-double-receive:2.2.3")
    implementation("io.ktor:ktor-server-auth:2.2.3")
    implementation("io.ktor:ktor-server-freemarker:2.2.3")
    implementation("io.ktor:ktor-server-sessions:2.2.3")
    implementation("io.ktor:ktor-serialization-jackson:2.2.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.72")
    implementation("org.hibernate:hibernate-validator:8.0.0.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")
    implementation("no.api.freemarker:freemarker-java8:2.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.insert-koin:koin-core:3.3.2")
    implementation("io.insert-koin:koin-ktor:3.3.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.redisson:redisson:3.19.3")
    implementation("org.flywaydb:flyway-core:9.14.1")
    implementation("org.jooq:jooq:3.17.7")
    implementation("org.jooq:jooq-kotlin:3.17.7")
    implementation("org.jooq:jooq-kotlin-coroutines:3.17.7")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")
    implementation("org.postgresql:postgresql:42.5.3")
    jooqGenerator("org.postgresql:postgresql:42.5.3")
    flywayMigration("org.postgresql:postgresql:42.5.3")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.3")
//    testImplementation("io.insert-koin:koin-test:3.3.2")
//    testImplementation("io.insert-koin:koin-test-junit5:3.3.2")
}

flyway {
    url = "jdbc:postgresql://localhost:6000/catfood"
    user = "catfood"
    password = "catfood"
    configurations = arrayOf("flywayMigration")
}

jooq {
    version.set("3.17.7")
    configurations {
        create("main") {  // name of the jOOQ configuration
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:6000/catfood"
                    user = "catfood"
                    password = "catfood"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                    }
                    target.apply {
                        packageName = "dog.catfood.jooq"
                    }
                }
            }
        }
    }
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
