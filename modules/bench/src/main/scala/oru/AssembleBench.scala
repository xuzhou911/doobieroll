package oru

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

import example.Awesome
import example.TestModelHelpers._
import example.model.{DbCompany, DbDepartment, DbEmployee, Wrapper}
import io.circe.parser._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import shapeless._

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class AssembleBench {

  val wrappers10K: Vector[Wrapper] = {
    val r = new String(
      this.getClass.getClassLoader.getResourceAsStream("testdata.json").readAllBytes(),
      StandardCharsets.UTF_8
    )
    decode[Vector[Wrapper]](r)
      .getOrElse(throw new Exception("decode failed"))
  }

  val hlist10K: Vector[DbCompany :: DbDepartment :: DbEmployee :: HNil] = wrappers10K.map(_.asHList)
  val tuple10K: Vector[(DbCompany, DbDepartment, DbEmployee)] = wrappers10K.map(a => (a.c, a.d, a.e))

  val optHList10k: Vector[DbCompany :: Option[DbDepartment] :: Option[DbEmployee] :: HNil] = wrappers10K.map(wrapperToOptHList)

  val hlist1K: Vector[DbCompany :: DbDepartment :: DbEmployee :: HNil] = wrappers10K.map(_.asHList).take(1000)
  val optHList1k: Vector[DbCompany :: Option[DbDepartment] :: Option[DbEmployee] :: HNil] = optHList10k.take(1000)

  import example.AwesomeSpec.ExampleModelInstances._

  @Benchmark
  def awesome10k(blackhole: Blackhole): Unit =
    blackhole.consume(Awesome.assembleUnordered(hlist10K))

  @Benchmark
  def awesome1k(blackhole: Blackhole): Unit =
    blackhole.consume(Awesome.assembleUnordered(hlist1K))

  @Benchmark
  def awesomeOpt1k(blackhole: Blackhole): Unit =
    blackhole.consume(Awesome.assembleUnordered(optHList1k))

  @Benchmark
  def naive10k(blackhole: Blackhole): Unit =
    blackhole.consume(Naive.assembleUnordered(hlist10K))

}
