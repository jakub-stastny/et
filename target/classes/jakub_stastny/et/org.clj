(ns jakub-stastny.et.org
  "...."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [jakub-stastny.et.utils :refer :all]
            [clojure.java.shell :refer [sh]] ; chmod +x
            [jakub-stastny.et.parser :as parser]))

(defn load-tasks
  "..."
  [path]
  (if (.exists (io/as-file path)) ; Replace by fs/exists from bb.
    (parser/parse-examples (str/split (slurp path) #"\n"))
    (throw-user-error (str "File " path " doesn't exist"))))

(defn write-script-2
  "...."
  [script-name command lines task-file-path]
  (let [shebang (str "#!/usr/bin/env " command)
        indent (re-find #"^ *" (first lines))]
    (spit script-name
          (str/join "\n"
                    (apply conj
                           [shebang ""]
                           (map
                            (fn [line] (-> line
                                           (str/replace indent "")
                                           (str/replace #"\{\{\s*[CP]WD\s*\}\}" (System/getenv "PWD"))
                                           (str/replace #"\{\{\s*FILE\s*\}\}" task-file-path)))
                            (conj lines "")))))
    (sh "chmod" "+x" script-name)))

(defn get-ext
  "..."
  [exts lang] (or (get exts (keyword lang)) lang))

(defn get-cmd
  "..."
  [cmds lang] (or (get cmds (keyword lang)) lang))

; TODO: don't rewrite if same.
(defn write-script
  "....."
  [{:keys [task-name lang lines opts] :as task} {:keys [exts cmds]}]
  (let [shebang (if (opts :shebang)
                  (str/split (str/replace (opts :shebang) #"#!" "") #"\s+")
                  (str/split (get-cmd cmds lang) #"\s+"))
        script-path (str "/tmp/" task-name "." (get-ext exts lang))]
    (write-script-2 script-path (str/join " " shebang) lines "ReplaceMe.org")
    (conj shebang script-path)))

(defn exec-task
  "..."
  [task config]
  (let [exec-fn (config :exec-fn)
        base-command (write-script task config)
        command (apply conj base-command (task :args))]
    ;; With exec it'll never run so no prob.
    ;; (prn command) (prn res) (puts)
    (info "Running " (colour :green "$ ") (colour :purple (str/join " " command)) "\n")
    (let [res (apply exec-fn command)]
      (if (= (res :exit) 0)
        (puts (str/trim (res :out)))
        (puts (str/trim (res :err))))
      (System/exit (res :exit)))))

(defn run-task
  "..."
  [task-def config]
  (let [examples (load-tasks (task-def :path))
        tasks (filter #(= ((% :opts) :task) (symbol "yes")) examples)
        task (first (filter #(= (task-def :task-name) (% :task-name)) tasks))]
    (if task
      (exec-task (conj task-def task) config)
      (puts "No such task " (task-def :task-name) "\n\nAvailable tasks: " (pr-str tasks)))))
;; (abort (str "No such task: " task-name "\nTasks in this namespace: ...."))

;; (defn filter-tasks [namespace]
;;     (filter (fn [task]
;;             (= (symbol "yes") ((task :opts) :task)))
;;     (parse-examples (get-lines-or-abort namespace))))


;;    (defn run [fully-qualified-task-name]
;;      (let [namespace (first (str/split fully-qualified-task-name #"/"))
;;            task-name (last (str/split fully-qualified-task-name #"/"))
;;            lines (get-lines-or-abort namespace)
;;            tasks (filter-tasks namespace)]
;;        (exec tasks task-name)))
;;            (run a)))
;; (defn convert-ns-to-path [namespace]
;;   (when (or (re-find #"[/:]" namespace))
;;     (throw (Exception. "Namespace uses . rather than /")))
;;   (str (str/replace namespace #"\." "/") ".org"))

;; (prn (babel-to-map ""))
;; (prn (babel-to-map ":tangle test.clj"))
;; (prn (babel-to-map ":tangle \"test.clj\""))
;; (prn (babel-to-map ":shebang \"#!/usr/bin/env clojure -M\""))
;; (prn (babel-to-map ":task yes :shebang \"#!/usr/bin/env clojure -M\""))
;; (puts)
;; (System/exit 1)
