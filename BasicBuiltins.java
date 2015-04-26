import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BasicBuiltins {

	public static void main(String[] args) {
		install();
		BeeEye.run("(print 'hi' 'there' 3 4.1)");
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		McCarthyBuiltins.install();

		BeeEye.GLOBAL_SCOPE.put("equals", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return BeeEye.eval(args.get(0), scope).equals(
						BeeEye.eval(args.get(1), scope));
			}
		});

		BeeEye.GLOBAL_SCOPE.put("strcat", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				StringBuilder sb = new StringBuilder();
				for (Object arg : args)
					sb.append((String) BeeEye.eval(arg, scope));
				return sb.toString();
			}
		});

		BeeEye.GLOBAL_SCOPE.put("print", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				if (args.size() > 0) {
					System.out.print(BeeEye.eval(args.get(0), scope));
					for (Object arg : args.subList(1, args.size())) {
						System.out.print(' ');
						System.out.print(BeeEye.eval(arg, scope));
					}
				}
				System.out.println();
				return args.get(args.size() - 1);
			}
		});

		BeeEye.GLOBAL_SCOPE.put("true", true);
		BeeEye.GLOBAL_SCOPE.put("false", false);

		return installed = true;
	}
}
