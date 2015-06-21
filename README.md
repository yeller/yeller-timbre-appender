# yeller-timbre-appender

A Clojure library that integrates [yellerapp.com](http://yellerapp.com) with the Timbre logging library.

## Installation

Leiningen:
```
[yeller-timbre-appender "0.4.1"]
```

## Usage

#### Setup

First initialize the Yeller timbre appender:

```clojure
(require '[yeller.timbre-appender])
(timbre/merge-config!
  {:appenders
    {:yeller-appender
      (yeller.timbre-appender/make-yeller-appender
        {:token "YOUR TOKEN HERE" :environment "production"})}})
```

Note that Yeller doesn't record errors sent from the `test` or `development`
environments.

Also, if you have an existing Yeller client, you can pass it in with
`:yeller/client` instead of needing a token. This is useful if you have an
existing yeller client instance and don't want two instances of it:

```clojure
(require '[yeller.timbre-appender]
         '[yeller.clojure.client])
(def client (yeller.clojure.client/client {:token "YOUR TOKEN HERE" :environment "production"})
(timbre/merge-config!
  {:appenders
    {:yeller-appender
        (yeller-timbre-appender/make-yeller-appender
            {:yeller/client client})}})
```

Once you have the yeller timbre appender configured, record an exception using
timbre's usual logging mechanisms:

```clojure
;; recording an exception runs via timbre as usual
(require '[taoensso.timbre :as timbre])
(timbre/error (ex-info "woops" {:some :useful-data}))
```

The Yeller appender records errors sent at the `error` and `fatal` levels by
default. If you have an `ex-info` style error, it extracts the data given and
sends it to Yeller.

### Extra information, url, location and more

To attach extra information, like the url and so on, you can pass a map as the second argument to timbre's `error` call:

```clojure
(require '[taoensso.timbre :as timbre])
(timbre/error (ex-info "woops" {:some :useful-data}) {:custom-data {:params {:user-id 1}} :url "http://example.com"})
```

For example, to use the timbre error in a ring middleware:

```clojure
(require '[taoensso.timbre :as timbre])

(defn wrap-error-handling [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t {:url (:uri req)})))))
```

The map argument takes the same set of keys the yeller clojure client takes as its second argument. See the docstring on `yeller.clojure.client/report`:

```clojure
(doc yeller.clojure.client/report)
```


## License

Copyright © 2014-2015 Tom Crayford

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
