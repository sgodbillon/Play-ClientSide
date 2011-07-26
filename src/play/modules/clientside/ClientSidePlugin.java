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
    	ClientSidePlugin.paramsNamesByAction = makeParamsNamesByAction();
    	clearCache();
    }
    
    private static Map<String, List<List<String>>> makeParamsNamesByAction() {
    	HashMap<String, List<List<String>>> paramsNamesByAction = new HashMap<String, List<List<String>>>();
        for(ApplicationClass applicationClass: Play.classes.getAssignableClasses(Controller.class)) {
            try {
                for (Method action: applicationClass.javaClass.getDeclaredMethods()) {
                    if (Modifier.isPublic(action.getModifiers()) && Modifier.isStatic(action.getModifiers()) && action.getReturnType().equals(Void.class)) {
                        String actionName = action.getDeclaringClass().getName().substring("controllers.".length()) + "." + action.getName();
                        ArrayList<List<String>> paramsNames = new ArrayList<List<String>>();
                        ArrayList<String> params = new ArrayList<String>();
                        for (String param : Java.parameterNames(action)) {
                            // TODO Handle skipped parameters
                            params.add(param);
                        }
                        paramsNames.add(params);
                        paramsNamesByAction.put(actionName, paramsNames);
                    }
                }
            } catch (Exception e) {
                throw new UnexpectedException(e);
            }
        }
        return paramsNamesByAction;
    }
    
    private static void clearCache() {
    	for(String key: CACHE_KEYS) {
    		Cache.delete(key);
    	}
    }
    
    protected static Map<String, List<List<String>>> paramsNamesByAction = new HashMap<String, List<List<String>>>();
}
