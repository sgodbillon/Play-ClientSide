package controllers.play.modules.clientside;

import play.Play;
import play.cache.Cache;
import play.modules.clientside.Routes;
import play.mvc.Controller;

import java.util.Date;

public class ClientSideController extends Controller {
    public static void exportAllRoutes() {
		response.contentType = "application/json";
		renderText(Routes.getJSONForAllRoutes());
	}

    public static void allJsRoutes() {
        String routesJson = null;

        if (Play.mode.isProd()) {
            // If the client have set a If-Modified-Since, try to render a 304
            if (request.headers.containsKey("if-modified-since")) {
                final Long lastModified = Cache.get(ROUTES_LAST_MODIFIED_KEY, Long.class);
                if (lastModified != null) {
                    final Long date = Long.parseLong(request.headers.get("if-modified-since").value());
                    if (lastModified <= date) {
                        notModified();
                    }
                }
            }
            routesJson = Cache.get(ROUTES_JSON_KEY, String.class); // Otherwise try to fetch routes from the cache
        }

        if (routesJson == null) {
            routesJson = Routes.getJSONForAllRoutes();
            Cache.set(ROUTES_JSON_KEY, routesJson);
        }

        final Date lastModified = new Date();
        Cache.set(ROUTES_LAST_MODIFIED_KEY, lastModified.getTime());
        response.setHeader("Last-Modified", "" + lastModified.getTime());

        request.format = "js";
        render(routesJson);
    }

    private final static String ROUTES_LAST_MODIFIED_KEY = "play-clientside.routes.lastModified";
    private static final String ROUTES_JSON_KEY = "play-clientside.routes";
}
