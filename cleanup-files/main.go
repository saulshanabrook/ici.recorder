// parses the checkpoint for a spark job and deletes the files after they
// have been processed
//
// every 100 seconds it:
//   1. Finds the file with the largest integer in the commits folder
//   2. reads all sources <= that file, stopping at a `.compact` source, which contains all previous numbers
//   2. For each commit, reads the source with that number
//   3. parses that source for files
//   4. deletes all the files delete
package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/url"
	"os"
	"path/filepath"
	"strconv"
	"time"
)

var checkpointFolder = os.Getenv("CHECKPOINT_FOLDER")

func handleErr(err error) {
	if err != nil {
		log.Panic(err)
	}
}

func processStreamedFile(uri string) {
	u, err := url.Parse(uri)
	handleErr(err)
	err = os.Remove(u.Path)
	if err == nil {
		log.Printf("Deletedg %v", u.Path)
	} else if !os.IsNotExist(err) {
		handleErr(err)
	}
}
func processCheckpointFile(path string) {
	log.Printf("Processing checkpoint %v...", path)
	f, err := os.Open(path)
	defer f.Close()
	handleErr(err)
	scanner := bufio.NewScanner(f)

	// fill up pathsPerBatch from file
	isFirstLine := true
	for scanner.Scan() {
		if isFirstLine {
			isFirstLine = false
			continue
		}
		var l struct {
			Path string
		}
		handleErr(json.Unmarshal(scanner.Bytes(), &l))
		processStreamedFile(l.Path)
	}
}

func mostRecentCommit() (maxCommit int) {
	files, err := ioutil.ReadDir(filepath.Join(checkpointFolder, "commits"))
	handleErr(err)
	for _, file := range files {
		i, err := strconv.Atoi(filepath.Base(file.Name()))
		handleErr(err)
		if i > maxCommit {
			maxCommit = i
		}
	}
	return
}

func isCompact(commit int) bool {
	return commit%10 == 9
}
func scan() {
	commit := mostRecentCommit()
	for ; commit >= 0; commit-- {
		path := filepath.Join(
			checkpointFolder,
			"sources",
			"0",
			strconv.Itoa(commit),
		)
		if isCompact(commit) {
			path = fmt.Sprintf("%v.compact", path)
		}
		processCheckpointFile(path)
		if isCompact(commit) {
			break
		}

	}
}

func main() {
	for {
		scan()
		time.Sleep(100 * time.Second)
	}
}
