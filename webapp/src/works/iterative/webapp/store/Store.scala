package works.iterative.webapp.store

import izumi.reflect.Tag
import zio.{Has, Layer, Ref, UIO, ZLayer}

object Store {
  trait Service[State] {
    def getState: UIO[State]
    def subscribe(listener: State => Unit): UIO[UIO[Unit]]
  }

  def live[State: Tag, Event: Tag](
      initialState: State,
      reducer: Event => State => State
  ): Layer[Nothing, Dispatch[Event] with Store[State]] =
    ZLayer.fromEffectMany(for {
      state <- Ref.make[State](initialState)
      listeners <- Ref.make[Set[State => Unit]](Set.empty)
    } yield {
      val appStore = new AppStore[State, Event](state, listeners, reducer)
      Has[Dispatch.Service[Event]](appStore) ++ Has[Store.Service[State]](
        appStore
      )
    })
}
