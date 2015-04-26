import java.util.ArrayList;

/** S-expression parser. The parsed result is a mixture of
	Integer, Double, String and ArrayList. */
@SuppressWarnings({"unchecked", "rawtypes"})
public class Parser {

	public static void main(String[] args) {
		System.out.println(parse("(hello 'world')"));
	}

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
			else if (t.matches("[0-9]*\\.[0-9]+") || t.matches("[0-9]+\\."))
				ret = Double.parseDouble(t);
			else {
				ret = t;
			}
			break;
		}
		return ret;
	}
}
