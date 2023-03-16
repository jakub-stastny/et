(ns jakub-stastny.et.built-ins
  "Built-in tasks."
  (:require [jakub-stastny.et.org :as org]
            [jakub-stastny.et.utils :refer :all]))

(defn tasks
  "Built-in task to show all tasks in given paths."
  [paths]
  (puts "\n" (colour :blue :bold "Available tasks"))
  (doseq [path paths]
    (let [tasks (filter (fn [task] (get-in task [:opts :doc])) (org/load-tasks path))]
      (when-not (empty? tasks)
        (puts "\nNamespace " (colour :cyan path) ":")
        (doseq [{:keys [task-name opts]} tasks]
          (puts (colour :yellow task-name)
                (apply str (repeat (- 12 (count task-name)) " "))
                (opts :doc) "."))))))

(defn help
  "Built-in task to show help.
   Unlike other tasks can take 0 arguments."
  [& args]
  (puts "\n" (colour :green :bold "Help:"))
  (puts "-T|--tasks task-file.org           # Show documented tasks in given task files")
  (puts "task-file.org task-name arg1 arg2  # Run task-name from task-file.org with arguments arg1 arg2"))

(def ^:private built-in-tasks
  {:tasks tasks :help help})

(defn- make-built-in-task
  "Wrap fn as a task." [name]
  {:task-name name :fn (built-in-tasks name)})

(defn get-task
  "Get a built-in task. Defaults to the help task."
  [arg] (cond
          (some #(= arg %) ["-T" "--tasks" :tasks])
          (make-built-in-task :tasks)

          (some #(= arg %) ["-h" "-H" "--help" :help])
          (make-built-in-task :help)))
