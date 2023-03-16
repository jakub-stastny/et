(ns jakub-stastny.et.utils)

(def ^:private colours {:red 31 :green 32 :yellow 33
                        :blue 34 :purple 35 :cyan 36 :grey 37})

; https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences
(def ^:private font-effects {:normal 0 :bold 1})

(defn colour
  "Wraps text in ANSI colour escape sequence"
  ([c text]
   (colour c :normal text))

  ([c font-effect text]
   (if (= (System/getenv "TERM") "xterm-256color")
     (let [colour-code (colours c)
           font-effect-code (font-effects font-effect)]
       (str "\033[" font-effect-code ";" colour-code "m" text "\033[0m"))
     text)))

(defn abort [& chunks]
  (println (apply str (colour :red "Error: ") chunks))
  (System/exit 1))

(defn puts [& chunks]
  (println (apply str chunks)))

(defn info [& chunks]
  (apply puts (colour :white :bold "~ ") chunks))

(defn task-type [task]
  (cond
    (contains? task :fn) :built-in
    (and (contains? task :path) (contains? task :task-name)) :custom
    true :invalid))

(defmulti pr-task "Print task" task-type)

(defmethod pr-task :built-in [{:keys [task-name args]}]
  (let [base-str (str "built-in task " (colour :yellow (name task-name)))]
    (if (empty? args)
      base-str
      (str base-str " and args " (colour :cyan (str args))))))

(defmethod pr-task :custom [{:keys [task-name path args]}]
  (let [base-str (str "task " (colour :yellow (name task-name)) " from " (colour :green path))]
    (if (empty? args)
      base-str
      (str base-str " and args " (colour :cyan (str args))))))

(defmethod pr-task :invalid [{:keys [task-name] :as task}]
  (str (colour :red "invalid task " task-name ": " (pr-str task))))

(defn throw-user-error
  "..."
  [message]
  (throw (ex-info message {:type :user-error})))

(defn rethrow
  "..."
  [error]
  (throw (ex-info (str "rethrow " (ex-message error)) {:rethrown true} error)))
