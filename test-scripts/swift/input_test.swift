import Foundation

func main() {
    print("This is a stdout message")

    fputs("This is a stderr message\n", stderr)

    print("Please enter your name:")
    if let name = readLine() {
        print("Hello, \(name)!")
    }

    print("Script completed successfully")
}

main()
