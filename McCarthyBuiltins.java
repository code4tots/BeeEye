import java.util.List;
import java.util.Map;

/*
Some of McCarthy's builtins seem kind of unnecessary given the object model.
Will add them as they are found to be useful.
*/

@SuppressWarnings({"unchecked", "rawtypes"})
public class McCarthyBuiltins {

	public static void main(String[] args) {
		install();
		BasicBuiltins.install();
		BeeEye.run("(label f 3) (print f)");
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("eq", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return
					BeeEye.eval(args.get(0), scope) ==
					BeeEye.eval(args.get(1), scope);
			}
		});

		BeeEye.GLOBAL_SCOPE.put("quote", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return args.get(0);
			}
		});

		BeeEye.GLOBAL_SCOPE.put("cond", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				for (List pair : (List<List>) args) {
					if (BeeEye.truthy(BeeEye.eval(pair.get(0), scope)))
						return BeeEye.eval(pair.get(1), scope);
				}
				return false;
			}
		});

		BeeEye.GLOBAL_SCOPE.put("lambda", new Macro() {
			public Object call(List args, final Map<String, Object> scope) {
				final List names = (List) args.get(0);
				final Object body = args.get(1);
				return new Macro() {
					public Object call(List args, Map<String, Object> argScope) {
						Map<String, Object> execScope = BeeEye.newScope(scope);
						for (int i = 0; i < names.size(); i++)
							execScope.put(
								(String) names.get(i),
								BeeEye.eval(args.get(i), argScope));
						return BeeEye.eval(body, execScope);
					}
				};
			}
		});

		BeeEye.GLOBAL_SCOPE.put("label", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				String name = (String) args.get(0);
				Object value = BeeEye.eval(args.get(1), scope);
				scope.put(name, value);
				return value;
			}
		});

		return installed = true;
	}
}
