(ns integration-test
  (:require [clojure.test :refer :all]
            [babashka.process :as process]
            [clojure.java.shell :refer [sh]]))

(defn run-bb [args]
  (process/shell "bb" args))

(defn run-java [args]
  (sh "java" "-jar" args))

(defn assert-exit-status [result expected-status]
  (is (= (:exit result) expected-status)))

(defn assert-output-matches [result expected-regex]
  (is (re-find (re-pattern expected-regex) (:out result))))

(deftest test-works
  (println "Hey there!"))

;; (deftest test-babashka-execution
;;   (let [result (run-bb "your-program.clj")]
;;     (assert-exit-status result 0)
;;     (assert-output-matches result "hello")))

;; (deftest test-java-execution
;;   (let [result (run-java "your-program.jar")]
;;     (assert-exit-status result 0)
;;     (assert-output-matches result "hello")))
