import re

RE_WS = re.compile(r'\s*')
RE_QUOTE = re.compile('|'.join((
		r'r"[^"]*"',
		r'"(\\"|[^"])*"',
		r"r'[^']*'",
		r"'(\\'|[^'])*'",
		r'r"""[^"]*"""',
		r'"""(\\"|[^"])*"""',
		r"r'''[^']*'''",
		r"'''(\\'|[^'])*'''",)))
RE_INT = re.compile(r'[0-9]+(?!\.)')
RE_FLOAT = re.compile('|'.join((r'[0-9]+\.[0-9]*', r'\.[0-9]+')))
RE_ID = re.compile(r'[^()"' "'" '\s]+')

def parse(s):
	i = [0]

	def skip_spaces():
		i[0] = RE_WS.match(s, i[0]).end()

	def parse_one():
		skip_spaces()

		if s[i[0]] == '(':
			ret = parse_many()
			skip_spaces()
			assert s[i[0]] == ')'
			i[0] += 1
			return ret

		m = RE_QUOTE.match(s, i[0])
		if m:
			i[0] = m.end()
			return ['quote', eval(m.group())]

		for pattern in (RE_INT, RE_FLOAT):
			m = pattern.match(s, i[0])
			if m:
				i[0] = m.end()
				return eval(m.group())

		m = RE_ID.match(s, i[0])
		if m:
			i[0] = m.end()
			return m.group()

		assert False

	def parse_many():
		xs = []
		skip_spaces()
		while i[0] < len(s) and s[i[0]] != ')':
			xs.append(parse_one())
			skip_spaces()
		return xs

	ret = parse_many()
	assert i[0] == len(s)
	return ret

def eval_(scope, d):
	if isinstance(d, (int, float)):
		return d
	elif isinstance(d, list):
		f = eval_(scope, d[0])
		return f(scope, d[1:])
	assert False, type(d)

GLOBAL_SCOPE = dict()

print parse('1 2 3.3')
