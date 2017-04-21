(defproject ici.recorder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://saulshanabrook.com/ici.recorder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main "ici-recorder.clojush-benchmark"
  ; :main "ici-recorder.parquet.add-data"
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                ;  [proto-repl "0.3.1"]
                ;  [clojure.java-time "0.2.2"]
                 [org.apache.parquet/parquet-hadoop "1.9.0"]
                ;  [com.fzakaria/slf4j-timbre "0.3.4"]
                ;  [org.slf4j/log4j-over-slf4j "1.7.14"]
                  ; :exclusions [org.slf4j/slf4j-api]]
                 [environ "1.1.0"]
                 [potemkin "0.4.3"]
                 [proto-repl "0.3.1"]
                ;  [com.taoensso/timbre "4.10.0"]
                 [org.clojure/test.check "0.9.0"]
                 [criterium "0.4.4"]
                ;  [org.apache.logging.log4j/log4j-api "2.8.2"]
                ;  [org.apache.logging.log4j/log4j-core "2.8.2"]
                ;  [org.apache.logging.log4j/log4j-1.2-api "2.8.2"]]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.apache.hadoop/hadoop-client "2.3.0"]]

  ; :exclusions [org.slf4j/slf4j-log4j12]
  :managed-dependencies [; use the same version of this as parquet-hadoop
                         [org.apache.hadoop/hadoop-common "2.3.0"]
                         ; force upgrade this to deal with
                         ; https://github.com/ptaoussanis/carmine/issues/5
                         [org.xerial.snappy/snappy-java "1.1.4-M3"]]
  :repositories [["localrepo1"  {:url "file:myrepo"
                                 ;; provide these so doesn't iteractivly ask for config
                                 :username :env/localrepo_username
                                 :password :env/localrepo_password}]]
  :jvm-opts ^:replace ["-Xmx16G"
                       "-XX:+UseConcMarkSweepGC"
                       "-XX:+CMSClassUnloadingEnabled"
                       "-Xverify:none"]
                      ;  "-agentpath:/Users/saul/Downloads/YourKit-Java-Profiler-2017.02.app/Contents/Resources/bin/mac/libyjpagent.jnilib"]

  :aot :all
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["shell" "git" "commit" "-am" "Version ${:version} [ci skip]"]
                  ["vcs" "tag" "v" "--no-sign"] ; disable signing and add "v" prefix
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version" "qualifier"]
                  ["shell" "git" "commit" "-am" "Version ${:version} [ci skip]"]
                  ["vcs" "push"]]
  :global-vars {*warn-on-reflection* true})
