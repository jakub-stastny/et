#!/usr/bin/env bb

;; Run: bb -cp "$(clojure -Spath)" run-tests.bb

(require '[clojure.test :refer :all]
         '[integration-test :refer :all])

(run-tests 'integration-test)
