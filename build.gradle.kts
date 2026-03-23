plugins {
    alias(libs.plugins.bakery)
    alias(libs.plugins.readme)
}

bakery { configPath = file("site.yml").absolutePath }
