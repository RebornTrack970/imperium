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

import com.xpdustry.foundation.common.misc.PasswordRequirement
import com.xpdustry.foundation.common.misc.UsernameRequirement
import reactor.core.publisher.Mono

data class PlayerIdentity(val uuid: String, val usid: String)

interface AccountService {
    fun register(username: String, password: CharArray, allowReservedUsernames: Boolean = false): Mono<Void>
    fun migrate(oldUsername: String, newUsername: String, password: CharArray): Mono<Void>
    fun login(username: String, password: CharArray, identity: PlayerIdentity): Mono<Void>
    fun logout(identity: PlayerIdentity, all: Boolean = false): Mono<Boolean>
    fun refresh(identity: PlayerIdentity): Mono<Void>
    fun changePassword(oldPassword: CharArray, newPassword: CharArray, identity: PlayerIdentity): Mono<Void>
    fun findAccountByIdentity(identity: PlayerIdentity): Mono<Account>
}

sealed class AccountException : Exception() {
    class AlreadyRegistered : AccountException()
    class NotRegistered : AccountException()
    class NotLogged : AccountException()
    class WrongPassword : AccountException()
    class InvalidPassword(val missing: List<PasswordRequirement>) : AccountException()
    class InvalidUsername(val missing: List<UsernameRequirement>) : AccountException()
}
