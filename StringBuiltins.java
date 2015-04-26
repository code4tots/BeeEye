import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class StringBuiltins {

	public static void main(String[] args) {
		BasicBuiltins.install();
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("strcat", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				StringBuilder sb = new StringBuilder();
				for (Object arg : args)
					sb.append((String) BeeEye.eval(arg, scope));
				return sb.toString();
			}
		});

		return installed = true;
	}
}
