(ns system-utils.version
  (:require [anomalies.core :as anom]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [integrant.core :as ig]
            [orzo.core :as orzo]
            [orzo.git :as git]
            [taoensso.timbre :refer [debug info warn error fatal report]]))

(defn gen []
  (-> (orzo/calver "YY.0M.0D")
      (orzo/append (str "-" (git/sha)))
      (orzo/append (git/unclean-status "-UNCLEAN"))))


(defn gen-and-persist! []
  (-> (gen)
      (orzo/save-file "resources/version.txt")))


(defn load-version
  ([]
   (load-version nil))
  ([{:keys [throw?] :as opts}]
   (let [version (some-> "version.txt"
                         io/resource
                         slurp)]
     (if (and throw? (nil? version))
       (anom/throw-anom (->> ["Unavailable version!"
                              "Make sure it was generated and is in"
                              "`resources/version.txt`"](s/join " "))
                        {::anom/category ::anom/not-found})
       (or version "<UNAVAILABLE>")))))


(defmethod ig/init-key ::dynamic-gen [_ {:keys [environment-id]}]
  (info {:msg "Initializing Dynamic Gen Versioner"
         :environment-id environment-id})
  {:version (gen)
   :environment-id environment-id})


(defmethod ig/init-key ::read-from-disk [_ {:keys [environment-id]}]
  (info {:msg "Initializing Production Version"})
  {:version (load-version {:throw? true})
   :environment-id environment-id})
