import curses

from BeeEye import add_builtin_function, add_builtin_macro, exec_, eval_, eval_all, Int, PRELUDES

STDSCR = None

PRELUDES.append("""

(def get-dim getdim)

(def get-width (lambda ()
	(getitem (get-dim) 0)
))

(def get-height (lambda ()
	(getitem (get-dim) 1)
))

""")

@add_builtin_function
def be_getdim():
	y, x = map(str, STDSCR.getmaxyx())
	return [x, y]

@add_builtin_function
def be_putch(x, y, ch):
	STDSCR.addch(Int(y), Int(x), ch)
	return ch

@add_builtin_function
def be_getkey():
	return STDSCR.getkey()

@add_builtin_function
def be_clear():
	STDSCR.clear()
	return ''

def main(stdscr):
	global STDSCR
	STDSCR = stdscr
	curses.curs_set(0)

	with open('sample.cbe') as f:
		exec_(f.read())

if __name__ == '__main__':
	curses.wrapper(main)
