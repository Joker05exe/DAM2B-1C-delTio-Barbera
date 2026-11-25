package cat.institut.dam.contactes

import android.net.Uri

/**
 * Repositori en memòria (singleton) per compartir la llista de contactes,
 * els favorits i les fotos triades entre pantalles.
 */
object ContactesRepository {

    private val contactes = mutableListOf<Contacte>()
    private val favorits = mutableSetOf<Int>()
    private val fotos = mutableMapOf<Int, Uri>()

    /** Llista completa de contactes carregats actualment. */
    val llista: List<Contacte>
        get() = contactes.toList()

    /** Substitueix la llista de contactes per una de nova. */
    fun actualitza(novs: List<Contacte>) {
        contactes.clear()
        contactes.addAll(novs)
    }

    /** Retorna el contacte amb l'[id] indicat, o `null` si no existeix. */
    fun contacte(id: Int): Contacte? = contactes.firstOrNull { it.id == id }

    /** Indica si el contacte amb aquest [id] està marcat com a favorit. */
    fun esFavorit(id: Int): Boolean = id in favorits

    /** Alterna l'estat de favorit del contacte [id] i retorna el nou estat. */
    fun alternaFavorit(id: Int): Boolean {
        return if (id in favorits) {
            favorits.remove(id)
            false
        } else {
            favorits.add(id)
            true
        }
    }

    /** Filtra la llista pel nom, la ciutat o el correu electrònic (sense distingir majúscules). */
    fun cerca(texte: String): List<Contacte> {
        val consulta = texte.trim()
        if (consulta.isEmpty()) return llista
        return contactes.filter {
            it.nom.contains(consulta, ignoreCase = true) ||
                it.ciutat.contains(consulta, ignoreCase = true) ||
                it.email.contains(consulta, ignoreCase = true)
        }
    }

    /** Desa la URI d'una foto triada de la galeria per al contacte [id]. */
    fun desaFoto(id: Int, uri: Uri) {
        fotos[id] = uri
    }

    /** Retorna la URI de la foto personalitzada del contacte [id], o `null` si no n'hi ha. */
    fun foto(id: Int): Uri? = fotos[id]
}
