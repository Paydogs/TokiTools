package hu.paydogs.tokitools.proportion

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import hu.paydogs.tokitools.settings.SettingsStore
import org.json.JSONArray
import org.json.JSONObject

/** A complete proportion as shown when it was remembered; [result] was the calculated field. */
data class RememberedProportion(val values: Map<ProportionField, String>,
                                val result: ProportionField) {
    val label: String
        get() {
            fun value(field: ProportionField): String = values[field].orEmpty()
            return "${value(ProportionField.X1)}/${value(ProportionField.X2)} = " +
                "${value(ProportionField.Y1)}/${value(ProportionField.Y2)}"
        }
}

private const val MAX_REMEMBERED = 50

/** Newest first; the same values remembered again move to the top instead of repeating. */
fun List<RememberedProportion>.withRemembered(entry: RememberedProportion): List<RememberedProportion> =
    (listOf(entry) + filter { existing: RememberedProportion -> existing.values != entry.values })
        .take(MAX_REMEMBERED)

class ProportionHistoryStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SettingsStore.PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<RememberedProportion> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        // Unreadable data is dropped rather than crashing the app.
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return (0 until array.length()).mapNotNull { index: Int -> decode(array.optJSONObject(index)) }
    }

    fun save(history: List<RememberedProportion>) {
        val array = JSONArray()
        history.forEach { entry: RememberedProportion -> array.put(encode(entry)) }
        prefs.edit { putString(KEY_HISTORY, array.toString()) }
    }

    private fun encode(entry: RememberedProportion): JSONObject {
        val json = JSONObject()
        ProportionField.entries.forEach { field: ProportionField -> json.put(field.name, entry.values[field].orEmpty()) }
        json.put(KEY_RESULT, entry.result.name)
        return json
    }

    private fun decode(json: JSONObject?): RememberedProportion? {
        if (json == null) return null
        val result = ProportionField.entries.firstOrNull { field: ProportionField -> field.name == json.optString(KEY_RESULT) }
            ?: return null
        val values = ProportionField.entries.associateWith { field: ProportionField -> json.optString(field.name) }
        return RememberedProportion(values = values, result = result)
    }

    private companion object {
        const val KEY_HISTORY = "proportion_history"
        const val KEY_RESULT = "result"
    }
}
