package oyun

import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }

package object ws {

  type Emit[A] = Function[A, Unit]

  type ClientSystem = ActorSystem[Clients.Control]
  type ClientBehavior = Behavior[ipc.ClientMsg]
  type Client = ActorRef[ipc.ClientMsg]
  type ClientEmit = Emit[ipc.ClientIn]
  
}
