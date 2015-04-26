public class LispMachine {

	public static void main(String[] args) {
		System.out.println(parse("(1 2 3) 4 5. 6 'hello' there"));
		System.out.println(eval(parse("hello"), new Axiom[0]));
	}



	//// Everything static beyond this point.

	/// axiom.

	abstract public static class Axiom {
		abstract public String getName();
		abstract public Thing call(Cons data, Cons args);
	}

	/// eval.

	private static Thing evalAxiom(Thing f, Cons args, Axiom[] axioms) {
		if (f instanceof Atom) {
			return evalAxiom(list(f), args, axioms);
		}
		else if (f instanceof Cons) {
			String name = f.car().str();
			for (Axiom axiom : axioms)
				if (name.equals(axiom.getName()))
					return axiom.call(f.cdr(), args);
			throw new Error(name);
		}
		throw f.err();
	}

	public static Thing eval(Thing d, Axiom[] axioms) {
		if (d instanceof Atom) {
			String name = d.str();
			for (Axiom axiom : axioms)
				if (name.equals(axiom.getName()))
					return d;
			// TODO: implement a lookup table.
			return d;
		}
		else if (d instanceof Cons) {
			if (d.nil())
				return d;
			return evalAxiom(eval(d.car(), axioms), d.cdr(), axioms);
		}
		throw d.err();
	}

	public static Thing evalAll(Thing d, Axiom[] axioms) {
		
	}

	/// Object model.

	public static Atom atom(String x) { return new Atom(x); }
	public static Cons cons() { return new Cons(); }
	public static Cons cons(Thing a, Thing b) { return new Cons(a, (Cons) b); }
	public static Cons list(Thing... args) {
		Cons ret = cons();
		for (int i = args.length - 1; i >= 0; i--)
			ret = cons(args[i], ret);
		return ret;
	}

	abstract public static class Thing {
		public Error err() { return new Error(getClass().toString()); }

		// atom methods
		public String str() { throw err(); }

		// list methods
		public Thing car() { throw err(); }
		public Cons cdr() { throw err(); }
		public void push(Thing x) { throw err(); }
		public Thing pop() { throw err(); }

		// all methods
		abstract public boolean nil();
		abstract public void toStringBuilder(StringBuilder sb);

		public String toString() {
			StringBuilder sb = new StringBuilder();
			toStringBuilder(sb);
			return sb.toString();
		}
	}

	final public static class Atom extends Thing {
		private String x;
		public Atom(String i) { x = i; }

		public String str() { return x; }

		public boolean nil() { return false; }
		public void toStringBuilder(StringBuilder sb) { sb.append(x); }
	}

	final public static class Cons extends Thing {
		private Thing a;
		private Cons b;
		public Cons() { a = b = null; }
		public Cons(Thing ia, Cons ib) { a = ia; b = ib; }

		public Thing car() { return a; }
		public Cons cdr() { return b; }
		public void push(Thing x) { b = new Cons(a, b); a = x; }
		public Thing pop() { Thing r = a; a = b.car(); b = b.cdr(); return r; }

		public boolean nil() { return a == null; }
		public void toStringBuilder(StringBuilder sb) {
			sb.append('(');
			toStringBuilderWithoutParens(sb);
			sb.append(')');
		}
		public void toStringBuilderWithoutParens(StringBuilder sb) {
			if (!nil()) {
				a.toStringBuilder(sb);
				if (!b.nil()) {
					sb.append(' ');
					b.toStringBuilderWithoutParens(sb);
				}
			}
		}
	}

	/// parse.

	public static Thing parse(String s) {
		Parser parser = new Parser(s);
		Thing result = parser.parseMany();
		if (!parser.fin())
			throw new Error();
		return result;
	}

	final public static class Parser {
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

		public Thing parseMany() {
			skipSpaces();
			if (fin() || ch() == ')')
				return cons();
			else
				return cons(parseOne(), parseMany());
		}

		public Thing parseOne() {
			Thing ret;
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
				ret = cons(atom("quote"), cons(atom(sb.toString()), cons()));
				break;
			default:
				j = i;
				while (
						!fin() && ch() != '(' && ch() != ')' &&
						ch() != '"' && ch() != '\'' &&
						!Character.isWhitespace(ch()))
					i++;
				ret = atom(s.substring(j, i));
				break;
			}
			return ret;
		}
	}
}