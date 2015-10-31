#lang plai-typed

(define-type ArithC
  [numC (n : number)]
  [plusC (l : ArithC) (r : ArithC)]
  [multC (l : ArithC) (r : ArithC)]
  [eqC (l : ArithC) (r : ArithC)]
  [ifC (if : ArithC) (then : ArithC) (else : ArithC)])




(define (interp [a : ArithC]) : number
  (type-case ArithC a
    [numC (n) n]
    [plusC (l r) (+ (interp l) (interp r))]
    [multC (l r) (* (interp l) (interp r))]
    [eqC   (l r) (= l r)]
    [ifC (i t e) (if i t e)]))
          

 (define (= [l : ArithC] [r : ArithC]) : number
   (cond
     [(equal? (interp l) (interp r)) 1]
     [(not(equal? (interp l) (interp r))) 0]))
 
 (define (if [i : ArithC] [t : ArithC] [e : ArithC]) : number
    (type-case ArithC i
           [numC (n)(cond
                    [(not(equal? (numC n) (numC 0)))(interp t)]
                    [(equal? (numC n) (numC 0)) (interp e)])]
           [plusC (l r) (+ (interp l) (interp r))]
           [multC (l r) (* (interp l) (interp r))]
           [eqC (l r) (= l r)]
           [ifC (i t e)(interp i)]))

          
(define (parse [s : s-expression]) : ArithC
  (cond
    [(s-exp-number? s) (numC (s-exp->number s))]
    [(s-exp-list? s)
     (let ([sl (s-exp->list s)])
       (case (s-exp->symbol (first sl))
         [(+) (plusC (parse (second sl)) (parse (third sl)))]
         [(*) (multC (parse (second sl)) (parse (third sl)))]
         [(=) (eqC (parse (second sl)) (parse (third sl)))]
         [(if)(ifC (parse (second sl)) (parse (third sl)) (parse (fourth sl)))]
         [else (error 'parse "invalid list input")]))]
    [else (error 'parse "invalid input")]))           
               



(test (interp (parse '(if 0 1 2))) 2)
(test (interp (parse '(if 1 2 3))) 2)
(test (interp (parse '(if (= 1 2) 1 2))) 2)





(test(interp (eqC (numC 5) (numC 5)))1)
(test (interp (ifC (numC 0) (numC 1)(numC 2)))2)
(test (interp (ifC (numC 1) (numC 2) (numC 3)))2)




