package site.dao

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class Schema(val schema_name: String)

class SupabaseSchemaManager(private val supabase: SupabaseClient) {

    /**
     * Lists schemas by calling the 'get_schemas' RPC function in Supabase.
     */
    suspend fun listSchemas(): List<String> {
        val result = supabase.postgrest.rpc("get_schemas").decodeList<Schema>()
        return result.map { it.schema_name }
    }
}