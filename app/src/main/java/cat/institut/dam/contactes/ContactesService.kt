package cat.institut.dam.contactes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Servei encarregat d'obtenir els contactes (de la xarxa o de la còpia local)
 * i de descarregar imatges d'avatar.
 */
object ContactesService {

    const val URL_CONTACTES = "https://jsonplaceholder.typicode.com/users"
    private const val TIMEOUT_MS = 8000

    /** Descarrega la llista de contactes des del servidor remot. */
    fun desDeXarxa(): List<Contacte> {
        val connexio = (URL(URL_CONTACTES).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
        }
        return try {
            val text = connexio.inputStream.bufferedReader().use { it.readText() }
            parseja(text)
        } finally {
            connexio.disconnect()
        }
    }

    /** Llegeix la llista de contactes del fitxer local `assets/contactes.json`. */
    fun desDeRecurs(context: Context): List<Contacte> {
        val text = context.assets.open("contactes.json").bufferedReader().use { it.readText() }
        return parseja(text)
    }

    /** Converteix una cadena JSON (array d'usuaris) en una llista de [Contacte]. */
    fun parseja(text: String): List<Contacte> {
        val array = JSONArray(text)
        val resultat = mutableListOf<Contacte>()
        for (i in 0 until array.length()) {
            resultat.add(Contacte.desDeJson(array.getJSONObject(i)))
        }
        return resultat
    }

    /** Descarrega una imatge des d'una [url] i la retorna com a [Bitmap], o `null` si falla. */
    fun baixaImatge(url: String): Bitmap? {
        var connexio: HttpURLConnection? = null
        return try {
            connexio = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
            }
            connexio.inputStream.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        } finally {
            connexio?.disconnect()
        }
    }
}
