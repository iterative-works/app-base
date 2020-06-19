package works.iterative.webapp.store

import zio.UIO

object Dispatch {
  trait Service[-E] {
    def dispatch(event: E): UIO[Unit]
  }

}
