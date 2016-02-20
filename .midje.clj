(change-defaults  :fact-filter #(and (not (:spike %1))
                                    (not (:acceptance %1)))
                  :visible-future false
                 )