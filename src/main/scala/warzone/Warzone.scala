package warzone

import cats.effect._
import cats.implicits._
import discord.model.AccountName
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.Credentials
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import warzone.Warzone._

class Warzone(client: Client[IO], token: String) {

  def checkMatchStatsForUser(accountName: AccountName): IO[Response] =
    client.expect[Response](GET(endpoint.addPath(accountName.value).withQueryParam("type", "wz"), headers(token)))

  def getLatestWinForUser(accountName: AccountName): IO[Win] = {
    // checkMatchStatsForUser(accountName).map { response =>
    //   val stats = response.game.flatMap { a =>
    //     val id = a.id
    //     val stats = a.segments.headOption.map(_.stats)
    //     val kills = stats.flatMap(_.kills)
    //     val damageDone = stats.flatMap(_.damageDone)
    //     val score = stats.flatMap(_.score)
    //   }
      ???
    // }
  }
}

object Warzone {
  val token = "0ae6b064-5b74-4401-be8d-f42b00e9decb"

  case class Win(matchId: String, playerStats: List[PlayerStats])
  case class PlayerStats(playerId: String, damageDone: Option[String], kills: Option[String], score: Option[String])

  case class Response(game: Option[Game])
  case class Game(id: String, segments: List[Segment])
  case class Segment(stats: Stats)
  case class Stats(kills: Option[Stat], damageDone: Option[Stat], teamPlacement: Option[Stat])
  case class Stat(value: Double, displayValue: String)

  implicit val statDecoder: Decoder[Stat]         = deriveDecoder[Stat]
  implicit val statsDecoder: Decoder[Stats]       = deriveDecoder[Stats]
  implicit val segmentDecoder: Decoder[Segment]   = deriveDecoder[Segment]
  implicit val matchDecoder: Decoder[Game]       = deriveDecoder[Game]
  implicit val responseDecoder: Decoder[Response] = _.downField("data").get[Json]("matches").map { matches =>
    val game = matches.hcursor.downArray
    val segments = game.get[List[Segment]]("segments").toOption
    val id = game.downField("attributes").get[String]("id").toOption
    (id, segments).mapN(Game)
  }.map(Response)

  val endpoint               = uri"https://api.tracker.gg/api/v1/warzone/matches/battlenet"
  def headers(token: String) = Authorization(Credentials.Token("TRN-Api-Key".ci, token))
}
