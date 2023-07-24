/*
 * Foundation, the software collection powering the Xpdustry network.
 * Copyright (C) 2023  Xpdustry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.xpdustry.foundation.common.database.model

import com.xpdustry.foundation.common.FoundationCommonModule
import com.xpdustry.foundation.common.application.KotlinAbstractModule
import com.xpdustry.foundation.common.application.SimpleFoundationApplication
import com.xpdustry.foundation.common.config.FoundationConfig
import com.xpdustry.foundation.common.config.MongoConfig
import com.xpdustry.foundation.common.database.Database
import com.xpdustry.foundation.common.misc.ExitStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.util.Base64
import kotlin.random.Random

// TODO: Finish writing tests
@Testcontainers
class AccountServiceTest {
    private lateinit var application: SimpleFoundationApplication
    private lateinit var database: Database
    private lateinit var service: SimpleAccountService

    @BeforeEach
    fun init() {
        application = SimpleFoundationApplication(
            modules = listOf(FoundationCommonModule(), AccountServiceTestModule()),
            production = false,
        )

        database = application.instance(Database::class)
        service = application.instance(SimpleAccountService::class)

        application.init()
    }

    @AfterEach
    fun exit() {
        application.exit(ExitStatus.EXIT)
    }

    @Test
    fun `test simple registration`() {
        val username = randomUsername()

        StepVerifier.create(service.register(username, INVALID_PASSWORD))
            .expectError(AccountException.InvalidPassword::class.java)
            .verify()

        StepVerifier.create(service.register(username, TEST_PASSWORD_1))
            .verifyComplete()

        StepVerifier.create(service.register(username, TEST_PASSWORD_1))
            .expectError(AccountException.AlreadyRegistered::class.java)
            .verify()

        StepVerifier.create(database.accounts.findByUsername(username))
            .expectNextMatches { it.username == username }
            .verifyComplete()
    }

    @Test
    fun `test simple login`() {
        val username = randomUsername()
        val identity = randomPlayerIdentity()

        StepVerifier.create(service.login(username, TEST_PASSWORD_1, identity))
            .expectError(AccountException.NotRegistered::class.java)
            .verify()

        StepVerifier.create(service.register(username, TEST_PASSWORD_1))
            .verifyComplete()

        StepVerifier.create(service.login(username, TEST_PASSWORD_2, identity))
            .expectError(AccountException.WrongPassword::class.java)
            .verify()

        StepVerifier.create(service.login(username, TEST_PASSWORD_1, identity))
            .verifyComplete()

        StepVerifier.create(database.accounts.findByUsername(username))
            .expectNextMatches { it.sessions.contains(service.createSessionToken(identity).block()) }
            .verifyComplete()
    }

    private fun randomPlayerIdentity(): PlayerIdentity {
        val uuidBytes = ByteArray(16)
        Random.nextBytes(uuidBytes)
        val usidBytes = ByteArray(8)
        Random.nextBytes(usidBytes)
        return PlayerIdentity(
            Base64.getEncoder().encodeToString(uuidBytes),
            Base64.getEncoder().encodeToString(usidBytes),
        )
    }

    private fun randomUsername(): String {
        val chars = CharArray(16)
        for (i in chars.indices) {
            chars[i] = Random.nextInt('a'.code, 'z'.code).toChar()
        }
        return String(chars)
    }

    private class AccountServiceTestModule : KotlinAbstractModule() {
        override fun configure() {
            bind(FoundationConfig::class)
                .instance(FoundationConfig(mongo = MongoConfig(port = MONGO_CONTAINER.getMappedPort(27017))))
        }
    }

    companion object {
        @Container
        private val MONGO_CONTAINER = MongoDBContainer(DockerImageName.parse("mongo:6"))

        private val TEST_PASSWORD_1 = "ABc123!#".toCharArray()
        private val TEST_PASSWORD_2 = "123ABc!#".toCharArray()
        private val INVALID_PASSWORD = "1234".toCharArray()
    }
}
