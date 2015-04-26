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

		return installed = true;
	}
}
