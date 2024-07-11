;; This is a generated file do not edit
#!/usr/bin/env bb
(require '[clojure.java.io :as io])

(defn read-org-file [file-path]
  (slurp file-path))

(defn extract-code-blocks [content]
  (let [pattern #"(?s)#\+BEGIN_SRC\s+(\w+).*?\n(.*?)#\+END_SRC"
        matches (re-seq pattern content)]
    (map (fn [[_ lang block]] {:lang lang :block block}) matches)))

(defn write-code-blocks [file-path blocks]
  (doseq [{:keys [lang block]} blocks
          :let [output-file (str (subs file-path 0 (last-index-of file-path ".")) "_" (gensym) "." lang)]]
    (spit output-file block)
    (println (str "Tangled " lang " code block to " output-file))))

(defn tangle [file-path]
  (let [content (read-org-file file-path)
        blocks (extract-code-blocks content)]
    (write-code-blocks file-path blocks)))

(defn -main [& args]
  (if (empty? args)
    (println "Usage: clojure tangle.clj <org-file>")
    (tangle (first args))))

;; Run the script with the provided arguments
(apply -main *command-line-args*)
