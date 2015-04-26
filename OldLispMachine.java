import java.util.Map;
import java.util.HashMap;

/*
 * Base class for a LispMachine.
 * Subclasses just need to override getAxioms.
 */
public class LispMachine {

	/// object model

	final public static Nil nil = new Nil();
	public static Atom atom(String x) { return new Atom(x); }
	public static Cons cons(Thing a, Thing b) { return new Cons(a, b.asList()); }
	public static List list(Thing... args) {
		List ret = nil;
		for (int i = args.length - 1; i >= 0; i--)
			ret = cons(args[i], ret);
		return ret;
	}

	abstract public static class Thing {
		public Error err() { return new Error(getClass().toString()); }
		public String str() { throw err(); }
		public Thing car() { throw err(); }
		public List cdr() { throw err(); }
		public List asList() { throw err(); }
		public void insert(Thing x) { throw err(); }
		public void insertAtEnd(Thing x) { throw err(); }

		abstract public void appendToStringBuilder(StringBuilder sb);
		public String toString() {
			StringBuilder sb = new StringBuilder();
			appendToStringBuilder(sb);
			return sb.toString();
		}
	}
	final public static class Atom extends Thing {
		final private String x;
		public Atom(String i) { x = i; }
		public String str() { return x; }
		public void appendToStringBuilder(StringBuilder sb) { sb.append(x); }
	}
	abstract public static class List extends Thing {
		public List asList() { return this; }
	}
	final public static class Cons extends List {
		final private Thing a;
		private List b;
		public Cons(Thing ia, List ib) { a = ia; b = ib; }
		public Thing car() { return a; }
		public List cdr() { return b; }
		public void insert(Thing x) { b = cons(x, b); }
		public void insertAtEnd(Thing x) { if (b instanceof Nil) insert(x); else b.insertAtEnd(x); }

	}
	final public static class Nil extends List {}

	/// axioms

	abstract public static class Axiom {
		abstract public String getName();
		abstract public Thing call(LispMachine machine, List data, List args);
	}

	public Axiom[] getAxioms() {
		return new Axiom[0];
	}

	/// eval

	public Thing evalAxiom(Thing f, List args) {
		if (f instanceof Atom) {
			return evalAxiom(list(f), args);
		}
		else if (f instanceof Cons) {
			String name = f.car().str();
			for (Axiom axiom : getAxioms())
				if (name.equals(axiom.getName()))
					return axiom.call(this, f.cdr(), args);
			throw new Error(name);
		}
		throw f.err();
	}

	public Thing eval(Thing d) {
		if (d instanceof Atom) {
			String name = d.str();
			for (Axiom axiom : getAxioms())
				if (name.equals(axiom.getName()))
					return d;
			// TODO: implement a lookup table.
			return d;
		}
		else if (d instanceof Cons) {
			return evalAxiom(eval(d.car()), d.cdr());
		}
		else if (d instanceof Nil) {
			return d;
		}
		throw d.err();
	}

	/// parse

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
				return nil;
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
				ret = cons(atom("quote"), cons(atom(sb.toString()), nil));
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
