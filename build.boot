(set-env!
 :source-paths #{"src" "markdown/clj" "markdown/cljc"}
 :target-path #{"target"}
 :dependencies '[[adzerk/boot-cljs "2.1.4"]
                 [pandeiro/boot-http "0.8.3"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 ;[adzerk/boot-reload "0.5.2"]
                 ;[markdown-clj "1.0.5"]
                 [clj-time "0.14.2"]
                 [selmer "1.11.7"]
                 ;[adzerk/boot-test "1.2.0"]
                 ;[seancorfield/boot-expectations "1.0.11"]
                 [metosin/bat-test "0.4.0" :scope "test"]
                 [onetom/boot-lein-generate "0.1.3" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.13" :scope "test"]
                 [deraen/boot-sass "0.3.1"]
                 ])


(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[ablog.core :refer [generate get-settings]]
         ;'[adzerk.boot-reload :refer [reload]]
         ;'[adzerk.boot-test :refer :all]
         '[deraen.boot-sass :refer [sass]]
         '[metosin.bat-test :refer (bat-test)])


(defn- macro-files-changed
  "获取变动的文件: 增加或修改"
  [diff]
  (->> (input-files diff)
       (by-ext ["md"])
       (map tmp-path)))


(deftask watch-generate-public
  []
  (let [tmp-result (tmp-dir!)
        compilers  (atom {})
        prev       (atom nil)
        prev-deps  (atom (get-env :dependencies))
        settings (get-settings)]
    (comp
      (with-pre-wrap fileset
        (let [diff          (fileset-diff @prev fileset)
              macro-changes (macro-files-changed diff)])
        (generate "pub")
        (reset! prev fileset)
        (-> fileset commit!)))))


(deftask watch-generate-local
         []
         (let [tmp-result (tmp-dir!)
               compilers  (atom {})
               prev       (atom nil)
               prev-deps  (atom (get-env :dependencies))
               settings (get-settings)]
           (comp
             (with-pre-wrap fileset
                            (let [diff          (fileset-diff @prev fileset)
                                  macro-changes (macro-files-changed diff)])
                            (generate "dev")
                            (reset! prev fileset)
                            (-> fileset commit!)))))


(deftask parse
  "generate html"
  []
(generate "pub"))

(deftask build
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (aot :namespace #{'main.entrypoint})
   (uber)
   (jar :file "project.jar" :main 'main.entrypoint)
   (sift :include #{#"project.jar"})
   (target)))


(deftask dev
         "run the blog server"
         []

         (let [settings (get-settings)
               public-dir (:public-dir settings)
               other-paths [(:posts-dir settings) (:pages-dir settings) (:private-posts-dir settings)]
               src-paths #{"src" "markdown/clj" "markdown/cljc"}
               ]
           (set-env! :source-paths (reduce (fn [s k] (if k (conj s k) s)) src-paths other-paths))
           (comp
             (serve :dir public-dir :port 3006)
             (watch)
             ;(reload)
             (watch-generate-local)
             ;(cljs :compiler-options {:output-to "main.js"})
             ;(target :dir #{(str public-dir "js/")})
             )))


(deftask pub
  "run the blog with private server"
  []
  (let [settings (get-settings)
        public-dir (:public-dir settings)
        posts-dir (:posts-dir settings)
        pages-dir (:pages-dir settings)
        private-posts-dir (:private-posts-dir settings)
        ]
  (set-env! :source-paths #{posts-dir pages-dir private-posts-dir})
  (comp
    (serve :dir public-dir :port 3006)
    (watch)
    ;(reload)
    (watch-generate-public)
    ;(cljs :compiler-options {:output-to "main.js"})
    ;(target :dir #{(str public-dir "js/")})
)))
