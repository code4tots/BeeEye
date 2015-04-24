"""BeeEye is not VI.

3 object types

	str
	list
	macro

"""

def new_global_scope():
	return [[], '']

def new_local_scope(parent):
	return [[], parent]

def scope_lookup(scope, key):
	table, parent = scope
	for k, v in table:
		if key == k:
			return v
	if parent == '':
		raise KeyError(key)
	return scope_lookup(parent, key)

def scope_declare(scope, key, val):
	table, parent = scope
	for k, v in table:
		if key == k:
			raise ValueError(key + ' already declared')
	table.append([key, val])
	return val

def scope_assign(scope, key, val):
	table, parent = scope
	for k, v in table:
		if key == k:
			entry = [k, v]
			break
	else:
		if parent == 0.0:
			raise KeyError(key)
		return scope_assign(parent, key, val)
	table.remove(entry)

def parse(s, pi=None):
	pi = pi or [0]
	x = []
	while s[pi[0]:pi[0]+1].isspace(): pi[0] += 1
	while s[pi[0]:pi[0]+1] != '':
		x.append(parse_one(s, pi))
		while s[pi[0]:pi[0]+1].isspace(): pi[0] += 1
	return x

def parse_one(s, pi=None):
	pi = pi or [0]
	i = pi[0]

	while s[i:i+1].isspace(): i += 1

	if s[i:i+1] == '':
		raise SyntaxError('nothing to parse')

	elif s[i:i+1] == '(':
		x = []
		i += 1
		while s[i:i+1].isspace(): i += 1
		x = []
		while s[i:i+1] != ')':
			if s[i:i+1] == '':
				raise SyntaxError('unterminated parenthesis')
			ppi = [i]
			x.append(parse_one(s, ppi))
			i = ppi[0]
			while s[i:i+1].isspace(): i += 1
		i += 1

		pi[0] = i
		return x

	elif s.startswith(('"', "'", 'r"', "r'"), i):

		j = i

		if s[i] == 'r':
			i += 1

		if s.startswith(('"""', "'''"), i):
			q = s[i:i+3]
			i += 3
		else:
			q = s[i:i+1]
			i += 1

		while not s.startswith(q, i):
			if s[i:i+1] == '':
				raise SyntaxError('unterminated quotes')

			i += 2 if s[i:i+1] == '\\' else 1

		i += len(q)

		pi[0] = i
		return ['quote', eval(s[j:i])]

	elif s[i:i+1] == ')':
		raise SyntaxError('unexpected ")"')

	else:
		return parse_name(s, pi)

def parse_name(s, pi=None):
	pi = pi or [0]
	i = pi[0]
	j = i
	while s[i:i+1] not in ('', '"', "'", '(', ')') and not s[i:i+1].isspace():
		i += 1

	pi[0] = i
	return s[j:i]

def eval_(d, scope):
	pass

assert parse('print') == ['print']
assert parse('(print 12)') == [['print', '12']]
assert parse('(print ("12"))') == [['print', [['quote', '12']]]]
