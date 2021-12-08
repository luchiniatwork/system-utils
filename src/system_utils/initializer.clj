(ns system-utils.initializer
  (:gen-class)
  (:require [ambiente.core :as ambiente]
            [clojure.java.io :as io]
            [edamame.core :as edn]
            [integrant.core :as ig]))

(def ^:private default-env "local")

(def ^:private readers {'ig/ref ig/ref
                        'ig/refset ig/refset})

(defn ^:private config-resource [f-name]
  (->> f-name
       (io/file "config")
       .getPath
       io/resource))

(defn ^:private read-file [f]
  (some-> f
          slurp
          (edn/parse-string {:readers readers})))

(defn read-config []
  (let [base (-> "base.edn" config-resource read-file)
        env (or (ambiente/env :env) default-env)
        config-file (str env  ".edn")
        config (-> config-file config-resource read-file)
        merged-config (merge base config)]
    (when (nil? base)
      (throw (ex-info (str "Unable to load base config: base.edn")
                      {:anomaly/category ::invalid-system-config})))
    (when (nil? config)
      (throw (ex-info (str "Unable to load config: " config-file)
                      {:anomaly/category ::invalid-system-config})))
    (when (nil? merged-config)
      (throw (ex-info (str "Unable to merge config")
                      {:anomaly/category ::invalid-system-config})))
    merged-config))

(def system nil)

(defn start-system []
  (let [config (read-config)]
    (ig/load-namespaces config)
    (alter-var-root #'system (fn [_] (ig/init config)))
    :started))

(defn stop-system []
  (when (and (bound? #'system)
             (not (nil? system)))
    (alter-var-root #'system (fn [system] (ig/halt! system)))
    :stopped))

(defn -main [& args]
  (start-system))
