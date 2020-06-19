package works.iterative.webapp

import izumi.reflect.Tag
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import works.iterative.webapp.store.{Dispatch, Store}
import zio.ZLayer

abstract class ConnectedWebApp[State: Tag, Event: Tag] extends WebApp {
  type BaseEnv = zio.ZEnv with store.Store[State] with store.Dispatch[Event]
  type AppEnv <: BaseEnv

  def reducer: Event => State => State

  def initialState: State

  def completeLayer: ZLayer[Any, Nothing, AppEnv]

  def baseLayer
      : ZLayer[Any, Nothing, Dispatch[Event] with Store[State] with zio.ZEnv] =
    store.Store.live(initialState, reducer) ++ zio.ZEnv.live

  override lazy val runtime: zio.Runtime[AppEnv] =
    zio.Runtime.unsafeFromLayer(completeLayer)

  def subscribe(listener: State => Unit): () => Unit = {
    val sub = for {
      unsub <- store.subscribe[State](listener)
    } yield () => runtime.unsafeRun(unsub)
    runtime.unsafeRun(sub)
  }

  @react object Connect {
    case class Props(children: State => ReactElement)

    val component: FunctionalComponent[Props] = FunctionalComponent[Props] {
      p =>
        val (state, updateState) = useState(initialState)
        useEffect(() => {
          subscribe(st => updateState(st))
        })
        p.children(state)
    }
  }
}
