(ns system-utils.log
  (:require [edamame.core :as edn]
            [integrant.core :as ig]
            [jsonista.core :as json]
            [system-utils.correlation :refer [*correlation-id*]]
            [taoensso.timbre :as timbre]))

(defn ^:private correlation-middleware [data]
  (update data :correlation-id #(or % *correlation-id*)))

(defn ^:private json-output [{:keys [level instant correlation-id
                                     msg_ hostname_
                                     ?ns-str ?file ?line] :as input}]
  (try
    (let [msg (force msg_)
          event (edn/parse-string msg)
          event' (if (map? event) event {:msg msg})
          location (str (or ?ns-str ?file "?") ":" (or ?line "?"))]
      (json/write-value-as-string
       (cond-> {:timestamp instant
                :level level
                :event event'
                :hostname (force hostname_)
                :location (str (or ?ns-str ?file "?") ":" (or ?line "?"))}
         correlation-id
         (assoc :correlation-id correlation-id))))
    (catch Throwable ex
      (clojure.pprint/pprint ex))))

(defmethod ig/init-key ::config [_ opts]
  (condp = opts
    :DEFAULT (do (timbre/set-config! timbre/example-config)
                 (timbre/merge-config! {:output-fn json-output
                                        :middleware [correlation-middleware]}))))
