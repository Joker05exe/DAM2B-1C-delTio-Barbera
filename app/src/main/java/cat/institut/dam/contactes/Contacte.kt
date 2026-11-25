package cat.institut.dam.contactes

import org.json.JSONObject

/**
 * Model de dades d'un contacte de l'agenda.
 */
data class Contacte(
    val id: Int,
    val nom: String,
    val username: String,
    val email: String,
    val telefon: String,
    val web: String,
    val carrer: String,
    val ciutat: String,
    val codiPostal: String,
    val latitud: Double,
    val longitud: Double
) {
    /** Inicials de fins a dues paraules del nom per mostrar al cercle de la llista. */
    val inicials: String
        get() = nom
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")

    /** Adreça postal completa en una sola línia. */
    val adrecaCompleta: String
        get() = "$carrer, $ciutat, $codiPostal"

    /** URL de l'avatar del contacte a pravatar. */
    val urlAvatar: String
        get() = "https://i.pravatar.cc/300?u=$username"

    companion object {
        /** Construeix un [Contacte] a partir d'un [JSONObject] amb l'esquema de JSONPlaceholder. */
        fun desDeJson(json: JSONObject): Contacte {
            val adreca = json.getJSONObject("address")
            val geo = adreca.getJSONObject("geo")
            return Contacte(
                id = json.getInt("id"),
                nom = json.getString("name"),
                username = json.getString("username"),
                email = json.getString("email"),
                telefon = json.getString("phone"),
                web = json.getString("website"),
                carrer = adreca.getString("street"),
                ciutat = adreca.getString("city"),
                codiPostal = adreca.getString("zipcode"),
                latitud = geo.optString("lat", "0").toDoubleOrNull() ?: 0.0,
                longitud = geo.optString("lng", "0").toDoubleOrNull() ?: 0.0
            )
        }
    }
}
