(ns jakub-stastny.et.parser
  "...."
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(defn parse-var [line]
(str/trim (str/replace line #"^\s*#\+\w+:?(.*)$" "$1")))

(defn babel-to-map [string]
    (edn/read-string (str "{" string "}")))

(defn parse-examples [lines]
  (let [update-last-task
        (fn [tasks update-fn]
          (conj
           (vec (butlast tasks))
           (conj (last tasks) (update-fn (last tasks)))))]

    (first
     (reduce
      (fn [[tasks status] line]
        ;; (prn {:t tasks :s status :l line}) ; --------------
        (cond
          ;; Read name.
          (re-find #"^\s*#\+(?i)name:" line)
          [(conj tasks {:name (parse-var line)}) :named]

          ;; Read block options.
          (and (re-find #"^\s*#\+(?i)begin_src" line)
               (= status :named))
          [(update-last-task
            tasks
            (fn [task]
              (let [raw-opts (str/split (parse-var line) #"\s+")
                    lang (first raw-opts)
                    opts (babel-to-map (str/join " " (rest raw-opts)))]
                {:lines [] :lang lang :opts opts})))
           :reading]

          ;; Stop reading block body.
          ;; We do need this line even though it does the same
          ;; as the default cond so the end_src line gets skipped.
          (re-find #"^\s*#\+(?i)end_src" line)
          [tasks nil]

          ;; Read body.
          (= status :reading)
          [(update-last-task
            tasks
            (fn [task] {:lines (vec (conj (:lines task) line))}))
           :reading]

          :default [tasks nil]))
      [[] nil]
      lines))))
