package works.iterative.webapp

import zio.ZEnv
import zio.ZIO
import zio.ExitCode
import zio.UIO
import org.scalajs.dom
import org.scalajs.dom.raw.Element
import zio.Task
import zio.IO
import scala.annotation.nowarn
import slinky.core.facade.ReactElement
import slinky.hot
import scala.scalajs.LinkingInfo
import slinky.web.ReactDOM

trait WebApp {
  type AppEnv <: ZEnv
  type AppError <: Throwable
  type Handler = ZIO[AppEnv, AppError, Unit]

  protected def runtime: zio.Runtime[AppEnv]

  def bindElementId: String = "root"

  val getOrCreateContainer: UIO[Element] =
    Task
      .effectTotal(
        Option(dom.document.getElementById(bindElementId)).getOrElse {
          val elem = dom.document.createElement("div")
          elem.id = bindElementId
          dom.document.body.appendChild(elem)
          elem
        }
      )

  def app: ZIO[AppEnv, AppError, ReactElement]
  def displayError(ex: Throwable): ZIO[ZEnv, Throwable, ReactElement]

  def run(container: Element): ZIO[AppEnv, Throwable, Unit] = {
    val runReact = for {
      _ <- Task.when(LinkingInfo.developmentMode)(Task.effect(hot.initialize()))
      content <- app
    } yield {
      ReactDOM.render(content, container)
    }

    runReact.unit.catchAll(ex =>
      // TODO: logging
      displayError(ex).map(ReactDOM.render(_, container)).unit
    )
  }

  def dispatch(action: Handler): Unit =
    runtime.unsafeRunAsync_(action.catchAll(handleDispatchError))

  def handleDispatchError(err: AppError): ZIO[AppEnv, Nothing, Unit] =
    // TODO: log instead
    IO.effectTotal(dom.console.error("Dispatch error: %o", err))

  @nowarn("msg=never used")
  final def main(args0: Array[String]): Unit = {
    val application = for {
      container <- getOrCreateContainer
      result <- run(container)
    } yield result

    runtime.unsafeRunAsync(application.exitCode)(_ => ())
  }
}
