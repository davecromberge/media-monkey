package controllers

import app.ComponentRegistry
import views._
import models._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Packages extends Controller with ComponentRegistry {
 
  def index = Action { implicit request =>
    Async {
      packageRepo.all.map { packages => 
        Logger.debug("Index page shown for " + packages.size + " packages.")
        Ok(html.packages.index(packages))
      }
    }
  }

  def edit(id: String) = Action {
    Async {
      packageRepo.get(id).map { 
        case Some(pack) =>
          Logger.debug("Edit page shown for package with " + pack.attributes.size + " attributes.")
          Ok(html.packages.edit(pack.id.get, form.fill(pack)))
        case None => 
          NotFound
      }
    }
  }

  def update(id: String) = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(html.packages.edit(id, formWithErrors)),
      pack => 
        AsyncResult {
          packageRepo.update(pack).map ( _ =>
            Home.flashing("success" -> "Package %s has been updated".format(pack.name))
          )
        }
    )
  }

  def delete(id: String) = Action { implicit request =>
    Async {
      packageRepo.delete(id) 
        .map(_ => Home.flashing("success" -> "Package has been deleted"))
        .recover { case _ => InternalServerError }
    }
  }
 
  val form = Form(
    mapping(
      "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
      "name" -> nonEmptyText,
      "attributes" -> optional(
        seq(
          mapping(
            "code" -> nonEmptyText,
            "name" -> nonEmptyText,
            "value" -> optional(text)
          )(Attribute.apply)(Attribute.unapply)
        )
      )
    )(Package.apply)(Package.unapply)
  )

  val Home = Redirect(routes.Packages.index)
  
  implicit lazy val attributes: Future[Iterable[Attribute]] = attributeRepo.all
}

