import java.io.File
import java.net.URL
import java.nio.file.Paths

val PROJECT_ROOT_DIR: File = __FILE__.absoluteFile // this script
    .parentFile // public-suffix-dir
    .parentFile // scripts
    .parentFile // ProtonPass

val OUTPUT_FILE = Paths.get(PROJECT_ROOT_DIR.absolutePath, "pass", "password", "api", "src", "main", "kotlin", "proton", "android", "pass", "password", "api", "WordList.kt")

val DOWNLOAD_URL = "https://www.eff.org/files/2016/07/18/eff_large_wordlist.txt"

val url = URL(DOWNLOAD_URL)

println("Opening connection")
val connection = url.openConnection()
connection.connect()

println("Reading data")
val data = connection.inputStream.bufferedReader().readText()

val words = data.lines()
    .filter { it.isNotBlank() }
    .map { it.split("\t")[1] }
    .map { "\"$it\"" }

val outputFileAsFile: File = OUTPUT_FILE.toFile()
if (outputFileAsFile.exists()) {
    outputFileAsFile.delete()
}
outputFileAsFile.createNewFile()

val contents = """
package proton.android.pass.password.api

val WORDS = listOf(
    ${words.joinToString(",\n    ")}
)

""".trimIndent()

outputFileAsFile.writeText(contents)

println("Content has been written to $OUTPUT_FILE")
