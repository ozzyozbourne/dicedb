package main

import (
	"flag"
	"log"

	"github.com/ozzyozbourne/dicedb/go/config"
	"github.com/ozzyozbourne/dicedb/go/server"
)

func setupFlags() {
	flag.StringVar(&config.Host, "host", "0.0.0.0", "host for the dice server")
	flag.IntVar(&config.Port, "port", 7379, "port for the dice server")
	flag.Parse()
}

func main() {
	setupFlags()
	log.Printf("Rolling the dice 🎲")
	server.RunSyncTCPServer()
}
