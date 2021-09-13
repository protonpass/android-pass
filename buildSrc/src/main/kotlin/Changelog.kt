import java.io.File
import java.lang.StringBuilder

fun generateChangelog(
    workingDir: File = File("."),
    since: String? = null
): String =
    "git log --pretty=%s --no-decorate --abbrev-commit${since?.let { " HEAD ^$it" } ?: ""}"
        .runCommand(workingDir)
        .split("\n")
        .groupBy(Group::fromMessage) { message ->
            message.substringAfter(':')
        }
        .let { changes ->
            buildString {
                append("# Changelog\n\n")
                enumValues<Group>()
                    .filter { group -> group.isIncluded }
                    .forEach { group -> add(group, changes) }
            }
        }

private fun StringBuilder.add(group: Group, changes: Map<Group, List<String>>) {
    if (changes.containsKey(group)) {
        append("## ${group.title}\n")
        changes[group]?.forEach { message -> append("- $message\n") }
        append("\n")
    }
}

private enum class Group(val key: String, val title: String, val isIncluded: Boolean = false) {
    FEAT("feat", "New features", true),
    FIX("fix", "Bug fixes", true),
    CHORE("chore", "Maintenance"),
    BUILD("build", "Build"),
    CI("ci", "Continuous Integration"),
    DOCS("docs", "Documentation"),
    STYLE("style", "Style"),
    REFACTOR("refactor", "Refactoring"),
    PERF("perf", "Performances"),
    TEST("test", "Tests"),
    IGNORED("", "IGNORED");

    companion object {
        fun fromMessage(message: String): Group {
            values().forEach { group ->
                if (message.startsWith(group.key)) {
                    return group
                }
            }
            return IGNORED
        }
    }
}