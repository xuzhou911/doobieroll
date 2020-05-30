package oru
import java.io.{File, PrintWriter}

import orutest.TestModelHelpers._
import orutest.model.Wrapper
import io.circe.syntax._
import zio._

object GenerateTestData extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    genNonEmptyCompany
      .map { c =>
        companyToDbRows(c).map { case (c, d, e) => Wrapper(c, d, e) }
      }
      .runCollectN(35)
      .map(_.flatten)
      .provideLayer(zio.test.environment.testEnvironment)
      .map { c =>
        println(c.length)
        val pw = new PrintWriter(new File("testdata.json"))
        pw.write(c.asJson.spaces2)
        pw.close()
      }
      .map(_ => 0)

}