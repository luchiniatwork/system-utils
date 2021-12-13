(ns system-utils.env
  (:require [ambiente.core :as ambiente]
            [edamame.core :as edn]
            [integrant.core :as ig]
            [taoensso.timbre :refer [debug info warn error fatal report]]))

(defmethod ig/init-key ::var [_ {:keys [var]}]
  (info {:msg "Initializing Environment Variable"
         :var var})
  (ambiente/env var))

(defmethod ig/init-key ::edn-parsed-var [_ {:keys [var]}]
  (info {:msg "Initializing EDN-parsed Environment Variable"
         :var var})
  (-> var
      ambiente/env
      edn/parse-string))
