import java.lang.reflect.Method;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BeeEye {

	public static void main(String[] args) {
		run(slurp(System.in));
	}

	/// convenience functions

	public static String slurp(InputStream input) {
		try {
			byte[] buffer = new byte[input.available()];
			int length = input.read(buffer);
			input.close();
			return new String(buffer, 0, length);
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}

	/// things that are operators in java that should really be functions.

	public static boolean lessThan(Object a, Object b) {
		if (a instanceof Number && b instanceof Number)
			return ((Number) a).doubleValue() < ((Number) b).doubleValue();
		return ((Comparable) a).compareTo(b) < 0;
	}

	public static boolean not(boolean x) {
		return !x;
	}

	/// Reflection utils.

	public static Class[] getTypes(Object[] args) {
		Class[] types = new Class[args.length];
		for (int i = 0; i < args.length; i++)
			types[i] = args[i] == null ? Object.class : args[i].getClass();
		return types;
	}

	public static boolean typesMatch(Class a, Class b) {
		if (a.equals(int.class)) a = Integer.class;
		else if (a.equals(long.class)) a = Long.class;
		else if (a.equals(float.class)) a = Float.class;
		else if (a.equals(double.class)) a = Double.class;
		else if (a.equals(char.class)) a = Character.class;
		else if (a.equals(boolean.class)) a = Boolean.class;
		return a.isAssignableFrom(b);
	}

	public static boolean methodMatches(Method method, String name, Class[] types) {
		if (!method.getName().equals(name))
			return false;

		Class[] mtypes = method.getParameterTypes();

		if (!method.isVarArgs() && mtypes.length != types.length)
			return false;

		int upper = mtypes.length - (method.isVarArgs() ? 1 : 0);

		for (int i = 0; i < upper; i++)
			if (!typesMatch(mtypes[i], types[i]))
				return false;

		return true;
	}

	public static Method findMethod(Class c, String name, Class[] types) {
		try {
			for (Method method : c.getDeclaredMethods())
				if (methodMatches(method, name, types))
					return method;
			return c.getMethod(name, types);
		}
		catch (Exception e) {
			throw new Error(e);
		}
	}

	/// eval

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
		return
			(x instanceof Boolean) ? (Boolean) x :
			(x instanceof String) ? ((String) x).length() > 0 :
			(x instanceof Number) ? ((Number) x).doubleValue() != 0 :
			(x instanceof Collection) ? ((Collection) x).size() > 0 :
			true;
	}

	/// Parser

	public static class Parser {

		public static ArrayList parse(String s) {
			Parser parser = new Parser(s);
			ArrayList result = parser.parseMany();
			assert parser.fin();
			return result;
		}

		private String s;
		private int i;

		public Parser(String ss) {
			s = ss;
			i = 0;
		}

		public char ch() {
			return s.charAt(i);
		}

		public boolean fin() {
			return i >= s.length();
		}

		public void skipSpaces() {
			while (!fin() && Character.isWhitespace(ch()))
				i++;
		}

		public ArrayList parseMany() {
			skipSpaces();
			if (fin() || ch() == ')')
				return new ArrayList();
			else {
				Object x = parseOne();
				ArrayList list = parseMany();
				list.add(0, x);
				return list;
			}
		}

		public Object parseOne() {
			Object ret;
			char q;
			int j;
			StringBuilder sb;
			switch(ch()){
			case '(':
				i++;
				ret = parseMany();
				assert ch() == ')';
				i++;
				break;
			case ')': throw new Error();
			case '"':
			case '\'':
				sb = new StringBuilder();
				q = ch();
				j = i;
				i++;
				while (ch() != q) {
					if (ch() == '\\') {
						i++;
						switch(ch()) {
						case '\\': sb.append('\\'); break;
						case '"': sb.append('"'); break;
						case '\'': sb.append('\''); break;
						case 'n': sb.append('\n'); break;
						case 't': sb.append('\t'); break;
						default: throw new Error(Character.toString(ch()));
						}
					}
					else {
						sb.append(ch());
					}
					i++;
				}
				assert ch() == q;
				i++;
				ArrayList list = new ArrayList();
				list.add("quote");
				list.add(sb.toString());
				ret = list;
				break;
			default:
				j = i;
				while (
						!fin() && ch() != '(' && ch() != ')' &&
						ch() != '"' && ch() != '\'' &&
						!Character.isWhitespace(ch()))
					i++;
				String t = s.substring(j, i);
				if (t.matches("[0-9]+"))
					ret = Integer.parseInt(t);
				else if (t.matches("[0-9]*\\.[0-9]+|[0-9]+\\."))
					ret = Double.parseDouble(t);
				else {
					ret = t;
				}
				break;
			}
			return ret;
		}
	}

	/// Macro

	abstract public static class Macro {
		abstract public Object call(List args, Map<String, Object> scope);
	}

	/// Builtins

	static {

		/// Give prelude a reference to BeeEye.
		/// This way, even if we change packages, we can still uniformly reference it.
		GLOBAL_SCOPE.put("BeeEye", BeeEye.class);

		/// language axioms

		GLOBAL_SCOPE.put("eq", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return
					eval(args.get(0), scope) ==
					eval(args.get(1), scope);
			}
		});

		GLOBAL_SCOPE.put("quote", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return args.get(0);
			}
		});

		GLOBAL_SCOPE.put("cond", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				for (List pair : (List<List>) args) {
					if (truthy(eval(pair.get(0), scope)))
						return evalAll(new ArrayList(pair.subList(1, pair.size())), scope);
				}
				return false;
			}
		});

		GLOBAL_SCOPE.put("lambda", new Macro() {
			public Object call(List args, final Map<String, Object> scope) {
				final List names = (List) args.get(0);
				final List body = new ArrayList(args.subList(1, args.size()));
				return new Macro() {
					public Object call(List args, Map<String, Object> argScope) {
						Map<String, Object> execScope = newScope(scope);
						for (int i = 0; i < names.size(); i++)
							execScope.put(
								(String) names.get(i),
								eval(args.get(i), argScope));
						return evalAll(body, execScope);
					}
				};
			}
		});

		GLOBAL_SCOPE.put("label", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				String name = (String) args.get(0);
				Object value = eval(args.get(1), scope);
				scope.put(name, value);
				return value;
			}
		});

		/// Other conveniences

		GLOBAL_SCOPE.put("true", true);
		GLOBAL_SCOPE.put("false", false);
		GLOBAL_SCOPE.put("null", null);

		GLOBAL_SCOPE.put("macro", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				final List names = (List) args.get(0);
				final List body = new ArrayList(args.subList(1, args.size()));
				return new Macro() {
					public Object call(List macroArgs, Map<String, Object> macroScope) {
						Map<String, Object> execScope = newScope(scope);
						execScope.put((String) names.get(0), macroArgs);
						execScope.put((String) names.get(1), macroScope);
						return evalAll(body, execScope);
					}
				};
			}
		});

		/// Java reflection functions

		GLOBAL_SCOPE.put("get-class", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				return eval(args.get(0), scope).getClass();
			}
		});

		GLOBAL_SCOPE.put("get-class-by-name", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					return Class.forName((String) eval(args.get(0), scope));
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		GLOBAL_SCOPE.put("get-field", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					Object x = eval(args.get(0), scope);
					String name = (String) eval(args.get(1), scope);
					Class c = x.getClass();
					return c.getDeclaredField(name).get(x);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		GLOBAL_SCOPE.put("get-static-field", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					Class c = (Class) eval(args.get(0), scope);
					String name = (String) eval(args.get(1), scope);
					return c.getDeclaredField(name).get(null);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		GLOBAL_SCOPE.put("invoke-method", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					Object x = eval(args.get(0), scope);
					String name = (String) eval(args.get(1), scope);
					Class c = x.getClass();
					Object[] methodArgs = new Object[args.size() - 2];

					for (int i = 2; i < args.size(); i++)
						methodArgs[i-2] = eval(args.get(i), scope);

					Class[] methodTypes = getTypes(methodArgs);

					return findMethod(c, name, methodTypes).invoke(x, methodArgs);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

		GLOBAL_SCOPE.put("invoke-static-method", new Macro() {
			public Object call(List args, Map<String, Object> scope) {
				try {
					Class c = (Class) eval(args.get(0), scope);
					String name = (String) eval(args.get(1), scope);
					Object[] methodArgs = new Object[args.size() - 2];

					for (int i = 2; i < args.size(); i++)
						methodArgs[i-2] = eval(args.get(i), scope);

					Class[] methodTypes = getTypes(methodArgs);

					return findMethod(c, name, methodTypes).invoke(null, methodArgs);
				}
				catch (Exception e) {
					throw new Error(e);
				}
			}
		});

	}
}
