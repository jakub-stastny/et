(ns jakub-stastny.et.runner
  "Main namespace. Provides the default runner as well as the `run`
  fn for creating custom runners with their own custom config."
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.java.shell]
            [jakub-stastny.et.utils :refer :all]
            [jakub-stastny.et.config :as config]
            [jakub-stastny.et.built-ins :as built-ins]
            [jakub-stastny.et.org :as org]))

(defmulti exec-task
  "Docstring"
  (fn [task _] (task-type task)))

(defmethod exec-task :built-in [task _]
  ((task :fn) (task :args)))

(defmethod exec-task :custom [task config]
  (org/run-task task config))

(defmethod exec-task :error [task _]
  ;; (throw (ex-info "Don't know how to handle task" {:task task}))
  ((built-ins/get-task :help) :fn))

;; Wrapper
(defn run-task
  "...."
  [task config]
  (info "Running " (pr-task task) ".")
  (exec-task task config))

(defn handle-att-error [error]
  (println (colour :red "Error in args-to-task:")
           "caused the following error:")
  (println error)
  (System/exit 1))

(defn handle-user-error
  "Abort on user error, re-throw otherwise."
  [error]
  ;(prn (ex-cause error))
  (let [info (:or (ex-data error) {})]
    (if (= (info :type) :user-error)
      (abort (str (ex-message error) "."))
      (rethrow error))))

;; ; This is how you wrap exceptions:
;; (def err (try (/ 1 0) (catch Exception e e)))
;; (def rer (try (throw (ex-info "rethrown" {:rethrown true} err)) (catch Exception e e)))
;; (try
;;   (try (+ 1 nil) (catch Exception e (throw (ex-info "Rethrown" {:rethrown true} e))))
;;   (catch Exception e
;;     (prn e)
;;     ;; (prn (ex-cause e))
;;     ))
;; (System/exit 1)

(defmacro abort-on-error
  [handler body]
  `(try ~body (catch Exception e# (~handler e#))))

(defn run
  "Main entry fn for a custom runner.
   Takes command-line `custom-config` that it merges
   into the default one and command-line `args`."
  ([args] (run {} args))

  ([custom-config args]
   (abort-on-error
    handle-user-error
    (let [config (conj config/default-config custom-config)]
      (let [defs (abort-on-error handle-att-error ((config :args-to-tasks) args))]
        (doseq [def defs] (run-task def config)))))))

(defn -main
  "This is the main entry for the default runner.
   Write a custom runner if you want to customise the config."
  [& args] (run args))
