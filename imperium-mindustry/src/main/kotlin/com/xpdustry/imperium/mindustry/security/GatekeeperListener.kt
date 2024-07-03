/*
 * Imperium, the software collection powering the Chaotic Neutral network.
 * Copyright (C) 2024  Xpdustry
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
package com.xpdustry.imperium.mindustry.security

import com.xpdustry.distributor.api.DistributorProvider
import com.xpdustry.distributor.api.plugin.MindustryPlugin
import com.xpdustry.distributor.api.util.Priority
import com.xpdustry.imperium.common.application.ImperiumApplication
import com.xpdustry.imperium.common.collection.enumSetAllOf
import com.xpdustry.imperium.common.config.MindustryConfig
import com.xpdustry.imperium.common.inject.InstanceManager
import com.xpdustry.imperium.common.inject.get
import com.xpdustry.imperium.common.misc.LoggerDelegate
import com.xpdustry.imperium.mindustry.translation.gatekeeper_failure
import com.xpdustry.sentinel.gatekeeper.GatekeeperPipeline
import com.xpdustry.sentinel.gatekeeper.GatekeeperResult
import com.xpdustry.sentinel.processing.Processor
import java.time.Duration
import java.util.concurrent.CompletableFuture

class GatekeeperListener(instances: InstanceManager) : ImperiumApplication.Listener {
    private val config = instances.get<MindustryConfig>()
    private val badWords = instances.get<BadWordDetector>()
    private val plugin = instances.get<MindustryPlugin>()

    override fun onImperiumInit() {
        if (!config.security.gatekeeper) {
            logger.warn("Gatekeeper is disabled. ONLY DO IT IN DEVELOPMENT.")
        }

        DistributorProvider.get()
            .serviceManager
            .register(
                plugin,
                GatekeeperPipeline.PROCESSOR_TYPE,
                Processor { ctx ->
                    val words = badWords.findBadWords(ctx.name, enumSetAllOf())
                    CompletableFuture.completedFuture(
                        if (words.isNotEmpty())
                            GatekeeperResult.Failure(
                                gatekeeper_failure("name.bad_word", words.toString()),
                                Duration.ZERO)
                        else GatekeeperResult.Success)
                },
                Priority.HIGH)
    }

    companion object {
        private val logger by LoggerDelegate()
    }
}
