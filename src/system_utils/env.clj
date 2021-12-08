(ns system-utils.env
  (:require [ambiente.core :as ambiente]
            [integrant.core :as ig]
            [taoensso.timbre :refer [debug info warn error fatal report]]))

(defmethod ig/init-key ::var [_ {:keys [var]}]
  (info {:msg "Initializing Environment Variable"
         :var var})
  (ambiente/env var))
