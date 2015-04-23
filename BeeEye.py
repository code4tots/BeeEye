"""BeeEye is not VI.

4 basic object types

	str
	list
	dict
	macro

Only str and list are used in the ast.

"""

import sys

BUILTINS = dict()

PRELUDE = """

(def # $len)

"""

def wrap_function(f):
	def builtin_function(args, scope):
		return f(*[eval_(arg, scope) for arg in args])
	builtin_function.__name__ = f.__name__
	return builtin_function

def add_builtin(name, f):
	BUILTINS[name] = f
	return f

def add_builtin_macro(macro):
	return add_builtin(macro.__name__[len('be_'):], macro)

def add_builtin_function(builtin):
	return add_builtin_macro(wrap_function(builtin))

def new_global_scope():
	return [dict(BUILTINS), '']

def new_local_scope(parent):
	return [dict(), parent]

def scope_lookup(scope, key):
	table, parent = scope
	if key in table:
		return table[key]
	if parent == '':
		raise KeyError(key)
	return scope_lookup(parent, key)

def scope_declare(scope, key, val):
	table, parent = scope
	if key in table:
		raise ValueError(key + ' already declared')
	table[key] = val

def scope_assign(scope, key, val):
	table, parent = scope
	if key in table:
		table[key] = val
		return val
	if parent == '':
		raise KeyError(key)
	return scope_assign(parent, key, val)

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

	elif s.startswith(')'):
		raise SyntaxError('unexpected ")"')

	else:
		j = i
		while s[i:i+1] not in ('', '"', "'", '(', ')') and not s[i:i+1].isspace():
			i += 1

		pi[0] = i
		return s[j:i]

def eval_(d, scope):
	if isinstance(d, str):
		if d.startswith('$'):
			return scope_lookup(scope, d[1:])
		else:
			return d

	elif isinstance(d, list):
		f = eval_(d[0], scope)

		if isinstance(f, str):
			f = scope_lookup(scope, f)

		return f(d[1:], scope)

	else:
		raise ValueError("Can't eval object of type %s" % type(d))

def eval_all(dd, scope):
	last = ''
	for d in dd:
		last = eval_(d, scope)
	return last

def run(string, scope):
	return eval_all(parse(string), scope or new_global_scope())

def exec_(string, scope=None):
	scope = scope or new_global_scope()
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
		last = args[0]
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
		body_scope = new_local_scope(scope)
		for name, arg in zip(arg_names, args):
			scope_declare(body_scope, name, arg)
		return eval_all(body, body_scope)

	return lambda_

@add_builtin_macro
def be_def(args, scope):
	name, val = args
	val = eval_(val, scope)
	scope_declare(scope, name, val)
	return val

@add_builtin_macro
def be_let(args, scope):
	name, val = args
	val = eval_(val, scope)
	scope_assign(scope, name, val)
	return val

@add_builtin_function
def be_id(x):
	return id(x)

@add_builtin_function
def be_eq(a, b):
	return a == b

@add_builtin_function
def be_eval(d, scope):
	return eval_(d, scope)

@add_builtin_macro
def be_macro(args, scope):
	(args_name, scope_name), = args[:1]
	body = args[1:]

	def macro(macro_args, macro_scope):
		exec_scope = new_local_scope(scope)
		scope_declare(exec_scope, args_name, macro_args)
		scope_declare(exec_scope, scope_name, macro_scope)
		return eval_all(body, exec_scope)

	return macro

@add_builtin_function
def be_len(x):
	return str(len(x))

@add_builtin_function
def be_list(*args):
	return list(args)

@add_builtin_function
def be_dict(*args):
	return dict(zip(args[::2], args[1::2]))

assert parse('print') == ['print'] 
assert parse('(print 12)') == [['print', '12']]
assert parse('(print ("12"))') == [['print', [['quote', '12']]]]
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
assert exec_('(quote x)') == 'x'
assert exec_('(len abc567)') == '6'
assert exec_('(list 1 2 3)') == ['1', '2', '3']
assert exec_('(len (list 1 2 3))') == '3'
assert exec_('(dict 1 2 3 4)') == {'1': '2', '3': '4'}
assert exec_('(len (dict 1 2 3 4))') == '2'
exec_("""
(print (len abc567))
(print (# (list 1 2 3)))
""")
