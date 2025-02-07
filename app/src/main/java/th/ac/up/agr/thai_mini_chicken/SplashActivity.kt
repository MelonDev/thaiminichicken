package th.ac.up.agr.thai_mini_chicken

import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.*
import th.ac.up.agr.thai_mini_chicken.Data.Information
import th.ac.up.agr.thai_mini_chicken.Firebase.Firebase
import th.ac.up.agr.thai_mini_chicken.Logic.Dialog.ActionDialog
import th.ac.up.agr.thai_mini_chicken.Logic.Dialog.AlertsDialog
import th.ac.up.agr.thai_mini_chicken.ProgramMainActivity.ProgramMainActivity
import th.ac.up.agr.thai_mini_chicken.Tools.MelonTheme


class SplashActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var delayTime: Long = 0
    private var time: Long = 1000L

    private lateinit var mFirebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(MelonTheme.from(this).getStyle())
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash)

        mFirebaseAuth = FirebaseAuth.getInstance()

        handler = Handler()

        runnable = Runnable {

            mFirebaseAuth.currentUser?.let {
                checkProcess()
            } ?: run {
                goToLogin()
            }

        }

    }


    private fun checkProcess() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val firebase = Firebase.reference.child("ผู้ใช้").child(user.uid).child("รายละเอียด")
            firebase.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                    AlertsDialog(this@SplashActivity).setTitle(R.string.request_has_cancel).build().show()
                }

                override fun onDataChange(p0: DataSnapshot) {

                    p0.getValue(Information::class.java)?.let { info ->
                        if (info.farmName.isEmpty() || info.username.isEmpty() || info.phoneNumber.isEmpty() || info.farmAddress.isEmpty()) {
                            ActionDialog(this@SplashActivity).setTitle(R.string.alert_message).setMessage(R.string.inital_information_question_message).positive(R.string.yes_message_response) {
                                newStartProcess()
                            }.negative(R.string.skip_message_response) {
                                val intent = Intent(this@SplashActivity, ProgramMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }.build().show()
                        } else {
                            val intent = Intent(this@SplashActivity, ProgramMainActivity::class.java)
                            startActivity(intent)
                            firebase.removeEventListener(this)
                            finish()
                        }
                    } ?: run {
                        goToLogin()
                    }

                }
            })
        } ?: run {
            goToLogin()
        }

    }

    private fun goToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun newStartProcess() {
        val intent = Intent(this@SplashActivity, RegisterInfoActivity::class.java)
        finish()
        intent.putExtra("ID", "0")
        startActivity(intent)
        finish()
    }


    override fun onResume() {
        super.onResume()
        delayTime = time
        handler.postDelayed(runnable, delayTime)
        time = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        time = delayTime - (System.currentTimeMillis() - time)
    }

}
