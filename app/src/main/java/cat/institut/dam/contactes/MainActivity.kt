package cat.institut.dam.contactes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * MainActivity — la llista de contactes.
 *
 * Carrega els contactes des de la xarxa (o des de la còpia local si no hi ha
 * connexió), permet filtrar-los, marcar favorits, obrir el detall i mostrar
 * una notificació un cop carregats.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /** Codi de la petició de permís de notificacions. */
        private const val REQUEST_NOTIFICACIONS = 100
    }

    private lateinit var etCerca: EditText
    private lateinit var btnActualitza: Button
    private lateinit var pbCarga: ProgressBar
    private lateinit var txtBuit: TextView
    private lateinit var txtAvis: TextView
    private lateinit var rvContactes: RecyclerView
    private lateinit var adapter: ContacteAdapter

    private val handlerPrincipal = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Finestres.aplica(this, findViewById<View>(R.id.arrel))

        etCerca = findViewById(R.id.etCerca)
        btnActualitza = findViewById(R.id.btnActualitza)
        pbCarga = findViewById(R.id.pbCarga)
        txtBuit = findViewById(R.id.txtBuit)
        txtAvis = findViewById(R.id.txtAvis)
        rvContactes = findViewById(R.id.rvContactes)

        adapter = ContacteAdapter(
            onContacteClick = { contacte ->
                val intent = Intent(this, ContacteDetallActivity::class.java).apply {
                    putExtra(ContacteDetallActivity.EXTRA_ID, contacte.id)
                }
                startActivity(intent)
            },
            onFavoritClick = { contacte ->
                ContactesRepository.alternaFavorit(contacte.id)
            }
        )
        rvContactes.layoutManager = LinearLayoutManager(this)
        rvContactes.adapter = adapter

        etCerca.doAfterTextChanged {
            mostraLlista()
        }

        btnActualitza.setOnClickListener {
            carrega()
        }

        carrega()
    }

    override fun onResume() {
        super.onResume()
        if (ContactesRepository.llista.isNotEmpty()) {
            mostraLlista()
        }
    }

    private fun mostraLlista() {
        val consulta = etCerca.text.toString()
        val filtrats = ContactesRepository.cerca(consulta)
        adapter.actualitza(filtrats)

        if (filtrats.isEmpty()) {
            txtBuit.setText(
                if (consulta.isBlank()) R.string.txt_buit else R.string.txt_buit_cerca
            )
            txtBuit.visibility = View.VISIBLE
            rvContactes.visibility = View.GONE
        } else {
            txtBuit.visibility = View.GONE
            rvContactes.visibility = View.VISIBLE
        }
    }

    /**
     * Carrega els contactes en un fil de segon pla i actualitza la interfície
     * al fil principal. Si no hi ha xarxa, recorre a `assets/contactes.json`.
     */
    private fun carrega() {
        pbCarga.visibility = View.VISIBLE
        btnActualitza.isEnabled = false
        txtAvis.visibility = View.GONE

        Thread {
            var usantLocal = false
            val resultat = try {
                ContactesService.desDeXarxa()
            } catch (_: Exception) {
                usantLocal = true
                try {
                    ContactesService.desDeRecurs(this)
                } catch (_: Exception) {
                    null
                }
            }

            handlerPrincipal.post {
                if (isFinishing || isDestroyed) return@post
                pbCarga.visibility = View.GONE
                btnActualitza.isEnabled = true

                if (resultat == null) {
                    Toast.makeText(this, R.string.error_carrega, Toast.LENGTH_LONG).show()
                    mostraLlista()
                    return@post
                }

                ContactesRepository.actualitza(resultat)
                if (usantLocal) {
                    txtAvis.setText(R.string.avis_dades_locals)
                    txtAvis.visibility = View.VISIBLE
                }
                mostraLlista()

                if (tePermisNotificacions()) {
                    Notificacions.mostraContactesCarregats(this, resultat.size)
                } else {
                    demanaPermisNotificacions()
                }
            }
        }.start()
    }

    private fun tePermisNotificacions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * A partir d'Android 13 (API 33) cal demanar permís en temps d'execució
     * per poder mostrar notificacions. En versions anteriors el permís no
     * existeix i no cal demanar-lo.
     */
    private fun demanaPermisNotificacions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permissio = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permissio) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permissio), REQUEST_NOTIFICACIONS)
    }

    /**
     * Resposta de l'usuari a la petició de permís: si el denega, avisa'l amb
     * un Toast; si el concedeix, mostra la notificació.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICACIONS) {
            val concedit = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (concedit) {
                Notificacions.mostraContactesCarregats(this, ContactesRepository.llista.size)
            } else {
                Toast.makeText(this, R.string.permis_denegada, Toast.LENGTH_LONG).show()
            }
        }
    }
}
