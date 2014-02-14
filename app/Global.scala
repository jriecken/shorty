import com.google.inject.{Guice, AbstractModule}
import net.codingwell.scalaguice.ScalaModule
import play.api.GlobalSettings

/**
 * Set up dependency injection for the controllers.
 */
object Global extends GlobalSettings {

  val injector = Guice.createInjector(new AbstractModule with ScalaModule {
    def configure {
      // TODO: bind interfaces to impls
    }
  })

  /**
   * Get controller instances from the Guice application context.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)
}