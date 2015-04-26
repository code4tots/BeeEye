import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class CollectionBuiltins {

	public static void main(String[] args) {
		BasicBuiltins.install();
		BeeEye.run("(print (getitem (list 1 2 3) 1))");
		BeeEye.run(
			"(label ll (list 1 2 3))" +
			"(setitem ll 1 3)" +
			"(print ll)");
		BeeEye.run("(print (dict 1 2 3 4))");
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("list", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				List values = new ArrayList();
				for (Object arg : args)
					values.add(BeeEye.eval(arg, scope));
				return values;
			}
		});

		BeeEye.GLOBAL_SCOPE.put("dict", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Map map = new HashMap();
				for (int i = 0; i < args.size(); i += 2) {
					map.put(
						BeeEye.eval(args.get(i), scope),
						BeeEye.eval(args.get(i+1), scope));
				}
				return map;
			}
		});

		BeeEye.GLOBAL_SCOPE.put("getitem", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object x = BeeEye.eval(args.get(0), scope);
				Object i = BeeEye.eval(args.get(1), scope);
				if (x instanceof String)
					return ((String) x).charAt((Integer) i);
				if (x instanceof List)
					return ((List) x).get((Integer) i);
				if (x instanceof Map)
					return ((Map) x).get(i);
				throw new Error(x.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("setitem", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object x = BeeEye.eval(args.get(0), scope);
				Object i = BeeEye.eval(args.get(1), scope);
				Object v = BeeEye.eval(args.get(2), scope);
				if (x instanceof List)
					return ((List) x).set((Integer) i, v);
				if (x instanceof Map)
					return ((Map) x).put(i, v);
				throw new Error(x.getClass().toString());
			}
		});

		return installed = true;
	}
}
