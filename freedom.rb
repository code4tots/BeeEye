def assert &block
	raise RuntimeError unless yield
end

module Mtots module Freedom

def self.parse s
	Parser.new(s).parse
end

class Parser
	RE_WS = /\s*/m
	RE_QUOTE = /
		r"[^"]*" |
		"(\\"|[^"])*" |
		r'[^']*' |
		'(\\'|[^'])*' |
		r"""[^"]*""" |
		"""(\\"|(?!"""))*""" |
		r'''[^']*''' |
		'''(\\'|(?!'''))*'''/xm
	RE_FLOAT = /[0-9]+\.[0-9]*|\.[0-9]+/
	RE_INT = /[0-9]+/
	RE_ID = /[^()"'\s]+/

	def initialize s
		@s = s
		@i = 0
	end

	def c
		@s[@i]
	end

	def fin
		@i >= @s.size
	end

	def parse
		xs = parse_many
		assert { fin }
		xs
	end

	def skip_spaces
		@i += RE_WS.match(@s, @i).to_s.size
	end

	def parse_one
		skip_spaces

		p [c, RE_QUOTE.match(@s, @i)]

		if c == '('
			@i += 1
			ret = parse_many
			skip_spaces
			assert { c == ')' }
			@i += 1
			skip_spaces
			return ret
		end

		m = RE_QUOTE.match(@s, @i)
		if m
			s = m.to_s
			@i += s.size
			# TODO: handle 'r' case, and don't do interpolation.
			skip_spaces
			return ['quote', eval(s)]
		end

		m = RE_FLOAT.match(@s, @i)
		if m
			s = m.to_s
			@i += s.size
			skip_spaces
			return s.to_f
		end

		m = RE_INT.match(@s, @i)
		if m
			s = m.to_s
			@i += s.size
			skip_spaces
			return s.to_i
		end

		m = RE_ID.match(@s, @i)
		if m
			s = m.to_s
			@i += s.size
			skip_spaces
			return s
		end

		assert { false }
	end

	def parse_many
		xs = []
		skip_spaces
		xs.push parse_one and skip_spaces while !fin && c != ')'
		xs
	end
end

end end

# Mtots::Freedom.parse 'hi'

p Mtots::Freedom.parse '(hello "world!")'
