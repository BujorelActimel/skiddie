fun main() {
    println("This is a stdout message")

    System.err.println("This is a stderr message")

    println("Please enter your name:")
    val name = readLine()
    println("Hello, $name!")

    println("Script completed successfully")
}

main()