(defproject ici.recorder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://saulshanabrook.com/ici.recorder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [proto-repl "0.3.1"]
                 [org.clojure/test.check "0.9.0"]
                ;  [com.damballa/abracad "0.4.13"]
                 [clojure.java-time "0.2.2"]
                 [org.apache.parquet/parquet-hadoop "1.9.0"]
                ;  [org.apache.hadoop/hadoop-common "2.7.3"]
                 [com.fzakaria/slf4j-timbre "0.3.4"]
                 [org.slf4j/log4j-over-slf4j "1.7.14"]
                ;  [com.taoensso/timbre "4.8.0"]
                 [org.alluxio/alluxio-core-client "1.4.0"]]
                ;  [com.google.guava/guava "21.0"]]
  :main ici.test)
