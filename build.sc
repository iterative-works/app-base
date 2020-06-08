import mill._, scalalib._

import $file.base.build
import $file.base.versions, base.versions.V

trait DataModule extends base.build.BaseModule {
  def millSourcePath = build.millSourcePath / "data"

  def ivyDeps =
    super.ivyDeps() ++ Agg(
      ivy"org.typelevel::cats-core::${V.cats}",
      ivy"dev.zio::zio::${V.zio}"
    )
}

object dataJvm extends DataModule {
  object test extends Tests {
    def testFrameworks = T(Seq("org.scalatest.tools.Framework"))
    def ivyDeps =
      T(
        Agg(
          ivy"org.scalatest::scalatest:${V.scalatest}",
          ivy"org.typelevel::cats-laws:${V.cats}",
          ivy"org.typelevel::cats-testkit-scalatest:1.0.1",
          ivy"com.github.alexarchambault::scalacheck-shapeless_1.14:1.2.3"
        )
      )
  }
}

object dataJs extends DataModule with base.build.BaseJSModule
