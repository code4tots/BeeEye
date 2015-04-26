class Utils {
	static Thing nil = new Nil();
	static Thing str(String s) { return new Str(s); }
	static Thing pair(Thing a, Thing b) { return new Pair(a, b); }
}

public class BeeEye extends Utils {
	public static Thing parse(String s) {
		Parser parser = new Parser(s);
		Thing result = parser.parseMany();
		if (!parser.fin())
			throw new Error();
		return result;
	}

	public static void main(String[] args) {
		System.out.println(parse("(a b (  c 'hello'))"));
	}
}

class Parser extends Utils {
	String s;
	int i;

	Parser(String ss) {
		s = ss;
		i = 0;
	}

	char ch() {
		return s.charAt(i);
	}

	boolean fin() {
		return i >= s.length();
	}

	void skipSpaces() {
		while (!fin() && Character.isWhitespace(ch()))
			i++;
	}

	Thing parseMany() {
		skipSpaces();
		if (fin() || ch() == ')')
			return nil;
		else
			return pair(parseOne(), parseMany());
	}

	Thing parseOne() {
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
			ret = pair(str("quote"), pair(str(sb.toString()), nil));
			break;
		default:
			j = i;
			while (
					!fin() && ch() != '(' && ch() != ')' &&
					ch() != '"' && ch() != '\'' &&
					!Character.isWhitespace(ch()))
				i++;
			ret = str(s.substring(j, i));
			break;
		}
		return ret;
	}
}

abstract class Thing extends Utils {
	String getStr() { return null; }
	Thing getA() { return null; }
	Thing getB() { return null; }
	abstract void appendToStringBuilder(StringBuilder sb);
	void appendWithoutParenthesis(StringBuilder sb) { throw new Error(); }
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendToStringBuilder(sb);
		return sb.toString();
	}
}

class Str extends Thing {
	final String x;
	Str(String i) { x = i; }
	String getStr() { return x; }
	void appendToStringBuilder(StringBuilder sb) { sb.append(x); }
}

class Listy extends Thing {
	void appendToStringBuilder(StringBuilder sb) { sb.append("(");appendWithoutParenthesis(sb);sb.append(")"); }
}

class Pair extends Listy {
	final Thing a, b;
	Pair(Thing ta, Thing tb) { a = ta; b = tb; assert b instanceof Listy;}
	Thing getA() { return a; }
	Thing getB() { return b; }
	void appendWithoutParenthesis(StringBuilder sb) {
		a.appendToStringBuilder(sb);
		if (!(b instanceof Nil))
			sb.append(" ");
		b.appendWithoutParenthesis(sb);
	}
}

class Nil extends Listy {
	void appendWithoutParenthesis(StringBuilder sb) {}
}
