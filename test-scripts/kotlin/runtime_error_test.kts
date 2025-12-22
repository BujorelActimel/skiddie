// This script compiles but crashes at runtime
// Tests Skiddie's handling of runtime exceptions

println("Starting runtime error test...")
println("This should print successfully")

// Force a null pointer exception
val nullValue: String? = null
println("About to crash...")
println(nullValue!!.length) // This will throw NullPointerException

println("This line should never be reached")
