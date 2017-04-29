// parses the checkpoint for a spark job and deletes the files after they
// have been processed
//
// every 10 seconds it:
//   1. reads every file in the directory starts with an integer (ignores temp files)
//   2. parses them all for paths and batchIDs
//   3. goes through all files and tries to delete
package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"net/url"
	"os"
	"path/filepath"
	"time"
)

var checkpointFolder = os.Getenv("CHECKPOINT_FOLDER")

func handleErr(err error) {
	if err != nil {
		fmt.Println(err)
		panic(err)
	}
}

func processStreamedFile(uri string) {
	u, err := url.Parse(uri)
	handleErr(err)
	err = os.Remove(u.Path)
	if err == nil {
		fmt.Printf("Deletedg %v\n", u.Path)
	} else if !os.IsNotExist(err) {
		handleErr(err)
	}
}
func processCheckpointFile(path string) {
	fmt.Printf("Processing checkpoint %v...\n", path)
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

func scan() {
	pattern := filepath.Join(
		checkpointFolder,
		"sources",
		"0",
		"[0-9]*",
	)
	fmt.Printf("Scanning pattern %v...\n", pattern)
	matches, err := filepath.Glob(pattern)
	handleErr(err)
	for _, path := range matches {
		processCheckpointFile(path)
	}
}

func main() {
	for {
		scan()
		time.Sleep(10 * time.Second)
	}
}
