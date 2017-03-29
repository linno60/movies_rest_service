package com.oef.movies.http.routes

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.oef.movies.models.{MovieIdentification, MovieRegistration}
import com.oef.movies.services.MovieService
import spray.json._

trait MovieServiceRoute extends MovieService with BaseServiceRoute {

  val movieRoutes = pathPrefix("movies" / "imdbId" / Segment / "screenId" / Segment) { (imdbId, screentId) =>
    val urlIdentifiers = MovieIdentification(imdbId, screentId)
    pathEndOrSingleSlash {
      put { //Register a movie
        entity(as[MovieRegistration]) { movieRegistration =>
          validateEquals(urlIdentifiers, MovieIdentification(movieRegistration.imdbId, movieRegistration.screenId)) {
            val saved = save(movieRegistration)
            onComplete(saved) { unit =>
              complete(s"movie registered")
            }
          }
        }
      } ~
      patch { //Reserve a seat at the movie
        entity(as[MovieIdentification]) { movieIdentification =>
          validateEquals(urlIdentifiers, movieIdentification) {
            complete(
              reserve(movieIdentification)
                .map(res => JsObject("reservationResult" -> JsString(res.toString)))
            )
          }
        }
      } ~
      get { //Retrieve information about the movie
        val movieInfo = read(urlIdentifiers)
        complete(movieInfo.map(_.toJson))
      }
    }
  }

  private def validateEquals(urlIdentifiers: MovieIdentification, bodyIdentifiers: MovieIdentification): Directive0 = {
    validate(urlIdentifiers == bodyIdentifiers, s"uri: $urlIdentifiers and body: $bodyIdentifiers resource identifiers not matching")
  }

}
