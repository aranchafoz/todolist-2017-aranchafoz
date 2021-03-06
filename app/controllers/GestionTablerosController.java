package controllers;

import play.mvc.*;

import views.html.*;
import javax.inject.*;
import play.data.Form;
import play.data.FormFactory;
import play.data.DynamicForm;
import play.Logger;

import java.util.List;
import java.util.ArrayList;

import services.UsuarioService;
import services.TableroService;
import models.Usuario;
import models.Tablero;
import security.ActionAuthenticator;

public class GestionTablerosController extends Controller {

   @Inject FormFactory formFactory;
   @Inject UsuarioService usuarioService;
   @Inject TableroService tableroService;


  // Comprobamos si hay alguien logeado con @Security.Authenticated(ActionAuthenticator.class)
  // https://alexgaribay.com/2014/06/15/authentication-in-play-framework-using-java/
  @Security.Authenticated(ActionAuthenticator.class)
  public Result formularioNuevoTablero(Long idUsuario) {
     String connectedUserStr = session("connected");
     Long connectedUser =  Long.valueOf(connectedUserStr);
     if (connectedUser != idUsuario) {
        return unauthorized("Lo siento, no estás autorizado");
     } else {
        Usuario usuario = usuarioService.findUsuarioPorId(idUsuario);
        return ok(formNuevoTablero.render(usuario, formFactory.form(Tablero.class),""));
     }
  }

  @Security.Authenticated(ActionAuthenticator.class)
  public Result creaNuevoTablero(Long idUsuario) {
     String connectedUserStr = session("connected");
     Long connectedUser =  Long.valueOf(connectedUserStr);
     if (connectedUser != idUsuario) {
        return unauthorized("Lo siento, no estás autorizado");
     } else {
        Form<Tablero> tableroForm = formFactory.form(Tablero.class).bindFromRequest();
        if (tableroForm.hasErrors()) {
           Usuario usuario = usuarioService.findUsuarioPorId(idUsuario);
           return badRequest(formNuevoTablero.render(usuario, formFactory.form(Tablero.class), "Hay errores en el formulario"));
        }
        Tablero tablero = tableroForm.get();
        tableroService.nuevoTablero(idUsuario, tablero.getNombre());
        flash("aviso", "El tablero se ha grabado correctamente");
        return redirect(controllers.routes.GestionTablerosController.listaTableros(idUsuario));
     }
  }

  @Security.Authenticated(ActionAuthenticator.class)
  public Result listaTableros(Long idUsuario) {
    String connectedUserStr = session("connected");
    Long connectedUser =  Long.valueOf(connectedUserStr);
    if (connectedUser != idUsuario) {
      return unauthorized("Lo siento, no estás autorizado");
    } else {
      String aviso = flash("aviso");
      Usuario usuario = usuarioService.findUsuarioPorId(idUsuario);
      List<Tablero> tablerosAdministrados = tableroService.allTablerosAdministradosUsuario(idUsuario);
      List<Tablero> tablerosParticipados = tableroService.allTablerosParticipadosUsuario(idUsuario);
      List<Tablero> restoTableros = tableroService.obtenerRestoTableros(idUsuario);
      return ok(listaTableros.render(tablerosAdministrados, tablerosParticipados, restoTableros, usuario, aviso));
    }
  }

  @Security.Authenticated(ActionAuthenticator.class)
  public Result apuntarse(Long idUsuario, Long idTablero) {
    String connectedUserStr = session("connected");
    Long connectedUser =  Long.valueOf(connectedUserStr);
    if (connectedUser != idUsuario) {
      return unauthorized("Lo siento, no estás autorizado");
    } else {
      tableroService.apuntaParticipante(idUsuario, idTablero);

      flash("aviso", "El usuario se ha apuntado correctamente");
      return redirect(controllers.routes.GestionTablerosController.listaTableros(idUsuario));
    }
  }

  @Security.Authenticated(ActionAuthenticator.class)
  public Result detalleTablero(Long idUsuario, Long idTablero) {
    String connectedUserStr = session("connected");
    Long connectedUser =  Long.valueOf(connectedUserStr);
    if (connectedUser != idUsuario) {
      return unauthorized("Lo siento, no estás autorizado");
    } else {
      Tablero tablero = tableroService.obtenerTablero(idTablero);
      if (tablero == null) {
         return notFound("Tablero no encontrado");
      } else {
        List<Usuario> participantes = new ArrayList<Usuario>();
        participantes.addAll(tablero.getParticipantes());
        return ok(detalleTablero.render(tablero, participantes));
      }
    }
  }
 }
