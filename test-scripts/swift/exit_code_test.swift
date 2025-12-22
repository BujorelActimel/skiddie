import Foundation

// This script exits with a non-zero exit code
// Tests Skiddie's handling of process exit codes

print("Starting exit code test...")
print("This script will exit with code 42")

fputs("ERROR: Simulated failure condition\n", stderr)
exit(42)

print("This should never print")
