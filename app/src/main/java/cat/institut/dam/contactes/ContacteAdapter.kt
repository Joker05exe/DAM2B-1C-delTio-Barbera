package cat.institut.dam.contactes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter del [RecyclerView] per mostrar la llista de contactes.
 */
class ContacteAdapter(
    private val onContacteClick: (Contacte) -> Unit,
    private val onFavoritClick: (Contacte) -> Unit
) : RecyclerView.Adapter<ContacteAdapter.ContacteViewHolder>() {

    private val contactes = mutableListOf<Contacte>()

    /** Actualitza els elements visibles de la llista i refresca el [RecyclerView]. */
    fun actualitza(novs: List<Contacte>) {
        contactes.clear()
        contactes.addAll(novs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContacteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacte, parent, false)
        return ContacteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ContacteViewHolder, position: Int) {
        holder.bind(contactes[position])
    }

    override fun getItemCount(): Int = contactes.size

    /** ViewHolder d'una fila de contacte. */
    inner class ContacteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInicials: TextView = itemView.findViewById(R.id.tvInicials)
        private val tvNom: TextView = itemView.findViewById(R.id.tvNom)
        private val tvCiutat: TextView = itemView.findViewById(R.id.tvCiutat)
        private val tvCorreu: TextView = itemView.findViewById(R.id.tvCorreu)
        private val chkFavorit: CheckBox = itemView.findViewById(R.id.chkFavorit)

        fun bind(contacte: Contacte) {
            tvInicials.text = contacte.inicials
            tvNom.text = contacte.nom
            tvCiutat.text = contacte.ciutat
            tvCorreu.text = contacte.email

            // Desconnectem el listener abans d'assignar isChecked per evitar crides falses en reciclar.
            chkFavorit.setOnCheckedChangeListener(null)
            chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
            chkFavorit.setOnCheckedChangeListener { _, _ ->
                onFavoritClick(contacte)
            }

            itemView.setOnClickListener {
                onContacteClick(contacte)
            }
        }
    }
}
