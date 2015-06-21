(ns yeller.timbre-appender
  (:require [yeller-clojure-client :as yeller-client]))

(defn create-client [options]
  (if-let [client (:yeller/client options)]
    client
    (yeller-client/client options)))

(defn extract-ex-data [throwable]
  (if-let [data (ex-data throwable)]
    {:ex-data data}
    {}))

(defn extract-arg-data [raw-args]
  (if (map? (first raw-args))
    (first raw-args)
    {}))

(defn extract-data [throwable raw-args]
  (let [arg-data (extract-arg-data raw-args)
        ex-data (extract-ex-data throwable)]
    (merge
      arg-data
      {:custom-data (merge ex-data (:custom-data arg-data {}))})))

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
     :application-packages [\"com.myapp\"] : the name(s) of the root packages your
     app runs in. Defaults to reading out of project.clj (both locally and in an uberjar settings). This lets the web ui display stacktrace lines only from the app by default (and hide those from libraries and from clojure itself).
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
        :rate-limit nil
        :fn (fn [args]
              (let [throwable (:throwable args)
                    data (extract-data throwable (:args args))]
                (if (and (:error? args)
                         throwable)
                  (yeller-client/report
                    client
                    throwable
                    (merge {:environment (:environment with-default "production")
                            :location (:ns args)}
                           data)))))}
       timbre-options))))

(comment
  ;; for repl testing
  (do (require '[taoensso.timbre :as timbre]) (require '[yeller-timbre-appender :reload true]) (timbre/set-config! [:appenders :yeller] (yeller-timbre-appender/make-yeller-appender {:token "YOUR TOKEN HERE" :environment "timbre-test"})) (dotimes [_ 1] (timbre/error (ex-info "lol" {:foo 1}) {:custom-data {:params {:user-id 1}}})))
  )
