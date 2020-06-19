import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import $file.versions, versions.V

import mill._, scalalib._, scalajslib._

trait BaseModule extends ScalaModule {
  def scalaVersion = T("2.13.2")
  def scalacOptions = T {
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
      "-Ybackend-parallelism",
      "8", // Enable paralellisation — change to desired number!
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified", // and macro definitions. This can lead to performance improvements.
      "-Ymacro-annotations",
      "-Yrangepos" // for semantic DB
    )
  }
}

trait BaseJSModule extends BaseModule with ScalaJSModule {
  def scalaJSVersion = T("1.1.0")
  // Workaround until Mill has 1.1 version working - https://github.com/lihaoyi/mill/issues/894
  def scalaJSWorkerVersion = "1.0"
  // def jsEnvConfig = T(scalajslib.api.JsEnvConfig.JsDom())
  def moduleKind = T(scalajslib.api.ModuleKind.CommonJSModule)
  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"io.github.cquiroz::scala-java-time::${V.scalaJavaTime}",
      // FIXME: replace with selective TZDB data, this is too big!
      ivy"io.github.cquiroz::scala-java-time-tzdb::${V.scalaJavaTime}"
    )
  }
}

trait ComponentsJSModule extends BaseJSModule { self =>
  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"me.shadaj::slinky-core::${V.slinky}",
      ivy"me.shadaj::slinky-web::${V.slinky}",
      ivy"me.shadaj::slinky-hot::${V.slinky}"
    ) ++ Agg( // TODO: add the typings dynamically
      ivy"org.scalablytyped::react::16.9.32-48b0db",
      ivy"org.scalablytyped::typescript::3.8.3-236de3"
    )
  }

  trait Storybook extends BaseJSModule {
    def ivyDeps = T {
      super.ivyDeps() ++
        Agg(
          ivy"org.scalablytyped::storybook__react::5.3.18-bca59c",
          ivy"org.scalablytyped::storybook__addon-actions::5.3.18-4cae6f"
        )
    }
    def moduleDeps = Seq(self)
  }

  object test extends Tests {
    def testFrameworks = T(Seq("org.scalatest.tools.Framework"))
    def ivyDeps = T {
      Agg(ivy"org.scalatest::scalatest::${V.scalatest}")
    }
  }
}

trait DataModule extends BaseModule {
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

object dataJs extends DataModule with BaseJSModule

object webapp extends ComponentsJSModule { 
  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"dev.zio::zio::${V.zio}",
      ivy"dev.zio::zio-streams::${V.zio}",
      // TODO: use jshttp for production
      ivy"dev.zio::zio-logging-jsconsole::0.2.9",
      ivy"com.softwaremill.sttp.client::core::${V.sttp}",
      ivy"com.softwaremill.sttp.client::circe::${V.sttp}"
    ) ++ Agg(
      Seq("circe-core", "circe-generic", "circe-parser", "circe-generic-extras")
        .map(name => ivy"io.circe::$name::${V.circe}"): _*
    )
  }
}

trait AppJSModule extends BaseJSModule {
  def appModuleDeps: Seq[JavaModule] = Seq.empty[JavaModule]
  def moduleDeps: Seq[JavaModule] = Seq(webapp) ++ appModuleDeps
}
