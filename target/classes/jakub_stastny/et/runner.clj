(ns jakub-stastny.et.runner
  "........."
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.java.shell]
            [jakub-stastny.et.org :as org]))

(defn abort [message]
  (throw (ex-info
          (str \u001b "[31m" "Error: " \u001b "[0m" message)
          {:babashka/exit 1})))

(defn built-in-task--tasks
  ""
  [& args]
  (prn args)
  )

(def built-in-tasks
  {:tasks built-in-task--tasks
   :help #(println "TODO: Implement built-in task :help")})

(defn built-in-task [i]
  (cond
    (some #(= i %) ["-T" "--tasks"])
    {:name :tasks :fn (built-in-tasks :tasks)}

    true
    {:name :help :fn (built-in-tasks :help)}))

  ;; (defn show-task-in-ns [namespace]
  ;;   (prn (map (fn [t] {(t :name) ((t :opts) :doc)}) (filter-tasks namespace)))
  ;;   (println "\nIf you're not seeing some tasks, make sure they have :task yes in their begin_src options."))

  ;; (defn show-tasks-in-ns [namespaces]
  ;;   (doseq [namespace namespaces] (show-task-in-ns namespace)))
  ;; (defn show-all []
  ;;   (println "Show all ..."))

(defn args-to-tasks [args]
  (reduce
   (fn [acc i]
     (cond
       ;; First argument starting with a dash (-T, -h etc).
       (and (empty? acc) (re-matches #"-{1,2}\w+" i))
       (conj acc {:task (built-in-task i)})

       (and
        (re-find #"\.org$" i)
        ;; -T is followed by an org file, not a task name.
        (not (= (get-in (last acc) [:task :name]) :tasks)))
       (conj acc {:path i :args [] :task {}})

       (and
        (nil? ((last acc) :task-name))
        ;; -T is followed by an org file, not a task name.
        (not (= (get-in (last acc) [:task :name]) :tasks)))
       (conj (butlast acc)
             (assoc-in (last acc) [:task-name] i))

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
   :exts {:clojure "clj" :emacs-lisp "el"}
   :cmds {:clojure "clojure -M"} ; -M -m to run -main OR bb
   :exec-fn clojure.java.shell/sh})

(defn run-task [task config]
  (println (str "~ Task definition " (pr-str task)))
  (cond
    ;; Built-in task.
    (get-in task [:task :fn])
    ((get-in task [:task :fn]))

    ;; Org-defined task.
    (and (task :path) (task :task-name))
    (org/run-task task config)

    ;; Should never get here.
    true
    (abort "Nope")))

;; TODO: spec: validate keys of config.
(defn run
  "....."
  ([args] (run {} args))

  ([custom-config args]
   (let [config (conj default-config custom-config)]
     (let [defs
           (try ((config :args-to-tasks) args)
                (catch Exception e
                  (println "E")))]
       (doseq [def defs] (run-task def config))))))

; Main entry point if the default runner is used.
(defn -main [& args] (run args))
