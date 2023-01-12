import java.io.File
import java.net.URL
import java.nio.file.Paths

val PROJECT_ROOT_DIR: File = __FILE__.absoluteFile // this script
    .parentFile // public-suffix-dir
    .parentFile // scripts
    .parentFile // ProtonPass

val OUTPUT_FILE = Paths.get(PROJECT_ROOT_DIR.absolutePath, "pass", "data", "impl", "src", "main", "res", "raw", "public_suffix_list.txt")

val DOWNLOAD_URL = "https://publicsuffix.org/list/public_suffix_list.dat"

val url = URL(DOWNLOAD_URL)

println("Opening connection")
val connection = url.openConnection()
connection.connect()

println("Reading data")
val data = connection.inputStream.bufferedReader().readText()

val meaningfulLines = data.lines()
    .filterNot { it.startsWith("//") || it.startsWith("!") || it.isBlank() }
    .map { it.replace("*.", "") }
    .joinToString("\n")

val outputFileAsFile: File = OUTPUT_FILE.toFile()
if (outputFileAsFile.exists()) {
    outputFileAsFile.delete()
}
outputFileAsFile.createNewFile()
outputFileAsFile.writeText(meaningfulLines)

println("Content has been written to $OUTPUT_FILE")
