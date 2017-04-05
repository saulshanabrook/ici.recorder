(defproject ici.recorder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://saulshanabrook.com/ici.recorder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [proto-repl "0.3.1"]
                 [clojure.java-time "0.2.2"]
                 [org.apache.parquet/parquet-hadoop "1.9.0"]
                 [com.fzakaria/slf4j-timbre "0.3.4"]
                 [org.slf4j/log4j-over-slf4j "1.7.14"]
                 [org.alluxio/alluxio-core-client "1.4.0"]
                 [environ "1.1.0"]
                 [potemkin "0.4.3"]
                 [com.taoensso/timbre "4.8.0"]]

  :exclusions [org.slf4j/slf4j-log4j12]
  :managed-dependencies [[org.apache.hadoop/hadoop-common "2.2.0"]]
  ; :jvm-opts ^:replace ["-Xmx32G"]
                      ;  "-XX:+UseConcMarkSweepGC"
                      ;  "-XX:+CMSClassUnloadingEnabled"
                      ;  "-Xverify:none"])
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
                  ["vcs" "push"]])
