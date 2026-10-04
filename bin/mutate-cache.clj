;; Sandbox cache adapter. Cache semantics and configuration belong to Heretic.
(require '[clojure.java.io :as io]
         '[heretic.core :as heretic]
         '[heretic.sandbox :as sandbox])

(let [[cmd] *command-line-args*
      _ (when-not (#{"status" "clean"} cmd)
          (throw (ex-info "Expected status or clean" {:command cmd})))
      config (heretic/load-config)
      dir (sandbox/resolve-sandbox-dir config (System/getProperty "user.dir"))
      _ (when-not (.isFile (io/file dir "heretic.edn"))
          (throw (ex-info "No retained Heretic sandbox; run bin/mutate first"
                          {:sandbox dir})))
      ;; Use the retained sandbox's sources/config for accurate staleness checks.
      ;; pr-str preserves argument boundaries without shell or code interpolation.
      code (str "(require 'heretic.core) (apply heretic.core/-main "
                (pr-str (vec *command-line-args*)) ")")
      command (sandbox/child-command config code)
      process (doto (ProcessBuilder. ^java.util.List command)
                (.directory (io/file dir))
                (.inheritIO))
      exit (.waitFor (.start process))]
  (shutdown-agents)
  (System/exit exit))
