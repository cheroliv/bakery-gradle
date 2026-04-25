package bakery

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
    val pushProfile: GitPushConfiguration? = null,
    val profileFiles: List<String> = emptyList(),
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
    val firebase: FirebaseContactFormConfig? = null
)

data class BakeConfiguration(
    val srcPath: String = "",
    val destDirPath: String = "",
    val cname: String = "",
)

data class FirebaseContactFormConfig(
    val project: FirebaseProjectInfo,
    val firestore: FirebaseFirestoreSchema,
    val callable: FirebaseCallableFunction
)

data class FirebaseProjectInfo(
    val projectId: String,
    val apiKey: String
)

data class FirebaseFirestoreSchema(
    val contacts: FirebaseCollection,
    val messages: FirebaseCollection
)

data class FirebaseCollection(
    val name: String,
    val fields: List<FirebaseField>,
    val rulesEnabled: Boolean
)

data class FirebaseField(
    val name: String,
    val type: String
)

data class FirebaseCallableFunction(
    val name: String,
    val params: List<FirebaseCallableParam>
)

data class FirebaseCallableParam(
    val name: String,
    val type: String
)

