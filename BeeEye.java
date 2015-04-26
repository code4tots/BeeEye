/*

Uses java types

	String
	ArrayList
	HashMap

*/

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BeeEye {

	public static void main(String[] args) {
		BasicBuiltins.install();
		run("(print 'hi')");
	}

	public final static Map<String, Object> GLOBAL_SCOPE = new HashMap<String, Object>();

	public static Object eval(Object d, Map<String, Object> scope) {
		if (d instanceof String)
			return lookup(scope, (String) d);
		if (d instanceof List) {
			Macro m = (Macro) eval(((List) d).get(0), scope);
			List args = new ArrayList(((List) d).subList(1, ((List) d).size()));
			return m.call(args, scope);
		}
		if (d instanceof Integer || d instanceof Double)
			return d;
		throw new Error(d.getClass().toString());
	}

	public static Object evalAll(List dd, Map<String, Object> scope) {
		Object last = null;
		for (Object d : dd) {
			last = eval(d, scope);
		}
		return last;
	}

	public static Object lookup(Map scope, String key) {
		if (scope.containsKey(key))
			return scope.get(key);
		if (scope.containsKey("__parent__"))
			return lookup((Map<String, Object>)scope.get("__parent__"), key);
		throw new Error(key);
	}

	public static Map<String, Object> newScope(Map<String, Object> parent) {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("__parent__", parent);
		return scope;
	}

	public static Object run(String text) {
		return run(text, GLOBAL_SCOPE);
	}

	public static Object run(String text, Map<String, Object> scope) {
		return evalAll(Parser.parse(text), scope);
	}

	public static boolean truthy(Object x) {
		if (x instanceof Boolean) {
			return (Boolean) x;
		}
		if (x instanceof Number) {
			return ((Number) x).doubleValue() != 0;
		}
		if (x instanceof Collection) {
			return ((Collection) x).size() > 0;
		}
		return true;
	}
}
