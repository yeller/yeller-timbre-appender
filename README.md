# yeller-timbre-appender

A Clojure library that integrates yellerapp.com with the Timbre logging library.

## Installation

Leiningen:
```
[yeller-timbre-appender "0.1.0-SNAPSHOT"]
```

## Usage

#### Setup

First initialize the Yeller timbre appender:

```clojure
(require '[yeller-timbre-appender :reload true])
(timbre/set-config! [:appenders :yeller]
  (yeller-timbre-appender/make-yeller-appender
  {:token "YOUR TOKEN HERE" :environment "production"}))
```

The record an exception using timbre's usual logging mechanisms:

```clojure
;; recording an exception runs via timbre as usual
(require '[taoensso.timbre :as timbre])
(timbre/error (ex-info "woops" {:some :useful-data}))
```

The Yeller appender records errors sent at the `error` and `fatal` levels. If
you have an `ex-info` style error, it extracts the data given and sends it to
Yeller.


## License

Copyright © 2014 Tom Crayford

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
