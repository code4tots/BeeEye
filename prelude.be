(label comment (macro (scope)))

(comment ============================================================
	Hello world! I am a comment.)

(comment ============================================================
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

(label eval (lambda (scope d)
	(invoke-static-method BeeEye 'eval' scope d)))

(label if (macro* (scope c a args)
	(cond
		((eval scope c)
			(eval scope a))
		(true
			(cond
				((< (eval scope (invoke-method args 'size')) 1) null)
				(true (eval scope (invoke-method args 'get' 0))))))))

(comment ============================================================
	Shell)

(print (if 0 'condition was true' 'condition was false'))
(print (if 0 'condition was true'))
