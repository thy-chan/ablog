(set-env!
 :source-paths #{"src"}
 :resource-paths #{"theme"}
 :dependencies '[[adzerk/boot-cljs "2.1.4"]
                 [pandeiro/boot-http "0.8.3"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [adzerk/boot-reload "0.5.2"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[ablog.build :refer :all]
         )

(deftask parse
  "just a test"
  []
  (parse-file))


(deftask dev
  "run the blog server"
  []
  (comp
   (serve :dir "public")
   (watch)
   (cljs)
   (target :dir #{"public"})))