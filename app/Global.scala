import com.google.inject.{AbstractModule, Guice}
import net.codingwell.scalaguice.ScalaModule
import play.api.GlobalSettings
import services._

/**
 * Set up dependency injection.
 */
object Global extends GlobalSettings {

  val injector = Guice.createInjector(new AbstractModule with ScalaModule {
    def configure() {
      bind[CounterService].to[CounterServiceImpl]
      bind[UrlShorteningService].to[UrlShorteningServiceImpl]
    }
  })

  /**
   * Get controller instances from the Guice application context.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)
}
