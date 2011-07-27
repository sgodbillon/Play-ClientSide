package play.modules.clientside;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import play.cache.Cache;
import play.modules.clientside.ClientSidePlugin.Action;
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
			json = makeJSONFor();
			Cache.add(CACHE_KEY, json);
		}else System.out.println("was in cache");
		return json;
	}
	
    public static String makeJSONFor(String... categories) {
    	List<Route> routes = Router.routes;
        ArrayList<ExtendedRoute> extendedRoutes = new ArrayList<ExtendedRoute>();
        for(Route route : routes) {
        	List<Action> matchingActions = matchingActionsFor(route.action, categories);
            ExtendedRoute extendedRoute = new ExtendedRoute();
            extendedRoute.action = route.action;
            extendedRoute.method = route.method;
            extendedRoute.pattern = route.path;
            extendedRoute.params = new ArrayList<List<String>>();
            for(Action action: matchingActions)
        		extendedRoute.params.add(action.params);
            extendedRoutes.add(extendedRoute);
        }
        Type listType = new TypeToken<List<ExtendedRoute>>() { }.getType();
        String result = new Gson().toJson(extendedRoutes, listType);
        System.out.println("result is \n\t" + result);
        return result;
    }
    
    private static List<Action> matchingActionsFor(String actionName, String... categories) {
    	ArrayList<Action> result = new ArrayList<ClientSidePlugin.Action>();
    	for(Action action: ClientSidePlugin.actionsByName.get(actionName)) {
    		if(categories == null || categories.length == 0 || intersect(action.categories, categories))
    			result.add(action);
    	}
    	return result;
    }
    
    private static boolean intersect(String[] a1, String[] a2) {
    	if(a1.length > 0 && a2.length > 0)
    		for(int i = 0; i < a1.length; i++)
    			for(int j = 0; j < a2.length; j++)
    				if(a1[i].equals(a2[j]))
    					return true;
    	return false;
    }
}
