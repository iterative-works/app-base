package works.iterative.data

import cats.tests.CatsSuite
import cats.laws.discipline.MonadTests
import org.scalacheck.ScalacheckShapeless._

class PotMonadTests extends CatsSuite {
  checkAll("Pot.MonadLaws", MonadTests[Pot].monad[Int, Int, String])
}
