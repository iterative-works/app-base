package works.iterative.webapp

import izumi.reflect.Tag
import zio.{Has, UIO, URIO, ZIO}

package object store {
  type Store[S] = Has[Store.Service[S]]
  type Dispatch[E] = Has[Dispatch.Service[E]]

  def getState[S: Tag]: ZIO[Store[S], Nothing, S] =
    ZIO.accessM[Store[S]](_.get.getState)

  def subscribe[S: Tag](listener: S => Unit): URIO[Store[S], UIO[Unit]] =
    ZIO.accessM(_.get.subscribe(listener))

  def dispatch[E: Tag](event: E): URIO[Dispatch[E], Unit] =
    ZIO.accessM[Dispatch[E]](_.get.dispatch(event))
}
