package site.util

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.Executors

object SimpleHttpServer {
    private var server: HttpServer? = null

    fun start(port: Int, rootDirectory: File, pidFile: File? = null) {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory) {
            throw IllegalArgumentException("Root directory must exist and be a directory: ${rootDirectory.absolutePath}")
        }

        server = HttpServer.create(InetSocketAddress(port), 0).apply {
            createContext("/") { exchange ->
                val requestPath = exchange.requestURI.path
                val filePath = if (requestPath == "/") "index.html" else requestPath.substring(1)
                val file = File(rootDirectory, filePath)

                if (file.exists() && file.isFile) {
                    exchange.sendResponseHeaders(200, file.length())
                    file.inputStream().copyTo(exchange.responseBody)
                } else {
                    exchange.sendResponseHeaders(404, 0)
                }
                exchange.close()
            }
            executor = Executors.newSingleThreadExecutor()
            start()
        }
        println("Simple HTTP Server started on port $port, serving files from ${rootDirectory.absolutePath}")

        pidFile?.let { file ->
            file.parentFile.mkdirs()
            file.writeText(ProcessHandle.current().pid().toString())
            println("Server PID written to ${file.absolutePath}")
        }
    }

    fun stop() {
        server?.stop(0)
        println("Simple HTTP Server stopped.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2 || args.size > 3) {
            System.err.println("Usage: SimpleHttpServer <port> <rootDirectory> [pidFile]")
            return
        }
        val port = args[0].toInt()
        val rootDirectory = File(args[1])
        val pidFile = if (args.size == 3) File(args[2]) else null
        SimpleHttpServer.start(port, rootDirectory, pidFile)
        // Keep the main thread alive so the server continues to run
        // Keep the main thread alive so the server continues to run
        while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        stop()
    }
}
