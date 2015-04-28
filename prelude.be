(label comment (macro (args scope)))

(comment
	Hello world! I am a comment.)

(comment
	Some super basic things ...)

(label print (lambda (x)
	(invoke-method (get-static-field (get-class-by-name 'java.lang.System') 'out') 'println' x)))

(label not (lambda (x)
	(invoke-static-method BeeEye 'not' x)))

(label = (lambda (a b)
	(invoke-method a 'equals' b)))

(label < (lambda (a b)
	(invoke-static-method BeeEye 'lessThan' a b)))

(label <= (lambda (a b)
	(cond
		((< a b) true)
		((= a b) true)
		(true false))))

(label > (lambda (a b) (not (<= a b))))

(label >= (lambda (a b) (not (< a b))))

(label eval (lambda (d scope)
	(invoke-static-method BeeEye 'eval' d scope)))

(label if (macro (args scope)
	(cond
		((eval (invoke-method args 'get' 0) scope)
			(eval (invoke-method args 'get' 1) scope))
		(true
			(cond
				((< (eval (invoke-method args 'size') scope) 3) null)
				(true (eval (invoke-method args 'get' 2) scope)))))))

(print (if 0 'condition was true' 'condition was false'))
