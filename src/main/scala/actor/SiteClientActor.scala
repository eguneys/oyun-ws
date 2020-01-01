package oyun.ws

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ Behavior }

import ipc._

object SiteClientActor {

  import ClientActor._

  def start(deps: Deps): Behavior[ClientMsg] = Behaviors.setup { ctx =>
    // import deps._
    onStart(deps, ctx)
    //req.user foreach { users.connect(_, ctx.self) }
    apply(State(), deps)
  }

  private def apply(state: State, deps: Deps): Behavior[ClientMsg] =
    ???
  
}
