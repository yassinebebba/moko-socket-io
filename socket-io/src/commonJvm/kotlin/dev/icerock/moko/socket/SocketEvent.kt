/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.socket

import io.socket.client.Socket

actual sealed class SocketEvent<T> : Mapper<T> {
    actual object Connect : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Socket.EVENT_CONNECT)
    }

    actual object Connecting : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_CONNECTING)
    }

    actual object Disconnect : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Socket.EVENT_DISCONNECT)
    }

    actual object Error : SocketEvent<Throwable>() {
        override val socketIoEvents: List<String> = listOf(
            Constants.EVENT_ERROR,
            Socket.EVENT_CONNECT_ERROR,
            Constants.EVENT_RECONNECT_ERROR
        )

        override fun mapper(array: Array<out Any>): Throwable {
            return when (val errorData = array[0]) {
                is Throwable -> errorData
                is String -> Exception(errorData)
                is Map<*, *> -> {
                    val message = errorData["message"] as? String ?: "Socket error"
                    Exception(message)
                }
                is org.json.JSONObject -> {
                    val message = try {
                        errorData.getString("message")
                    } catch (e: Exception) {
                        errorData.toString()
                    }
                    Exception(message)
                }
                else -> Exception("Socket error: $errorData")
            }
        }
    }

    actual object Message : SocketEvent<Any>() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_MESSAGE)
        override fun mapper(array: Array<out Any>): Any {
            return array
        }
    }

    actual object Reconnect : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_RECONNECT)
    }

    actual object ReconnectAttempt : SocketEvent<Int>() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_RECONNECT_ATTEMPT)
        override fun mapper(array: Array<out Any>): Int {
            return array[0] as Int
        }
    }

    actual object Ping : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_PING)
    }

    actual object Pong : SocketEvent<Unit>(), Mapper<Unit> by UnitMapper() {
        override val socketIoEvents: List<String> = listOf(Constants.EVENT_PONG)
    }

    abstract val socketIoEvents: List<String>

    private class UnitMapper : Mapper<Unit> {
        override fun mapper(array: Array<out Any>) = Unit
    }
}
