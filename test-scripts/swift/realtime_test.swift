import Foundation

let dateFormatter = DateFormatter()
dateFormatter.dateFormat = "HH:mm:ss"

print("Starting real-time output test...")
print("You should see these lines appear one by one, not all at once.\n")

for i in 1...50 {
    let currentTime = dateFormatter.string(from: Date())
    print("\u{001B}[36mLine \(i)\u{001B}[0m - Current time: \(currentTime)")
    fflush(stdout)
    Thread.sleep(forTimeInterval: 0.1)
}

print("\n\u{001B}[1;32m✓ Test complete!\u{001B}[0m")
print("If you saw lines appearing one by one, real-time display is working correctly.")
