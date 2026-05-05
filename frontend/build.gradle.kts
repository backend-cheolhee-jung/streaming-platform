plugins {
    base
}

val npmInstall by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Install npm dependencies"
    workingDir = projectDir
    commandLine("npm", "install")
    inputs.file("package.json")
    inputs.file("package-lock.json").optional()
    outputs.dir("node_modules")
}

val npmBuild by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Build React app with Vite"
    workingDir = projectDir
    commandLine("npm", "run", "build")
    dependsOn(npmInstall)
    inputs.dir("src")
    inputs.file("vite.config.ts")
    inputs.file("tsconfig.json")
    outputs.dir("dist")
}

val playwrightTest by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Run Playwright E2E tests"
    workingDir = projectDir
    commandLine("npx", "playwright", "test")
    dependsOn(npmInstall)
}

tasks.named("assemble") { dependsOn(npmBuild) }
tasks.named("check") { dependsOn(playwrightTest) }
tasks.named("clean") {
    doLast {
        delete("dist", "node_modules/.cache")
    }
}
