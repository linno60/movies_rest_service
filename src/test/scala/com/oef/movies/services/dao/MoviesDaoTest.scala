package com.oef.movies.services.dao

import com.oef.movies.IntegrationSpec
import org.postgresql.util.PSQLException
import org.scalatest.BeforeAndAfterEach
import scala.concurrent.ExecutionContext.Implicits.global

class MoviesDaoTest extends IntegrationSpec with BeforeAndAfterEach {
  private val dao = MoviesDao()

  "create" should {

    "insert a new entry" in {
      val info = randomMovieInfo()
      dao.read(info.movieIdentification).futureValue shouldBe None
      dao.create(info).futureValue shouldBe ((): Unit)
      dao.read(info.movieIdentification).futureValue.value shouldBe info
    }

    "throw an exception on existing entry insertion" in {
      val info = movieInfo()
      dao.create(info).futureValue
      whenReady(dao.create(info).failed) { e =>
        e shouldBe a[PSQLException]
        println(e.printStackTrace())
        e should have message
          """ERROR: duplicate key value violates unique constraint "movies_pkey"
            |  Detail: Key (imdb_id, screen_id)=(someImdbId, someScreenId) already exists.""".stripMargin
      }
    }

  }

  "read" should {

    "return an existing entry" in {
      val info = randomMovieInfo()
      dao.create(info).futureValue
      dao.read(info.movieIdentification).futureValue.value shouldBe info
    }

    "return none for non existing entry" in {
      val info = randomMovieInfo()
      dao.read(info.movieIdentification).futureValue shouldBe None
    }

  }

  "update" should {

    "update an existing entry reserving a seat" in {
      val info = randomMovieInfo()
      dao.create(info).futureValue
      def dbInfo = dao.read(info.movieIdentification).futureValue.value
      dbInfo.reservedSeats shouldBe 0
      dao.update(info.reserveOneSeat()).futureValue
      dbInfo.reservedSeats shouldBe 1
    }

    "do nothing for non existing entry" in {
      dao.update(randomMovieInfo()).futureValue shouldBe ((): Unit)
    }

  }

}
