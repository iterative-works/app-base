package works.iterative.webapp.store

import zio.{Ref, UIO, ZIO}

class AppStore[State, Event](
    state: Ref[State],
    listeners: Ref[Set[State => Unit]],
    reducer: Event => State => State
) extends Store.Service[State]
    with Dispatch.Service[Event] {
  override def getState: UIO[State] = state.get

  override def subscribe(listener: State => Unit): UIO[UIO[Unit]] =
    for {
      _ <- listeners.update(_ + listener)
      state <- state.get
      _ <- UIO.effectTotal(listener(state))
    } yield unsubscribe(listener)

  def unsubscribe(listener: State => Unit): UIO[Unit] =
    listeners.update(_ - listener)

  override def dispatch(event: Event): UIO[Unit] =
    for {
      newState <- state.updateAndGet(reducer(event))
      l <- listeners.get
      _ <- ZIO.collectAll(
        l.map(listener => UIO.effectTotal(listener(newState)))
      )
    } yield ()
}
