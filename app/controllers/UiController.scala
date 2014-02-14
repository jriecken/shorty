package controllers

import javax.inject.Singleton
import play.api.mvc._

@Singleton
class UiController extends Controller {

  def index = Action {
    // TODO: Implement
    Ok(views.html.index())
  }

}
