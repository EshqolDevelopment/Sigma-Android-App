package com.eshqol.sigma
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth

class Login : Activity() {
    private lateinit var auth: FirebaseAuth
    private var code = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth

        val uri = intent.data
        if (uri != null){
            code = uri.toString().split("www.eshqol.com/sigma/")[1]
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val login = findViewById<Button>(R.id.login_)
        val register = findViewById<Button>(R.id.register_)
        val email = findViewById<TextInputEditText>(R.id.inputUsername)
        val password = findViewById<TextInputEditText>(R.id.inputPassword)
        val error = findViewById<TextView>(R.id.error_message)

        val googleSignIn = findViewById<SignInButton>(R.id.sign_in_button)

        googleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 9001)
        }

        register.setOnClickListener {
            val Email = email.text.toString().lowercase()
            val Password = password.text.toString()
            if (Email == "" || Password == "") {
            error.text = "Please fill all the text required fields"

            } else if (Email.contains("@") || Email.contains(".") || Email.contains("_") || Email.contains("$") || Email.contains("#") || Email.contains("[")  || Email.contains("]")) {
                error.text = "Username must not contain '@', '#' , '.' , '_'"

            } else if (Email.contains(" ")) {
                error.text = "Username must not contain whitespaces"

            } else if (Email.length > 11) {
                error.text = "Username is too long"

            } else {
                createAccount(email = "$Email@sigma.com", password = Password)
            }
        }

        login.setOnClickListener {
            val Email = email.text.toString().lowercase()
            val Password = password.text.toString()
            if (Email == "" || Password == "") {
                error.text = "Please fill all the text required fields"

            } else {
                signIn(email = "$Email@sigma.com", password = Password)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (_: ApiException) { }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun firebaseAuthWithGoogle(idToken: String) {
        val error = findViewById<TextView>(R.id.error_message)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    updateUI(user)
                    if (task.result.additionalUserInfo?.isNewUser == true){
                        DataBase().onCreate(Helpers().getUsername(), getCountryCode())
                        setProfile((1..12).random())
                    }
                    intent()
                } else {
                    error.text = task.exception.toString().split(":")[1]
                    updateUI(null)
                }
            }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            intent()
        }
        updateUI(currentUser)
    }

    @SuppressLint("SetTextI18n")
    private fun createAccount(email: String, password: String) {
        val error = findViewById<TextView>(R.id.error_message)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUI(user)
                DataBase().onCreate(email.split("@")[0], getCountryCode())
                setProfile((1..12).random())
                intent()
            } else {
                val exception = task.exception.toString()
                if (exception == "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.") {
                    error.text = "Username is already in use"

                } else {
                    try {
                        error.text = exception.split(":")[1]
                    }catch (_:Exception){
                    }
                    updateUI(null)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun signIn(email: String, password: String) {
        val error = findViewById<TextView>(R.id.error_message)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                    intent()
                } else {
                    val exception = task.exception.toString()
                    if (exception == "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.") {
                        error.text = "The password or the username is invalid"
                    } else {
                        try{
                            val err = exception.split(":")[1]
                            if (err.trim() == "There is no user record corresponding to this identifier. The user may have been deleted.")
                                error.text = "The password or the username is invalid"
                            else{
                                error.text = err
                            }
                        } catch (_: Exception){}
                    }
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {}

    private fun intent() {
        val switchActivityIntent = Intent(this, HomeScreen::class.java)
        switchActivityIntent.putExtra("linkCode", code)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun setProfile(imageId: Int) {
        val username = Helpers().getUsername()
        DataBase().updateProfile(username, imageId)

        val sharedPreference = getSharedPreferences("SaveLocal", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("profileId", imageId.toString())
        editor.apply()
    }

    private fun getCountryCode(): String {
        val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return manager.simCountryIso.uppercase()
    }
}