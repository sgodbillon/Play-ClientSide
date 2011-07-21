package controllers.play.modules.clientside;

import play.modules.clientside.Routes;
import play.mvc.Controller;

public class ClientSideController extends Controller {
	public static void exportAllRoutes() {
		response.contentType = "application/json";
		renderText(Routes.getJSONForAllRoutes());
	}
}
