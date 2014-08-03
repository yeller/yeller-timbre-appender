(ns yeller-timbre-appender
  (:require [yeller-clojure-client :as yeller-client]))

(defn extract-data [throwable]
  (if-let [data (ex-data throwable)]
    {:ex-data data}
    {}))

(defn make-yeller-appender
  "Create a Yeller timbre appender.
   Required options:
     :token \"your api token here\"
   Optional:
     :environment \"production\" ; the name of the environment the app runs in
  (note that exceptions reported in \"development\" or \"test\" environments
  are ignored) (defaults to \"production\""
  [options]
  (assert (string? (:token options)) "make-yeller-appender requires a token")
  (let [with-default (merge {:environment "production"} options)
        client (yeller-client/client options)]

    {:doc "A timbre appender that sends errors to yellerapp.com"
     :min-level :warn
     :enabled? true
     :async? true
     :fn (fn [args]
           (let [throwable (:throwable args)
                 custom-data (extract-data throwable)]
             (if (and (:error? args)
                      throwable)
               (yeller-client/report
                 client
                 throwable
                 {:environment (:environment with-default "production")
                  :location (:ns args)
                  :custom-data custom-data}))))}))

(comment
  ;; for repl testing
  (do (require '[yeller-timbre-appender :reload true]) (timbre/set-config! [:appenders :yeller] (yeller-timbre-appender/make-yeller-appender {:token "YOUR TOKEN HERE" :environment "timebre-test"})) (timbre/error (ex-info "lol" {:foo 1}))))
