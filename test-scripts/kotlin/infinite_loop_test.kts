// This script runs an infinite loop
// Tests Skiddie's ability to stop/kill running processes

println("Starting infinite loop test...")
println("Press Cmd+Enter (or click Stop) to terminate this script")
println()

var counter = 0
while (true) {
    counter++
    println("Loop iteration: $counter")
    Thread.sleep(500)
}

println("This should never print")
