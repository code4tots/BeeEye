(label System (get-class-by-name 'java.lang.System'))
(label out (get-static-field System 'out'))
(invoke-method out 'println' "Hello world!")
