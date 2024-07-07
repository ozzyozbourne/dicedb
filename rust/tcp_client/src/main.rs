use std::{
    env,
    io::{Read, Write},
    net::TcpStream,
    time::Duration,
};

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() <= 3 {
        eprintln!("Usage: {} <HOST> <PORT>", args[0]);
    }

    match TcpStream::connect_timeout(
        &format!("{}:{}", args[1], args[2]).parse().unwrap(),
        Duration::new(5, 0),
    ) {
        Ok(mut stream) => {
            println!("Sucessfully conected");
            if let Err(e) = stream.write_all(b"ping") {
                eprintln!("Failed to send request: {}", e);
                return;
            }

            if let Err(e) = stream.flush() {
                eprintln!("Failed to flush stream: {}", e);
                return;
            }

            let mut buf = [0; 512];
            match stream.read(&mut buf) {
                Ok(bytes_read) => {
                    if bytes_read == 0 {
                        println!("No response received from the server");
                    } else {
                        println!(
                            "Response from server: {}",
                            String::from_utf8_lossy(&buf[..bytes_read])
                        );
                    }
                }
                Err(e) => eprintln!("Failed to read response: {}", e),
            }
        }
        Err(e) => eprintln!(
            "Failed to connect to {} {} error -> {}",
            args[1], args[2], e
        ),
    }
}
