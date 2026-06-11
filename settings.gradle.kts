rootProject.name = "AllMusic"

include(":codec")

val targetPlatform = providers.gradleProperty("platform").orNull
val isCI = providers.gradleProperty("platform").isPresent

fun shouldInclude(module: String): Boolean {
    if (!isCI) return true
    if (module.startsWith(":client:")) {
        val platform = module.substringAfter(":client:")
        return platform == targetPlatform
    }
    if (module.startsWith(":server:")) {
        val platform = module.substringAfter(":server:")
        return platform == targetPlatform
    }
    return true
}

if (!isCI || targetPlatform?.startsWith("fabric_") == true || 
    targetPlatform?.startsWith("forge_") == true || 
    targetPlatform?.startsWith("neoforge_") == true ||
    targetPlatform == "spigot" || targetPlatform == "paper" || 
    targetPlatform == "folia" || targetPlatform == "velocity") {
    if (!isCI || targetPlatform == null || targetPlatform.startsWith("fabric_") || 
        targetPlatform.startsWith("forge_") || targetPlatform.startsWith("neoforge_")) {
        include(":client")
        if (shouldInclude(":client:fabric_1_16_5")) include(":client:fabric_1_16_5")
        if (shouldInclude(":client:fabric_1_20_1")) include(":client:fabric_1_20_1")
        if (shouldInclude(":client:fabric_1_21")) include(":client:fabric_1_21")
        if (shouldInclude(":client:fabric_1_21_6")) include(":client:fabric_1_21_6")
        if (shouldInclude(":client:fabric_1_21_11")) include(":client:fabric_1_21_11")
        if (shouldInclude(":client:fabric_26_1")) include(":client:fabric_26_1")
        //include(":client:forge_1_7_10")
        //include(":client:forge_1_12_2")
        if (shouldInclude(":client:forge_1_16_5")) include(":client:forge_1_16_5")
        if (shouldInclude(":client:forge_1_20_1")) include(":client:forge_1_20_1")
        if (shouldInclude(":client:neoforge_1_21")) include(":client:neoforge_1_21")
        if (shouldInclude(":client:neoforge_1_21_6")) include(":client:neoforge_1_21_6")
        if (shouldInclude(":client:neoforge_1_21_11")) include(":client:neoforge_1_21_11")
        if (shouldInclude(":client:neoforge_26_1")) include(":client:neoforge_26_1")
    }

    if (!isCI || targetPlatform == null || 
        targetPlatform == "spigot" || targetPlatform == "paper" || 
        targetPlatform == "folia" || targetPlatform == "velocity" ||
        targetPlatform.startsWith("fabric_") || targetPlatform.startsWith("forge_") || 
        targetPlatform.startsWith("neoforge_")) {
        include(":server")

        if (shouldInclude(":server:fabric_1_16_5")) include(":server:fabric_1_16_5")
        if (shouldInclude(":server:fabric_1_20_1")) include(":server:fabric_1_20_1")
        if (shouldInclude(":server:fabric_1_21")) include(":server:fabric_1_21")
        if (shouldInclude(":server:fabric_1_21_6")) include(":server:fabric_1_21_6")
        if (shouldInclude(":server:fabric_1_21_11")) include(":server:fabric_1_21_11")
        if (shouldInclude(":server:fabric_26_1")) include(":server:fabric_26_1")

        //include(":server:forge_1_7_10")
        //include(":server:forge_1_12_2")
        if (shouldInclude(":server:forge_1_16_5")) include(":server:forge_1_16_5")
        if (shouldInclude(":server:forge_1_20_1")) include(":server:forge_1_20_1")

        if (shouldInclude(":server:neoforge_1_21")) include(":server:neoforge_1_21")
        if (shouldInclude(":server:neoforge_1_21_6")) include(":server:neoforge_1_21_6")
        if (shouldInclude(":server:neoforge_1_21_11")) include(":server:neoforge_1_21_11")
        if (shouldInclude(":server:neoforge_26_1")) include(":server:neoforge_26_1")

        if (shouldInclude(":server:spigot")) include(":server:spigot")
        if (shouldInclude(":server:paper")) include(":server:paper")
        if (shouldInclude(":server:folia")) include(":server:folia")
        if (shouldInclude(":server:velocity")) include(":server:velocity")

        include(":onejar")
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://nexus.gtnewhorizons.com/repository/public/")
    }
}
