package cat.institut.dam.contactes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Pantalla de detall d'un contacte amb la seva foto, dades i botons d'accions
 * mitjançant Intents implícits.
 */
class ContacteDetallActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        private const val REQUEST_FOTO = 200
    }

    private lateinit var ivFoto: ImageView
    private var contacteId: Int = -1
    private val handlerPrincipal = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall)
        Finestres.aplica(this, findViewById(R.id.arrel))

        contacteId = intent.getIntExtra(EXTRA_ID, -1)
        val contacte = ContactesRepository.contacte(contacteId)
        if (contacte == null) {
            Toast.makeText(this, R.string.error_contacte, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ivFoto = findViewById(R.id.ivFoto)
        findViewById<TextView>(R.id.tvNom).text = contacte.nom
        findViewById<TextView>(R.id.tvEmail).text = contacte.email
        findViewById<TextView>(R.id.tvTelefon).text = contacte.telefon
        findViewById<TextView>(R.id.tvAdreca).text = contacte.adrecaCompleta
        findViewById<TextView>(R.id.tvWeb).text = contacte.web

        val chkFavorit = findViewById<CheckBox>(R.id.chkFavorit)
        chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
        chkFavorit.setOnCheckedChangeListener { _, _ ->
            ContactesRepository.alternaFavorit(contacte.id)
        }

        carregaImatge(contacte)
        configuraBotons(contacte)
    }

    private fun carregaImatge(contacte: Contacte) {
        val fotoDesada = ContactesRepository.foto(contacte.id)
        if (fotoDesada != null) {
            ivFoto.setImageURI(fotoDesada)
            return
        }

        ivFoto.setImageResource(R.drawable.ic_contacte)
        Thread {
            val bitmap = ContactesService.baixaImatge(contacte.urlAvatar)
            if (bitmap != null) {
                handlerPrincipal.post {
                    if (!isFinishing && !isDestroyed && ContactesRepository.foto(contacte.id) == null) {
                        ivFoto.setImageBitmap(bitmap)
                    }
                }
            }
        }.start()
    }

    private fun configuraBotons(contacte: Contacte) {
        findViewById<Button>(R.id.btnTrucar).setOnClickListener {
            obreIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacte.telefon}")))
        }

        findViewById<Button>(R.id.btnCorreu).setOnClickListener {
            obreIntent(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contacte.email}")))
        }

        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            val uriGeo = Uri.parse(
                "geo:${contacte.latitud},${contacte.longitud}" +
                    "?q=${contacte.latitud},${contacte.longitud}(${Uri.encode(contacte.nom)})"
            )
            obreIntent(Intent(Intent.ACTION_VIEW, uriGeo))
        }

        findViewById<Button>(R.id.btnWeb).setOnClickListener {
            var web = contacte.web.trim()
            if (!web.startsWith("http://") && !web.startsWith("https://")) {
                web = "https://$web"
            }
            obreIntent(Intent(Intent.ACTION_VIEW, Uri.parse(web)))
        }

        findViewById<Button>(R.id.btnAlarma).setOnClickListener {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, 9)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarma_missatge, contacte.nom))
            }
            obreIntent(intent)
        }

        findViewById<Button>(R.id.btnGaleria).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_FOTO)
        }
    }

    private fun obreIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_app_no_disponible, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_FOTO || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Alguns selectors de documents no concedeixen permís persistent.
        }
        ContactesRepository.desaFoto(contacteId, uri)
        ivFoto.setImageURI(uri)
    }
}
