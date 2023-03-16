(ns jakub-stastny.et.config
  (:require [jakub-stastny.et.built-ins :as built-ins]))

(defn args-to-tasks [args]
  (reduce
   (fn [acc i]
     (cond
       ;; First argument starting with a dash (-T, -h etc).
       (and (empty? acc) (re-matches #"-{1,2}\w+" i))
       (conj acc (built-ins/get-task i))

       ;; Path name as the first argument (i. e. README.org).
       (and
        (re-find #"\.org$" i)
        (nil? (last acc)))
       (conj acc {:path i :args []})

       ;; -T README.org
       (and
        (last acc)
        (nil? ((last acc) :task-name))
        (not (= (get-in (last acc) [:task :task-name]) :tasks)))
       (conj (butlast acc)
             (assoc-in (last acc) [:task-name] i))

       ;; Add to args.
       true
       (conj (butlast acc)
             (update-in (last acc) [:args] #(conj % i)))))
   []
   args))

(defn task? ; TODO: Use it.
  "..."
  [example]
  (or (example :task) (example :doc)))

(def default-config
  {:args-to-tasks args-to-tasks
   :task? task?
   :exts {:clojure "clj" :emacs-lisp "el"}
   :cmds {:clojure "clojure -M"}        ; -M -m to run -main OR bb
   :exec-fn clojure.java.shell/sh}) ; Or bb.shell https://github.com/babashka/process
