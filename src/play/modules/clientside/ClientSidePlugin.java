package play.modules.clientside;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import play.Play;
import play.PlayPlugin;
import play.cache.Cache;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.exceptions.UnexpectedException;
import play.mvc.Controller;
import bytecodeparser.analysis.LocalVariable;

public class ClientSidePlugin extends PlayPlugin {
	private static final String[] CACHE_KEYS = new String[] {
		"play-clientside.json.routes.all"
	};
	
    @Override
    public void onApplicationStart() {
    	System.out.println("reloading clientsideplugin");
    	ClientSidePlugin.paramsNamesByAction = makeParamsNamesByAction();
    	clearCache();
    }
    
    private static HashMap<String, List<List<String>>> makeParamsNamesByAction() {
    	HashMap<String, List<List<String>>> paramsNamesByAction = new HashMap<String, List<List<String>>>();
        ClassPool classpool = Enhancer.newClassPool();
        for(ApplicationClass applicationClass: Play.classes.getAssignableClasses(Controller.class)) {
            try {
                CtClass ctClass = classpool.makeClass(new ByteArrayInputStream(applicationClass.enhancedByteCode));
                for(CtMethod action: ctClass.getDeclaredMethods()) {
                    System.out.println("method " + action.getLongName());
                    
                    System.out.println("action has local variable attr? " + bytecodeparser.utils.Utils.getLocalVariableAttribute(action) != null);
                    if(Modifier.isPublic(action.getModifiers()) && Modifier.isStatic(action.getModifiers()) && action.getReturnType().equals(CtClass.voidType)) {
                        String actionName = action.getDeclaringClass().getName().substring("controllers.".length()) + "." + action.getName();
                        System.out.println("->actionName="+actionName+": "+paramsNamesByAction.get(actionName));;
                        Map<Integer, LocalVariable> vars = new bytecodeparser.CodeParser(action).context.localVariables;
                        ArrayList<String> params = new ArrayList<String>();
                        for(int i = 0; i < vars.size(); i++) {
                            LocalVariable lv = vars.get(i);
                            if(!lv.isParameter)
                                break;
                            params.add(lv.name);
                        }
                        List<List<String>> paramsNames = paramsNamesByAction.get(actionName);
                        if(paramsNames == null) {
                            paramsNames = new ArrayList<List<String>>();
                            paramsNamesByAction.put(actionName, paramsNames);
                        } else {
                            System.out.println("add >1 params for action="+actionName);
                        }
                        paramsNames.add(params);
                        if(paramsNames.size() > 1)
                            System.out.println("-> size=" + paramsNames.size());
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
    
    protected static HashMap<String, List<List<String>>> paramsNamesByAction = new HashMap<String, List<List<String>>>();

}
