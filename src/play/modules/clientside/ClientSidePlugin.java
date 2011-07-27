package play.modules.clientside;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.Modifier;
import play.Play;
import play.PlayPlugin;
import play.cache.Cache;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.UnexpectedException;
import play.mvc.Controller;
import play.utils.Java;

public class ClientSidePlugin extends PlayPlugin {
	private static final String[] CACHE_KEYS = new String[] {
        Routes.CACHE_KEY
	};
	
    @Override
    public void onApplicationStart() {
    	System.out.println("reloading clientsideplugin");
    	ClientSidePlugin.actionsByName = makeActionsByName();
    	clearCache();
    }
    
    public static class Action {
        public String name;
        public List<String> params;
        public String[] categories;
    }
    
    public static Map<String, List<Action>> actionsByName = new HashMap<String, List<Action>>();
    
    private static Map<String, List<Action>> makeActionsByName() {
    	Map<String, List<Action>> actionsByName = new HashMap<String, List<Action>>();
        for(ApplicationClass applicationClass: Play.classes.getAssignableClasses(Controller.class)) {
            try {
                for (Method actionMethod: applicationClass.javaClass.getDeclaredMethods()) {
                    if (Modifier.isPublic(actionMethod.getModifiers()) && Modifier.isStatic(actionMethod.getModifiers()) && actionMethod.getReturnType().equals(Void.class)) {
                        String actionName = actionMethod.getDeclaringClass().getName().substring("controllers.".length()) + "." + actionMethod.getName();
                        List<Action> actions = actionsByName.get(actionName);
                        if(actions == null) {
                        	actions = new ArrayList<Action>();
                        	actionsByName.put(actionName, actions);
                        }
                        Action action = new Action();
                        if(actionMethod.isAnnotationPresent(ClientRoute.class)) {
                        	action.categories = actionMethod.getAnnotation(ClientRoute.class).value();
                        } else {
                        	action.categories = new String[0];
                        }
                        action.params = new ArrayList<String>();
                        for (String param : Java.parameterNames(actionMethod)) {
                            // TODO Handle skipped parameters
                        	action.params.add(param);
                        }
                        actions.add(action);
                    }
                }
            } catch (Exception e) {
                throw new UnexpectedException(e);
            }
        }
        return actionsByName;
    }
    
    private static void clearCache() {
    	for(String key: CACHE_KEYS) {
    		Cache.delete(key);
    	}
    }
}
