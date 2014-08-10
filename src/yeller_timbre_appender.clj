(ns yeller-timbre-appender
  (:require [yeller-clojure-client :as yeller-client]))

(defn create-client [options]
  (if-let [client (:yeller/client options)]
    client
    (yeller-client/client options)))

(defn extract-data [throwable]
  (if-let [data (ex-data throwable)]
    {:ex-data data}
    {}))

(defn make-yeller-appender
  "Create a Yeller timbre appender.
   (make-yeller-appender {:token \"YOUR API TOKEN HERE\"})
   Required options:
     either:
     :token \"your api token here\"
     OR
     :yeller/client an-existing-yeller-client
   The second option is for when you already use yeller somewhere else and
   only want one client in the codebase.
   Optional:
     :environment \"production\" ; the name of the environment the app runs in
  (note that exceptions reported in \"development\" or \"test\" environments
  are ignored) (defaults to \"production\"
  Optionally takes an additional map (as a first argument) which gets merged as timbre options:
  (make-yeller-appender
    {:min-level :error, :enabled? true}
    {:token \"your token here\"})"
  ([options] (make-yeller-appender {} options))
  ([timbre-options options]
   (assert (or (string? (:token options))
               (:yeller/client options)) "make-yeller-appender requires a :token or a :yeller/client")
   (let [with-default (merge {:environment "production"} options)
         client (create-client with-default)]
     (merge
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
                     :custom-data custom-data}))))}
       timbre-options))))

(comment
  ;; for repl testing
  (do (require '[yeller-timbre-appender :reload true]) (timbre/set-config! [:appenders :yeller] (yeller-timbre-appender/make-yeller-appender {:token "YOUR TOKEN HERE" :environment "timebre-test"})) (timbre/error (ex-info "lol" {:foo 1}))))
