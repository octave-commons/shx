(ns shx.infra.config-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [shx.infra.config :as cfg])
  (:import [java.nio.file Files]))

(defn- temp-dir []
  (str (Files/createTempDirectory "shx-test" (into-array java.nio.file.attribute.FileAttribute []))))

(defn- write-file [dir name content]
  (let [f (io/file dir name)]
    (io/make-parents f)
    (spit f content)
    (.getPath f)))

(deftest read-fragment-tree-bubbling-test
  (let [dir (temp-dir)
        _ (write-file dir "grandchild.edn" "{:vars {:GRANDCHILD \"deep\"}}")
        _ (write-file dir "child.edn"
                      (str "{:merge [\"" dir "/grandchild.edn\"] :vars {:CHILD \"mid\"}}"))
        top (write-file dir "env.edn"
                        (str "{:merge [\"" dir "/child.edn\"] :vars {:TOP \"top\"}}"))
        tree (cfg/read-fragment-tree top)]
    (testing "nested fragments form a tree with content and children"
      (is (= {:TOP "top"} (get-in tree [:content :vars])))
      (is (= {:CHILD "mid"} (get-in tree [:children 0 :content :vars])))
      (is (= {:GRANDCHILD "deep"}
             (get-in tree [:children 0 :children 0 :content :vars]))))))

(deftest read-fragment-tree-cycle-test
  (let [dir (temp-dir)
        _ (write-file dir "a.edn" (str "{:merge [\"" dir "/b.edn\"] :vars {:A \"a\"}}"))
        path-b (write-file dir "b.edn" (str "{:merge [\"" dir "/a.edn\"] :vars {:B \"b\"}}"))
        tree (cfg/read-fragment-tree path-b)]
    (testing "cycle terminates; both nodes keep their content"
      (is (= {:B "b"} (get-in tree [:content :vars])))
      (is (= {:A "a"} (get-in tree [:children 0 :content :vars])))
      (is (= {} (get-in tree [:children 0 :children 0 :content]))))))

(deftest read-fragment-tree-invalid-test
  (let [dir (temp-dir)
        bad (write-file dir "bad.edn" "{:vars {:A 42}}")
        tree (cfg/read-fragment-tree bad)]
    (testing "schema violation (non-string var value) yields empty content"
      (is (= {:content {} :children []} tree)))))
