package com.cheroliv.bakery

data class GitPushConfiguration(
    val from: String = "",
    val to: String = "",
    val repo: RepositoryConfiguration = RepositoryConfiguration(),
    val branch: String = "",
    val message: String = "",
)

data class RepositoryConfiguration(
    val name: String = "",
    val repository: String = "",
    val credentials: RepositoryCredentials = RepositoryCredentials(),
) {
    companion object {
        const val ORIGIN = "origin"
        const val CNAME = "CNAME"
        const val REMOTE = "remote"
    }
}

data class RepositoryCredentials(val username: String = "", val password: String = "")

data class SiteConfiguration(
    val bake: BakeConfiguration = BakeConfiguration(),
    val pushPage: GitPushConfiguration = GitPushConfiguration(),
    val pushMaquette: GitPushConfiguration = GitPushConfiguration(),
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
    val supabase: SupabaseContactFormConfig? = null
)

data class BakeConfiguration(
    val srcPath: String = "",
    val destDirPath: String = "",
    val cname: String? = null,
)

data class SupabaseContactFormConfig(
    val project: SupabaseProjectInfo,
    val schema: SupabaseDatabaseSchema,
    val rpc: SupabaseRpcFunction
)

data class SupabaseProjectInfo(
    val url: String,
    val publicKey: String // La clé "anon" pour le client JS
)

data class SupabaseDatabaseSchema(
    val contacts: SupabaseTable,
    val messages: SupabaseTable
)

data class SupabaseTable(
    val name: String,
    val columns: List<SupabaseColumn>,
    val rlsEnabled: Boolean
)

data class SupabaseColumn(
    val name: String,
    val type: String
)

data class SupabaseRpcFunction(
    val name: String,
    val params: List<SupabaseParam>
)

data class SupabaseParam(
    val name: String,
    val type: String
)

