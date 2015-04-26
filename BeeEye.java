/*

Uses java types

	String
	ArrayList
	HashMap

*/

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BeeEye {

	public static Map<String, Object> GLOBAL_SCOPE = new HashMap<String, Object>();

	public static Object eval(Object d, Map<String, Object> scope) {
		if (d instanceof String)
			return lookup(scope, (String) d);
		if (d instanceof List) {
			Macro m = (Macro) ((List) d).get(0);
			List args = new ArrayList(((List) d).subList(1, ((List) d).size()));
			return m.call(args, scope);
		}
		throw new Error(d.toString());
	}

	public static Object lookup(Map scope, String key) {
		if (scope.containsKey(key))
			return scope.get(key);
		if (scope.containsKey("__parent__"))
			return lookup((Map<String, Object>)scope.get("__parent__"), key);
		throw new Error(key);
	}

}
