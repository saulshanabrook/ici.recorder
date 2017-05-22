package main

import (
	"bufio"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"os"
	"path"

	uuid "github.com/satori/go.uuid"
)

func handleErr(err error) {
	if err != nil {
		log.Panic(err)
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
			log.Println("Opening connection")
			defer log.Println("Closing connection")
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
	for line := range lines {
		u := uuid.NewV4()
		// create as tempfile
		tmpPath := path.Join(tmpFolder, fmt.Sprint(u))
		handleErr(ioutil.WriteFile(tmpPath, line, 0777))
		// move so that it ends up there atomicaly
		path := path.Join(finalFolder, fmt.Sprintf("%v.json", u))
		handleErr(os.Rename(tmpPath, path))
		log.Printf("Saved line %v", u)
	}
}

func main() {
	lines := make(chan []byte)
	go listen(lines)
	write(lines)
}
