import java.util.List;
import java.util.Map;

public class ArithmeticBuiltins {

	public static void main(String[] args) {
		install();
		BasicBuiltins.install();
		BeeEye.run("(print '(+ 2 3) ='(+ 2 3))");
		BeeEye.run("(print '(- 2 3) ='(- 2 3))");
		BeeEye.run("(print '(* 2 3) ='(* 2 3))");
		BeeEye.run("(print '(/ 2 3) ='(/ 2 3))");
		BeeEye.run("(print '(% 2 3) ='(% 2 3))");
		BeeEye.run("(print '(% 2.3 1) =' (% 2.3 1))");
		BeeEye.run("(print '(% 2 1.2) =' (% 2 1.2))");
		BeeEye.run("(print '(% 2.1 1.2) =' (% 2.1 1.2))");
	}

	private static boolean installed = false;

	public static boolean install() {
		if (installed)
			return false;

		BeeEye.GLOBAL_SCOPE.put("+", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) + ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() + (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) + ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) + ((Double) b);
					}
				}
				if (a instanceof String) {
					if (b instanceof String) {
						return ((String) a) + ((String) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("-", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) - ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() - (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) - ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) - ((Double) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("*", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) * ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() * (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) * ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) * ((Double) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("/", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) / ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() / (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) / ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) / ((Double) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("%", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) % ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() % (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) % ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) % ((Double) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		BeeEye.GLOBAL_SCOPE.put("<", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				Object a = BeeEye.eval(args.get(0), scope);
				Object b = BeeEye.eval(args.get(1), scope);
				if (a instanceof Integer) {
					if (b instanceof Integer) {
						return ((Integer) a) < ((Integer) b);
					}
					else if (b instanceof Double) {
						return ((Integer) a).doubleValue() < (Double) b;
					}
				}
				if (a instanceof Double) {
					if (b instanceof Integer) {
						return ((Double) a) < ((Integer) b).doubleValue();
					}
					if (b instanceof Double) {
						return ((Double) a) < ((Double) b);
					}
				}
				if (a instanceof String) {
					if (b instanceof String) {
						return ((String) a) < ((String) b);
					}
				}
				throw new Error(a.getClass().toString() + " " + b.getClass().toString());
			}
		});

		return installed = true;
	}
}
