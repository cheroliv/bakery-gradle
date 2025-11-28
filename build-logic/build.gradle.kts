plugins { `kotlin-dsl` }

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()
    google()
}

dependencies {
    val supabaseVersion = "3.2.3"

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    testImplementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
    testImplementation("io.github.jan-tennert.supabase:auth-kt-jvm:$supabaseVersion")
}

tasks.test {
    useJUnitPlatform()
}
