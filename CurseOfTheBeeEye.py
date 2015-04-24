import curses

from BeeEye import add_builtin_function, add_builtin_macro, exec_, eval_, eval_all, Int

STDSCR = None

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

def main(stdscr):
	global STDSCR
	STDSCR = stdscr
	curses.curs_set(0)

	exec_("""

(def i 0)
(while (< $i 10)
	(putch (* 3 $i) $i x)
	(let i (+ $i 1))
)

(putch 20 20 (getkey))

(getkey)

(def x 0)
(def y 0)


""")


if __name__ == '__main__':
	curses.wrapper(main)
