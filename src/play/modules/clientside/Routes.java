package play.modules.clientside;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import play.cache.Cache;
import play.mvc.Router;
import play.mvc.Router.Route;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Routes {
	public static class ExtendedRoute {
        public String action;
        public String pattern;
        public List<List<String>> params;
        public String method;
    }

    public static final String CACHE_KEY = "play-clientside.json.routes.all";
    
	public static String getJSONForAllRoutes() {
		String json = Cache.get(CACHE_KEY, String.class);
		if(json == null) {
			System.out.println("cache miss");
			json = makeJSONFor(Router.routes);
			Cache.add(CACHE_KEY, json);
		}else System.out.println("was in cache");
		return json;
	}
	
    public static String makeJSONFor(List<Route> routes) {
        ArrayList<ExtendedRoute> extendedRoutes = new ArrayList<ExtendedRoute>();
        for(Route route : routes) {
            ExtendedRoute extendedRoute = new ExtendedRoute();
            extendedRoute.action = route.action;
            extendedRoute.method = route.method;
            extendedRoute.pattern = route.path;
            extendedRoute.params = ClientSidePlugin.paramsNamesByAction.get(extendedRoute.action);
            extendedRoutes.add(extendedRoute);
        }
        Type listType = new TypeToken<List<ExtendedRoute>>() { }.getType();
        String result = new Gson().toJson(extendedRoutes, listType);
        System.out.println("result is \n\t" + result);
        return result;
    }
}
