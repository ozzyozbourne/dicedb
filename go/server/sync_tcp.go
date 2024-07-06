package server

import (
	"fmt"
	"io"
	"log"
	"net"

	"github.com/ozzyozbourne/dicedb/go/config"
)

func readCommand(c net.Conn) ([]byte, error) {
	buf := make([]byte, 512)
	n, err := c.Read(buf[:])
	if err != nil {
		return []byte{}, err
	}
	return buf[:n], nil
}

func respond(cmd []byte, c net.Conn) error {
	if _, err := c.Write(cmd); err != nil {
		return err
	}
	return nil
}

func RunSyncTCPServer() {
	log.Printf("starting a synchronous TCP server on %s %d\n", config.Host, config.Port)
	var con_clients int

	lsnr, err := net.Listen("tcp", fmt.Sprintf("%s:%d", config.Host, config.Port))
	if err != nil {
		panic(err)
	}
	for {
		c, err := lsnr.Accept()
		if err != nil {
			panic(err)
		}
		con_clients += 1
		log.Printf("client connected with address: %v concurrent clients: %d\n", c.RemoteAddr(), con_clients)

		for {
			cmd, err := readCommand(c)
			if err != nil {
				c.Close()
				con_clients -= 1
				log.Printf("client disconnected %v concurrent clients %d\n", c.RemoteAddr(), con_clients)
				if err == io.EOF {
					break
				}
				log.Printf("%v\n", err)
			}
			log.Printf("command: %s", cmd)
			if err := respond(cmd, c); err != nil {
				log.Printf("%v\n", err)
			}
		}

	}

}
