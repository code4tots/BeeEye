import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class LogicalBuiltins {

	public static void main(String[] args) {
		BasicBuiltins.install();
		BeeEye.run("(print (any (list 1 2 3)))");
		BeeEye.run("(print (all (list 1 2 3)))");
		BeeEye.run("(print (any (list 0 2 3)))");
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("true", true);
		BeeEye.GLOBAL_SCOPE.put("false", false);

		BeeEye.GLOBAL_SCOPE.put("equals", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return BeeEye.eval(args.get(0), scope).equals(
						BeeEye.eval(args.get(1), scope));
			}
		});

		BeeEye.GLOBAL_SCOPE.put("not", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return !BeeEye.truthy(BeeEye.eval(args.get(0), scope));
			}
		});

		BeeEye.GLOBAL_SCOPE.put("or", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				return BeeEye.truthy(a) ? a : BeeEye.eval(args.get(1), scope);
			}
		});

		BeeEye.GLOBAL_SCOPE.put("and", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				return BeeEye.truthy(a) ? BeeEye.eval(args.get(1), scope) : a;
			}
		});

		BeeEye.GLOBAL_SCOPE.put("all", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Iterable iterable = (Iterable) BeeEye.eval(args.get(0), scope);
				Object last = null;
				for (Object x : iterable) {
					if (!BeeEye.truthy(x))
						return x;
					last = x;
				}
				return last;
			}
		});

		BeeEye.GLOBAL_SCOPE.put("any", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Iterable iterable = (Iterable) BeeEye.eval(args.get(0), scope);
				Object last = null;
				for (Object x : iterable) {
					if (BeeEye.truthy(x))
						return x;
					last = x;
				}
				return last;
			}
		});

		return installed = true;
	}
}
