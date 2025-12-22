fun ansi(code: String) = "\u001B[${code}m"
fun reset() = ansi("0")

println("${ansi("31")}Red text${reset()}")
println("${ansi("32")}Green text${reset()}")
println("${ansi("33")}Yellow text${reset()}")
println("${ansi("34")}Blue text${reset()}")
println("${ansi("35")}Magenta text${reset()}")
println("${ansi("36")}Cyan text${reset()}")
println("${ansi("37")}White text${reset()}")

println("${ansi("91")}Bright red text${reset()}")
println("${ansi("92")}Bright green text${reset()}")
println("${ansi("93")}Bright yellow text${reset()}")
println("${ansi("94")}Bright blue text${reset()}")
println("${ansi("95")}Bright magenta text${reset()}")
println("${ansi("96")}Bright cyan text${reset()}")

println("${ansi("1")}Bold text${reset()}")
println("${ansi("1;31")}Bold red text${reset()}")
println("${ansi("1;32")}Bold green text${reset()}")

println("${ansi("3")}Italic text${reset()}")
println("${ansi("3;34")}Italic blue text${reset()}")

println("${ansi("4")}Underlined text${reset()}")
println("${ansi("4;35")}Underlined magenta text${reset()}")

println("${ansi("1;4;32")}Bold underlined green text${reset()}")
println("${ansi("3;91")}Italic bright red text${reset()}")

println("${ansi("41")}Red background${reset()}")
println("${ansi("42")}Green background${reset()}")
println("${ansi("43")}Yellow background${reset()}")
println("${ansi("44")}Blue background${reset()}")

println("${ansi("31")}Red${reset()} and ${ansi("32")}green${reset()} and ${ansi("34")}blue${reset()}")

println("${ansi("1;32")}✓ Success:${reset()} Operation completed successfully")
println("${ansi("1;31")}✗ Error:${reset()} Something went wrong")
println("${ansi("1;33")}⚠ Warning:${reset()} Please check your input")

print("${ansi("36")}Processing: ${reset()}")
for (i in 1..10) {
    print("${ansi("1;32")}█${reset()}")
    Thread.sleep(150)
}
println(" ${ansi("1;32")}Done!${reset()}")

println("\n${ansi("1;37")}Color Table:${reset()}")
println("${ansi("1")}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${reset()}")
println("${ansi("1;34")}Name${reset()}        ${ansi("1;34")}Status${reset()}")
println("${ansi("1")}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${reset()}")
println("Build       ${ansi("1;32")}✓ PASSED${reset()}")
println("Tests       ${ansi("1;32")}✓ PASSED${reset()}")
println("Coverage    ${ansi("1;33")}⚠ 87%${reset()}")
println("Deploy      ${ansi("1;31")}✗ FAILED${reset()}")
println("${ansi("1")}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${reset()}")

val colors = listOf("31", "33", "32", "36", "34", "35")
val text = "ANSI colors work!"
println()
text.forEachIndexed { index, char ->
    val color = colors[index % colors.size]
    print("${ansi(color)}$char${reset()}")
}
println("\n")

println("${ansi("1;36")}ANSI color test complete!${reset()}")
