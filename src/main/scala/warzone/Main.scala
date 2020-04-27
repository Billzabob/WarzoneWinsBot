package warzone

import cats.effect._
import cats.effect._
import discord._
import java.net.http.HttpClient
import org.http4s.client.jdkhttpclient._
import warzone.DB._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    pool
      .use { p =>
        clients.flatMap {
          case (client, wsClient) =>
            val wz = new Warzone(client, args(1))
            val db  = new DB(p)
            val token = args(0)
            val discordClient = new DiscordClient(client, token)
            val app = new App(discordClient, db, wz)
            Discord(token, client, wsClient).start(app.handleEvent)
        }
      }
      .as(ExitCode.Success)

  val clients =
    IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))
}
