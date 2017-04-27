package main

import (
	"bufio"
	"fmt"
	"io"
	"io/ioutil"
	"net"
	"os"
	"path"
)

func handleErr(err error) {
	if err != nil {
		fmt.Println(err)
		panic(err)
	}
}

func listen(lines chan<- []byte) {
	defer close(lines)
	l, err := net.Listen("tcp", fmt.Sprintf(":%v", os.Getenv("PORT")))
	handleErr(err)
	defer l.Close()
	for {
		// Wait for a connection.
		conn, err := l.Accept()
		handleErr(err)
		// Handle the connection in a new goroutine.
		// The loop then returns to accepting, so that
		// multiple connections may be served concurrently.
		go func(c net.Conn) {
			defer c.Close()
			rd := bufio.NewReader(c)
			for {
				line, err := rd.ReadBytes('\n')
				if err == io.EOF {
					break
				} else {
					handleErr(err)
				}
				lines <- line
			}
		}(conn)
	}
}

func write(lines <-chan []byte) {
	folder := os.Getenv("FOLDER")
	finalFolder := path.Join(folder, "final")
	os.Mkdir(finalFolder, 0777)
	tmpFolder := path.Join(folder, "tmp")
	os.Mkdir(tmpFolder, 0777)
	checkpointPath := path.Join(folder, "checkpoint")
	iBytes, err := ioutil.ReadFile(checkpointPath)
	var i int
	if err == nil {
		i = int(iBytes[0])
	}
	for line := range lines {
		// create as tempfile
		tmpPath := path.Join(tmpFolder, fmt.Sprint(i))
		handleErr(ioutil.WriteFile(tmpPath, line, 0777))
		// move so that it ends up there atomicaly
		path := path.Join(finalFolder, fmt.Sprintf("%v.json", i))
		i++
		ioutil.WriteFile(checkpointPath, []byte{byte(i)}, 0777)
		handleErr(os.Rename(tmpPath, path))

	}
}

func main() {
	lines := make(chan []byte)
	go listen(lines)
	write(lines)
}
