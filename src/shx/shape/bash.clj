(ns shx.shape.bash
  "IR -> bash text emitter. Pure: data in, string out.

  Node types are validated against shx.law.ir at the program boundary
  (emit-program); individual emit methods assume valid input."
  (:require [clojure.string :as str]
            [shx.law.ir :as law]
            [shx.shape.quote :as q]))

(defmulti emit
  "Emit one IR node as bash text. Dispatches on node head keyword."
  {:arglists '([node])}
  first)

(defmethod emit :export [[_ {:keys [name value]}]]
  (str "export " name "=" (q/dq value)))

(defmethod emit :path/prepend [[_ dir & [{guard :if}]]]
  (let [line (str "PATH=" (q/dq dir) "${PATH:+:$PATH}")]
    (if (= guard :dir-exists)
      (str "[ -d " (q/dq dir) " ] && " line)
      line)))

(defmethod emit :path/append [[_ dir & [{guard :if}]]]
  (let [line (str "PATH=\"${PATH:+$PATH:}\"" (q/dq dir))]
    (if (= guard :dir-exists)
      (str "[ -d " (q/dq dir) " ] && " line)
      line)))

(defmethod emit :alias [[_ {:keys [name argv]}]]
  (str "alias " name "=" (q/sq (str/join " " argv))))

(defmethod emit :source [[_ file]]
  (str "[ -f " (q/dq file) " ] && . " (q/dq file)))

(defmethod emit :exec [[_ {:keys [argv dir env]}]]
  (if dir
    (str "(cd " (q/sq dir) " && " (q/env-prefix env) (q/argv->bash argv) ")")
    (str (q/env-prefix env) (q/argv->bash argv))))

(defmethod emit :pipe [[_ & stages]]
  (str/join " | " (map emit stages)))

(defmethod emit :capture [[_ inner]]
  (str "$(" (emit inner) ")"))

(defmethod emit :set [[_ {:keys [name value]}]]
  (str name "=" (if (vector? value)
                  (emit value)
                  (q/dq value))))

(defmethod emit :if [[_ test then else]]
  (str "if " (emit test) "; then " (emit then)
       (when else (str "; else " (emit else))) "; fi"))

(defmethod emit :test [[_ & args]]
  (str "[ " (str/join " " (map (fn [a] (if (str/starts-with? a "-") a (q/dq a))) args)) " ]"))

(defmethod emit :raw [[_ {:keys [lang text]}]]
  (if (= lang "bash")
    text
    (str "# raw " lang " block cannot be emitted as bash without translation")))

(defn emit-program
  "Emit a program (sequence of IR nodes) as bash text.
  Validates against shx.law.ir/Program before emitting; throws
  clojure.ExceptionInfo with malli explanation on invalid input."
  [nodes]
  (when-let [explanation (law/explain-program nodes)]
    (throw (ex-info "invalid IR program"
                    {:type :shx/invalid-program
                     :explanation explanation})))
  (str/join "\n" (map emit nodes)))
