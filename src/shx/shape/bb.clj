(ns shx.shape.bb
  "IR -> babashka/Clojure code emitter. Pure: data in, string out.

  Shell-state effects target a small shx.core runtime API (set-env!,
  path-prepend!, defalias, source-bash). Process effects target
  babashka.process directly."
  (:require [clojure.string :as str]
            [shx.law.ir :as law]
            [shx.shape.quote :as q]))

(defmulti emit
  "Emit one IR node as Clojure source text, or nil if unsupported."
  {:arglists '([node])}
  (fn [node] (when (vector? node) (first node))))

(defmethod emit :export [[_ {:keys [name value]}]]
  (str "(shx.core/set-env! " (pr-str name) " " (pr-str value) ")"))

(defmethod emit :path/prepend [[_ dir & [{guard :if}]]]
  (str "(when " (if (= guard :dir-exists)
                  (str "(.isDirectory (io/file " (pr-str dir) "))")
                  "true")
       " (shx.core/path-prepend! " (pr-str dir) "))"))

(defmethod emit :path/append [[_ dir & [{guard :if}]]]
  (str "(when " (if (= guard :dir-exists)
                  (str "(.isDirectory (io/file " (pr-str dir) "))")
                  "true")
       " (shx.core/path-append! " (pr-str dir) "))"))

(defmethod emit :alias [[_ {:keys [name argv]}]]
  (str "(shx.core/defalias " (pr-str name) " [" (q/argv->bb argv) "])"))

(defmethod emit :source [[_ file]]
  (str "(shx.core/source-bash " (pr-str file) ")"))

(defmethod emit :exec [[_ {:keys [argv dir env]}]]
  (str "(p/shell"
       (when (or dir env)
         (str " {:continue true"
              (when dir (str " :dir " (pr-str dir)))
              (when env (str " :extra-env "
                             (pr-str (into {} (map (fn [[k v]] [(name k) v]) env)))))
              "}"))
       " " (q/argv->bb argv) ")"))

(defmethod emit :pipe [[_ & stages]]
  (str "(p/pipeline "
       (str/join " " (map #(str/replace (emit %) #"^\(p/shell" "(p/process") stages))
       ")"))

(defmethod emit :capture [[_ inner]]
  (str "(:out (p/shell {:out :string :continue true} "
       (q/argv->bb (:argv (second inner))) "))"))

(defmethod emit :set [[_ {:keys [name value]}]]
  (str "(def " name " " (if (vector? value) (emit value) (pr-str value)) ")"))

(defmethod emit :if [[_ test then else]]
  (str "(if " (emit test) " " (emit then)
       (when else (str " " (emit else))) ")"))

(defmethod emit :test [[_ & args]]
  (if (= "-d" (first args))
    (str "(.isDirectory (io/file " (pr-str (second args)) "))")
    (str "(shx.core/test " (str/join " " (map pr-str args)) ")")))

(defmethod emit :raw [[_ {:keys [lang text]}]]
  (if (= lang "clojure")
    text
    (str "(p/shell " (pr-str lang) " \"-c\" " (pr-str text) ")")))

(defmethod emit :default [_]
  nil)

(defn emit-program
  "Emit a program as Clojure source text with the standard requires header.
  Validates against shx.law.ir/Program before emitting."
  [nodes]
  (when-let [explanation (law/explain-program nodes)]
    (throw (ex-info "invalid IR program"
                    {:type :shx/invalid-program
                     :explanation explanation})))
  (str "(require '[babashka.process :as p] '[clojure.java.io :as io])\n"
       (str/join "\n" (map #(or (emit %)
                                (str "# bb emit not implemented for " (first %)))
                           nodes))))
