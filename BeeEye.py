import operator
import sys

BUILTINS = set()

PRELUDE = """
"""

def wrap_function(f):
	def builtin_function(args, scope):
		return f(*[eval_(arg, scope) for arg in args])
	builtin_function.__name__ = f.__name__
	return builtin_function

def add_builtin_macro(macro):
	BUILTINS.add(macro)
	return macro

def add_builtin_function(builtin):
	return add_builtin_macro(wrap_function(builtin))

class Scope(object):

	def __init__(self):
		self.table = dict()

	def declare(self, key, val):
		self.table[key] = val

class GlobalScope(Scope):

	def __init__(self):
		super(GlobalScope, self).__init__()
		for builtin in BUILTINS:
			self.declare(builtin.__name__[len('be_'):], builtin)

	def __setitem__(self, key, val):
		if key not in self.table:
			raise KeyError(key)

		self.table[key] = val

	def __getitem__(self, key):
		return self.table[key]

class LocalScope(Scope):

	def __init__(self, parent):
		super(LocalScope, self).__init__()
		self.parent = parent

	def __setitem__(self, key, val):
		if key in self.table:
			self.table[key] = val
		else:
			self.parent[key] = val

	def __getitem__(self, key):
		return (self.table if key in self.table else self.parent)[key]

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

	else:
		j = i
		while s[i:i+1] not in ('', '"', "'", '(', ')') and not s[i:i+1].isspace():
			i += 1

		pi[0] = i
		return s[j:i]

def eval_(d, scope):
	if isinstance(d, str):
		if d.startswith('$'):
			return scope[d[1:]]
		else:
			return d

	elif isinstance(d, list):
		f = eval_(d[0], scope)

		if isinstance(f, str):
			f = scope[f]

		return f(d[1:], scope)

	else:
		raise ValueError("Can't eval object of type %s" % type(d))

def eval_all(dd, scope):
	last = ''
	for d in dd:
		last = eval_(d, scope)
	return last

def run(string, scope):
	return eval_all(parse(string), scope or GlobalScope())

def exec_(string, scope=None):
	scope = scope or GlobalScope()
	run(PRELUDE, scope)
	return run(string, scope)

@add_builtin_function
def be_strcat(*args):
	return ''.join(args)

@add_builtin_function
def be_floor(x):
	return str(int(float(x)))

@add_builtin_function
def be_mul(a, b):
	return str(float(a) * float(b))

@add_builtin_function
def be_div(a, b):
	return str(float(a) / float(b))

@add_builtin_function
def be_mod(a, b):
	return str(float(a) % float(b))

@add_builtin_function
def be_add(a, b):
	return str(float(a) + float(b))

@add_builtin_function
def be_sub(a, b):
	return str(float(a) - float(b))

@add_builtin_function
def be_print(*args):
	last = ''
	if args:
		sys.stdout.write(str(args[0]))
		for arg in args[1:]:
			sys.stdout.write(' ')
			sys.stdout.write(str(arg))
			last = arg
	sys.stdout.write('\n')
	return last

@add_builtin_macro
def be_quote(args, scope):
	q, = args
	return q

@add_builtin_macro
def be_lambda(args, scope):
	arg_names = args[0]
	body = args[1:]

	@wrap_function
	def lambda_(*args):
		body_scope = LocalScope(scope)
		for name, arg in zip(arg_names, args):
			body_scope.declare(name, arg)
		return eval_all(body, body_scope)

	return lambda_

@add_builtin_macro
def be_def(args, scope):
	name, val = args
	val = eval_(val, scope)
	scope.declare(name, val)
	return val

assert parse('print') == ['print']
assert parse('(print 12)') == [['print', '12']]
assert parse('(print (12))') == [['print', ['12']]]
assert exec_('') == ''
assert exec_('abc') == 'abc'
assert exec_('(add 1 2)') == '3.0'
assert exec_('(strcat 1 2)') == '12'
assert exec_('(floor (add .5 .5))') == '1'
assert exec_('(sub 1 2)') == '-1.0'
assert exec_('(mul 3 4)') == '12.0'
assert exec_('(div 3 4)') == '0.75'
assert exec_('(mod 3 4)') == '3.0'
assert exec_('((lambda (x) (strcat abc $x)) def)') == 'abcdef'
