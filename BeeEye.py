"""BeeEye is not VI.

3 object types

	str
	list
	macro

"""

GLOBALS = []

def new_global_scope():
	return [[[k, v] for k, v in GLOBALS], '']

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
	t = s[j:i]

	if all(c.isdigit() or c in '.' for c in t):
		return ['quote', t]
	else:
		return s[j:i]

def eval_(d, scope):
	if isinstance(d, str):
		result = scope_lookup(scope, d)
	elif isinstance(d, list):
		result = eval_(d[0], scope)(d[1:], scope)
	else:
		raise ValueError((type(d), d))

	if isinstance(result, (str, list)) or callable(result):
		return result
	elif isinstance(result, bool):
		return '1' if result else ''
	elif isinstance(result, (int, float)):
		return str(result)
	elif isinstance(result, tuple):
		return list(result)
	else:
		raise ValueError((type(result), result))

def eval_all(dd, scope):
	last = ''
	for d in dd:
		last = eval_(d, scope)
	return last

def run(string, scope):
	return eval_all(parse(string), scope)

def wrap_function(f):
	def builtin_function(args, scope):
		return f(*[eval_(arg, scope) for arg in args])
	builtin_function.__name__ = f.__name__
	return builtin_function

def builtin_quote(args, scope):
	q, = args
	return q
GLOBALS.append(['quote', builtin_quote])

@wrap_function
def builtin_print(*args):
	last = ''
	if args:
		sys.stdout.write(str(args[0]))
		last = args[0]
		for arg in args[1:]:
			sys.stdout.write(' ')
			sys.stdout.write(str(arg))
			last = arg
	sys.stdout.write('\n')
	return last
GLOBALS.append(['print', builtin_print])

@wrap_function
def builtin_strcat(*args):
	return ''.join(args)
GLOBALS.append(['strcat', builtin_strcat])

@wrap_function
def builtin_add(a, b):
	return float(a) + float(b)
GLOBALS.append(['add', builtin_add])

@wrap_function
def builtin_subtract(a, b):
	return float(a) - float(b)
GLOBALS.append(['subtract', builtin_subtract])

@wrap_function
def builtin_multiply(a, b):
	return float(a) * float(b)
GLOBALS.append(['multiply', builtin_multiply])

@wrap_function
def builtin_divide(a, b):
	return float(a) / float(b)
GLOBALS.append(['divide', builtin_divide])

@wrap_function
def builtin_modulo(a, b):
	return float(a) % float(b)
GLOBALS.append(['modulo', builtin_modulo])

@wrap_function
def builtin_length(xs):
	return len(xs)
GLOBALS.append(['length', builtin_length])

@wrap_function
def builtin_getitem(xs, i):
	return xs[int(float(i))]
GLOBALS.append(['getitem', builtin_getitem])

@wrap_function
def builtin_setitem(xs, i, x):
	xs[int(float(i))] = x
	return x
GLOBALS.append(['setitem', builtin_setitem])

@wrap_function
def builtin_list(*args):
	return args
GLOBALS.append(['list', builtin_list])

def builtin_def(args, scope):
	name, val_ast = args
	val = eval_(val_ast, scope)
	return scope_declare(name, val)
GLOBALS.append(['def', builtin_def])

def builtin_let(args, scope):
	name, val_ast = args
	val = eval_(val_ast, scope)
	return scope_assign(name, val)
GLOBALS.append(['let', builtin_let])

@wrap_function
def builtin_lexicographically_ordered(*args):
	return all(a < b for a, b in zip(args[:-1], args[1:]))
GLOBALS.append(['lexicographically-ordered', builtin_lexicographically_ordered])

def builtin_lambda(args, scope):
	arg_names = args[0]
	body = args[1:]

	@wrap_function
	def lambda_(*vals):
		exec_scope = new_local_scope(scope)
		for name, val in zip(arg_names, vals):
			scope_declare(exec_scope, name, val)
		scope_declare(exec_scope, '__args__', list(vals))
		return eval_all(body, exec_scope)

	return lambda_
GLOBALS.append(['lambda', builtin_lambda])

assert parse('print') == ['print']
assert parse('(print 12)') == [['print', ['quote', '12']]]
assert parse('(print ("12"))') == [['print', [['quote', '12']]]]

scope = new_global_scope()

assert run('(quote x)', scope) == 'x'
assert run('(strcat "a" "b")', scope) == 'ab'
assert run('(add 1 2)', scope) == '3.0'
assert run('(subtract 1 2.0)', scope) == '-1.0'
assert run('(multiply 1.5 2)', scope) == '3.0'
assert run('(divide 5 4)', scope) == '1.25'
assert run('(modulo 7.2 4)', scope) == '3.2'
assert run('(length 1234)', scope) == '4'
assert run('(getitem 1234 1)', scope) == '2'
assert run('(getitem (list 4 3 2 1) 1)', scope) == '3'
assert run('(list 1 2 3)', scope) == ['1', '2', '3']
assert run('(lexicographically-ordered 10 200 3)', scope) == '1'
assert run('(lexicographically-ordered 3 20)', scope) == ''
assert run('((lambda (x) (strcat x "def")) "abc")', scope) == 'abcdef'

PRELUDE = """

"""
