FROM golang:1.7

RUN go get github.com/djherbis/bufit github.com/satori/go.uuid
COPY main.go main.go
CMD ["go", "run", "main.go"]
