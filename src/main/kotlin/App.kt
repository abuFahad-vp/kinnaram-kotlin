package com.kilafath

import java.net.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class App {
    fun serverStart() = runBlocking {
        val port = 8000
        val serverSocket = ServerSocket(port)
        println("Server started, listening on $port")
        val channel = Channel<Message>()
        launch(Dispatchers.IO) {
            server(channel)
        }

        while (true) {
            val clientSocket = serverSocket.accept()
            channel.send(Message.ClientConnected(clientSocket))
            launch(Dispatchers.IO) {
                handleClient(clientSocket,channel)
            }
        }
    }

    private suspend fun server(messages: Channel<Message>) {
        val clients = mutableMapOf<String, Socket>()
        for (client in messages) {
            when (client) {
                is Message.ClientConnected -> {
                    println("Client connected: ${client.socket.inetAddress.hostAddress}")
                    clients[client.socket.inetAddress.hostAddress] = client.socket
                }
                is Message.ClientDisconnected -> {
                    println("Client disconnected: ${client.socket.inetAddress.hostAddress}")
                    clients.remove(client.socket.inetAddress.hostAddress)
                }
                is Message.NewMessage -> {
                    println("Client ${client.socket.inetAddress.hostAddress} said: ${client.msg}")
                    for ((addr,user) in clients) {
                        if (addr != client.socket.inetAddress.hostAddress) {
                            withContext(Dispatchers.IO) {
                                val output = user.getOutputStream().bufferedWriter()
                                output.write("${client.msg}\n")
                                output.flush()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleClient(clientSocket: Socket, channel: Channel<Message>) {
        withContext(Dispatchers.IO) {
            val input = clientSocket.getInputStream().bufferedReader()
            val output = clientSocket.getOutputStream().bufferedWriter()
            output.write("type ':quit' to exit from the server\n")
            output.flush()
            while (true) {
                val data = input.readLine()
                if (data == ":quit" || data == null) {
                    break
                }
                channel.send(Message.NewMessage(clientSocket,data))
            }
            channel.send(Message.ClientDisconnected(clientSocket))
            input.close()
            output.close()
            clientSocket.close()
        }
    }

    sealed class Message {
        data class ClientConnected(val socket: Socket): Message()
        data class ClientDisconnected(val socket: Socket): Message()
        data class NewMessage(val socket: Socket, val msg: String): Message()
    }
}

fun main() {
    val application = App()
    application.serverStart()
}