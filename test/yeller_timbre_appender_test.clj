(ns yeller-timbre-appender-test
  (:require [clojure.test :refer :all]
            [yeller.timbre-appender :refer :all]))

(deftest extract-data-grabs-ex-data
  (testing "it grabs ex-data if it's there"
    (is (= {:custom-data {:ex-data {:some-useful-data "lol"}}}
          (extract-data (ex-info "lol" {:some-useful-data "lol"}) {}))))

  (testing "it grabs :custom-data if it's in the arg keys"
    (is (= {:custom-data {:params {:user-id 1}}}
           (extract-data (Exception. "lol")
                         [{:custom-data {:params {:user-id 1}}}]))))

  (testing "it merges arg data and ex-data"
    (is (= {:custom-data {:params {:user-id 1} :ex-data {:some-useful-data "lol"}}}
           (extract-data (ex-info "lol" {:some-useful-data "lol"})
                         [{:custom-data {:params {:user-id 1}}}])))))
