import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* Builtins for helping use java stuff */
@SuppressWarnings({"unchecked", "rawtypes"})
public class JavaBuiltins {

	public static void main(String[] args) {
		BasicBuiltins.install();
		BeeEye.run("(print (call-java-method 32 toString))");
		BeeEye.run("(print (call-java-method 32 toString))");
		BeeEye.run("(print (get-class-by-name 'java.lang.Integer'))");
		BeeEye.run("(print (call-java-static-method (get-class-by-name 'java.lang.Integer') parseInt '3'))");
	}

	public static java.lang.reflect.Method findMethod(Class c, String name, Object[] args) {
		Class[] types = new Class[args.length];
		for (int i = 0; i < args.length; i++)
			types[i] = args[i].getClass();
		try {
			return c.getMethod(name, types);
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("get-class-by-name", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					return Class.forName((String) BeeEye.eval(args.get(0), scope));
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		BeeEye.GLOBAL_SCOPE.put("get-class", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return BeeEye.eval(args.get(0), scope).getClass();
			}
		});

		BeeEye.GLOBAL_SCOPE.put("call-java-method", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object x = BeeEye.eval(args.get(0), scope);
				String methodName = (String) args.get(1);
				Class c = x.getClass();
				Object[] methodArgs = new Object[args.size()-2];

				for (int i = 2; i < args.size(); i++)
					methodArgs[i-2] = BeeEye.eval(args.get(i), scope);

				try {
					return findMethod(c, methodName, methodArgs).invoke(x, methodArgs);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		BeeEye.GLOBAL_SCOPE.put("call-java-static-method", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Class c= (Class) BeeEye.eval(args.get(0), scope);
				String methodName = (String) args.get(1);
				Object[] methodArgs = new Object[args.size()-2];

				for (int i = 2; i < args.size(); i++)
					methodArgs[i-2] = BeeEye.eval(args.get(i), scope);

				try {
					return findMethod(c, methodName, methodArgs).invoke(null, methodArgs);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		return installed = true;
	}
}
