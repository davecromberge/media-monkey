package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json._
import play.api.Play.current
import play.modules.reactivemongo._
import scala.concurrent.{Future}

import app._
import core._
import views._
import models._


object Customers extends 
  Controller with 
  MongoController with 
  ComponentRegistry {
 
  def index = Action { implicit request =>
    Async {
      customerRepo.all.map { customers => 
        Logger.debug("Index page shown for " + customers.size + " customers.")
        Ok(html.customers.index(customers))
      }
    }
  }

  def edit(id: String) = Action {
    Async {
      customerRepo.get(id).map { 
        case Some(customer) => 
          Logger.debug("Edit page shown for customer with " + customer.attributes.size + " attributes.")
          Ok(html.customers.edit(id, form.fill(customer)))
        case None => 
          NotFound
      }
    }
  }

  def update(id: String) = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => 
        BadRequest(html.customers.edit(id, formWithErrors)),
      customer => 
        AsyncResult {
          customerRepo.update(id, customer).map ( _ =>
            Home.flashing("success" -> "Customer %s has been updated".format(customer.name))
          )
        }
    )
  }

  def delete(id: String) = Action { implicit request =>
    Async {
      customerRepo.delete(id) 
        .map(_ => Home.flashing("success" -> "Customer has been deleted"))
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
      "pack" -> optional(text),
      "attributes" -> optional(
        seq(
          mapping(
            "code" -> nonEmptyText,
            "name" -> nonEmptyText,
            "value" -> optional(text)
          )(Attribute.apply)(Attribute.unapply)
        )
      )
    )(Customer.apply)(Customer.unapply)
  )

  val Home = Redirect(routes.Customers.index)
  
  implicit lazy val attributes: Future[Iterable[Attribute]] = attributeRepo.all
}

