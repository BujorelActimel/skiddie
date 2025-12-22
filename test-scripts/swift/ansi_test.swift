func ansi(_ code: String) -> String {
    return "\u{001B}[\(code)m"
}

func reset() -> String {
    return ansi("0")
}

print("\(ansi("31"))Red text\(reset())")
print("\(ansi("32"))Green text\(reset())")
print("\(ansi("33"))Yellow text\(reset())")
print("\(ansi("34"))Blue text\(reset())")
print("\(ansi("35"))Magenta text\(reset())")
print("\(ansi("36"))Cyan text\(reset())")
print("\(ansi("37"))White text\(reset())")

print("\(ansi("91"))Bright red text\(reset())")
print("\(ansi("92"))Bright green text\(reset())")
print("\(ansi("93"))Bright yellow text\(reset())")
print("\(ansi("94"))Bright blue text\(reset())")
print("\(ansi("95"))Bright magenta text\(reset())")
print("\(ansi("96"))Bright cyan text\(reset())")

print("\(ansi("1"))Bold text\(reset())")
print("\(ansi("1;31"))Bold red text\(reset())")
print("\(ansi("1;32"))Bold green text\(reset())")

print("\(ansi("3"))Italic text\(reset())")
print("\(ansi("3;34"))Italic blue text\(reset())")

print("\(ansi("4"))Underlined text\(reset())")
print("\(ansi("4;35"))Underlined magenta text\(reset())")

print("\(ansi("1;4;32"))Bold underlined green text\(reset())")
print("\(ansi("3;91"))Italic bright red text\(reset())")

print("\(ansi("41"))Red background\(reset())")
print("\(ansi("42"))Green background\(reset())")
print("\(ansi("43"))Yellow background\(reset())")
print("\(ansi("44"))Blue background\(reset())")

print("\(ansi("31"))Red\(reset()) and \(ansi("32"))green\(reset()) and \(ansi("34"))blue\(reset())")

print("\(ansi("1;32"))✓ Success:\(reset()) Operation completed successfully")
print("\(ansi("1;31"))✗ Error:\(reset()) Something went wrong")
print("\(ansi("1;33"))⚠ Warning:\(reset()) Please check your input")

print("\n\(ansi("1;37"))Color Table:\(reset())")
print("\(ansi("1"))━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\(reset())")
print("\(ansi("1;34"))Name\(reset())        \(ansi("1;34"))Status\(reset())")
print("\(ansi("1"))━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\(reset())")
print("Build       \(ansi("1;32"))✓ PASSED\(reset())")
print("Tests       \(ansi("1;32"))✓ PASSED\(reset())")
print("Coverage    \(ansi("1;33"))⚠ 87%\(reset())")
print("Deploy      \(ansi("1;31"))✗ FAILED\(reset())")
print("\(ansi("1"))━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\(reset())")

let colors = ["31", "33", "32", "36", "34", "35"]
let text = "ANSI colors work!"
print()
for (index, char) in text.enumerated() {
    let color = colors[index % colors.count]
    print("\(ansi(color))\(char)\(reset())", terminator: "")
}
print("\n")

print("\(ansi("1;36"))ANSI color test complete!\(reset())")
