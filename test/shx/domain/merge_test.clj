(ns shx.domain.merge-test
  (:require [clojure.test :refer [deftest is testing]]
            [shx.domain.merge :as merge]))

(deftest merge-frag-test
  (testing "maps merge keywise, later wins"
    (is (= {:vars {:A "1" :B "2"}}
           (merge/merge-frag {:vars {:A "0" :B "2"}} {:vars {:A "1"}}))))
  (testing "vectors concatenate in order"
    (is (= {:paths-prepend ["a" "b" "c"]}
           (merge/merge-frag {:paths-prepend ["a" "b"]} {:paths-prepend ["c"]}))))
  (testing "scalars: later wins"
    (is (= {:x 2} (merge/merge-frag {:x 1} {:x 2}))))
  ;; The "anything else: later wins" rule from the ns docstring. A key whose
  ;; shape differs between two fragments is ordinary hand-edited-EDN breakage,
  ;; and without these the `and` guards above could each be an `or` — which
  ;; reaches (merge {:A "1"} ["x"]) and throws while rendering a login shell.
  (testing "type collision: later wins, whichever side the collection is on"
    (is (= {:vars ["x"]}
           (merge/merge-frag {:vars {:A "1"}} {:vars ["x"]})))
    (is (= {:vars {:A "1"}}
           (merge/merge-frag {:vars ["x"]} {:vars {:A "1"}})))
    (is (= {:paths-prepend "b"}
           (merge/merge-frag {:paths-prepend ["a"]} {:paths-prepend "b"})))))

(deftest fold-fragment-tree-test
  (testing "grandchild values bubble to the top"
    (let [tree {:content {:vars {:TOP "top"}}
                :children [{:content {:vars {:CHILD "mid"}}
                            :children [{:content {:vars {:GRANDCHILD "deep"}}
                                        :children []}]}]}]
      (is (= {:vars {:TOP "top" :CHILD "mid" :GRANDCHILD "deep"}}
             (merge/fold-fragment-tree tree)))))
  (testing "closer-to-root wins over deeper on conflict"
    (let [tree {:content {:vars {:X "root"}}
                :children [{:content {:vars {:X "child"}} :children []}]}]
      (is (= {:vars {:X "root"}}
             (merge/fold-fragment-tree tree))))))

(deftest apply-host-overrides-test
  (testing "host entry merges on top and :hosts is stripped"
    (is (= {:vars {:A "override"}}
           (merge/apply-host-overrides
            {:vars {:A "base"} :hosts {"h1" {:vars {:A "override"}}}}
            "h1"))))
  (testing "unknown host returns config without :hosts"
    (is (= {:vars {:A "base"}}
           (merge/apply-host-overrides
            {:vars {:A "base"} :hosts {"h1" {:vars {:A "x"}}}}
            "other")))))
