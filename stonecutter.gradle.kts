plugins {
    id("dev.kikugie.stonecutter")
}

// The version whose source comments are "active" in your editor.
// Switch with the generated tasks, e.g. "Set active project to 1.21.1".
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    // Mojang renamed ResourceLocation -> Identifier in the 1.21.11 / 26.x line.
    // This rewrites the token (usages AND imports) automatically for newer versions,
    // so the source can be written once using `ResourceLocation`.
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
        // Level.isClientSide became a private field with a public isClientSide() accessor
        // in the 26.x line. Every usage here is a `level.isClientSide` read, so appending
        // the call parentheses to the identifier turns each into the accessor invocation.
        string(current.parsed >= "26.1") {
            replace("isClientSide", "isClientSide()")
        }
    }
}

// Stonecutter 0.9.x auto-provides the "chiseled" tasks (e.g. `chiseledBuild`) that
// fan a task out across every declared version — no manual registration needed.
// Run `./gradlew tasks --group stonecutter` to list them.
