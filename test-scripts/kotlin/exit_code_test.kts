// This script exits with a non-zero exit code
// Tests Skiddie's handling of process exit codes

println("Starting exit code test...")
println("This script will exit with code 42")

System.err.println("ERROR: Simulated failure condition")
kotlin.system.exitProcess(42)

println("This should never print")
