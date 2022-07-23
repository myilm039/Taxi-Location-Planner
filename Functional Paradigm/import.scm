#lang scheme
(define (readlist filename)
 (call-with-input-file filename
  (lambda (in)
    (read in))))

(define (import)
  (let ((p65 (readlist "partition65.scm"))
        (p74 (readlist "partition74.scm")) 
        (p75 (readlist "partition75.scm"))
        (p76 (readlist "partition76.scm"))
        (p84 (readlist "partition84.scm"))
        (p85 (readlist "partition85.scm"))
        (p86 (readlist "partition86.scm")))
    (append p65 p74 p75 p76 p84 p85 p86)))

; Takes a list L as input, and writes the list in a text file, where the file name is defined by the parameter filename
(define (saveList filename L)
(call-with-output-file filename
(lambda (out)
(write L out))))

; Takes as input a list of clusters to be merged
; Returns the list of merged clusters
(define (mergeClusters list1)
  (merge-all (unique-clusters list1 empty) list1))

; If the current cluster label is a member of I, replaces the cluster label in list1 with newLabel and returns the result 
(define (replace I newLabel list1)
  (cond[(not (contains? I (first(reverse list1)))) (append (list(first list1)) (rest list1))]
       [else (append (reverse(rest (reverse list1))) (list newLabel))])
  )

; For all members of list1, apply the replace function and return the resulting list.
(define (relabel newLabel I list1 resultList)
  (cond [(empty? list1) (append (list(first resultList)) (rest resultList)) ]
  [else (relabel newLabel I (rest list1) (append resultList (list (replace I newLabel(first list1))) ))]
  ))

; From the list of points in the parameter list1, finds the list of cluster labels. The produced list is unique.
(define (unique-clusters list1 resultList)
  (cond[(empty? list1) (remove-duplicates (append (list(first resultList)) (rest resultList)))]
       [else (unique-clusters (rest list1) (append resultList (list (first (reverse (first list1))))))]))

; From the list of points in the parameter list1, generates the list of points that have a specific label. Returns the result under resultList.
(define (generate-clusters label list1 resultList)
  (cond[(empty? list1) (append (list(first resultList)) (rest resultList))]
       [(= label (first (reverse (first list1)))) (generate-clusters label (rest list1)(append resultList (list (first list1))))]
       [else (generate-clusters label (rest list1) resultList) ]))

; Deletes all occurrences of elements in I from list1 and returns the resulting list.
(define (delete-specific-elements I list1 resultList)
  (cond[(and(empty? resultList) (empty? list1)) empty]
       [(empty? list1) (append (list(first resultList)) (rest resultList))]
       [(contains? I (first list1)) (delete-specific-elements I (rest list1) resultList)]
       [else  (delete-specific-elements I (rest list1) (append resultList (list(first list1))))]))

; Takes a point (list1), generates the LABELS of the clusters which contain this point.
; EXCEPT the LABEL of the POINT in the parameters.
(define (generate-labels list1 globalList resultList)
  (cond[(empty? globalList) (delete-specific-elements (list (first(reverse list1))) (append (list(first resultList)) (rest resultList)) empty)]
       [(=(first(rest(first globalList))) (first(rest list1))) (generate-labels list1 (rest globalList) (append resultList (list (first(reverse(first globalList))))))]
       [else (generate-labels list1 (rest globalList) resultList) ])
  )

; Returns true if list1 contains elem
(define (contains? list1 elem)
  (cond [(empty? list1) #f ]
        [(= elem (first list1))]
        [else (contains? (rest list1) elem)]))

; Given a cluster, finds the list of the labels of intersecting clusters with the one in the parameters.
(define (intersect list1 resultList)
  (cond[(and (empty? list1) (empty? resultList)) empty]
       [(empty? list1) (remove-duplicates(flatten(append (list(first resultList)) (rest resultList))))]
       [else (intersect (rest list1) (append resultList (generate-labels (first list1) (import) empty) ))] ) )

  
; For all cluster labels in uniqueLabels, apply relabel to list1 and return the list
(define (merge-all uniqueLabels list1)
  (cond[(empty? uniqueLabels) (sort-output(remove-partition(append (list(first list1)) (rest list1)) empty))]
       [else (merge-all (delete-specific-elements (intersect (generate-clusters (first uniqueLabels) (import) empty) empty)(rest uniqueLabels) empty)
                        (relabel (first uniqueLabels) (intersect (generate-clusters (first uniqueLabels) (import) empty) empty) list1 empty  ))])
  )

; From a list of points, removes the partition information since it is not useful when merging the clusters.
(define (remove-partition list1 resultList)
  (cond[(empty? list1) (remove-duplicates(append (list(first resultList)) (rest resultList)))]
       [else (remove-partition (rest list1) (append resultList (list(rest(first list1)))))]))

; Takes a list as input (list1) returns the sorted list. For sorting criterion, uses the function sorting-standard?.
(define (sort-output list1)
  (sort list1 sorting-standard?))

; The criterion for sorting. For the final output, we want our clusters to be sorted by point ID. From smallest to largest.
(define (sorting-standard? x y)
    (<= (first x) (first y)))




  
  






  

