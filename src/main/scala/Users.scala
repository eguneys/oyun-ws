package oyun.ws

final class Users(oyun: Oyun) {

  // private val oyunIn = oyun.emit.site

  def connect(user: User, client: Client, silently: Boolean = false): Unit = ()
    // users.compute(user.id, {
    //   case (_, null) =>
    //     if (!disconnects.remove(user.id) && !silently) 
    //       oyunIn(OyunIn.ConnectUser(user))
    //     Set(client)
    //   case (_, clients) =>
    //     clients + client
    // })

}
