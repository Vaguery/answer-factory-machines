(change-defaults :fact-filter #(and (not (:spike %1))
                                    (not (:prototype %1))
                                    (not (:acceptance %1)))
                 :visible-future true
                 :print-level :print-namespaces
                 )
