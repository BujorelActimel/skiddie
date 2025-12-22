import java.time.LocalTime
import java.time.format.DateTimeFormatter

val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

println("Starting real-time output test...")
println("You should see these lines appear one by one, not all at once.\n")

for (i in 1..50) {
    val currentTime = LocalTime.now().format(timeFormatter)
    println("\u001B[36mLine $i\u001B[0m - Current time: $currentTime")
    System.out.flush()
    Thread.sleep(100)
}

println("\n\u001B[1;32m✓ Test complete!\u001B[0m")
println("If you saw lines appearing one by one, real-time display is working correctly.")
