package com.monishvm.simplenotes.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.monishvm.simplenotes.R

class LoginActivity : AppCompatActivity() {

    private lateinit var authBtn: com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton
    private lateinit var progressBar: ProgressBar

    companion object {
        var authAccount: AuthAccount? = null
        lateinit var authParams: AccountAuthParams
        lateinit var service: AccountAuthService
        private const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authBtn = findViewById(R.id.huaweiAuthBtn)
        progressBar = findViewById(R.id.progressBar)

        hideButtonAndShowProgressBar()


        if (!intent.getBooleanExtra("signOut", false)) {
            trySilentSignIn()
        } else {
            showButtonAndHideProgressBar()
        }

        authBtn.setOnClickListener {
            trySilentSignIn()
            hideButtonAndShowProgressBar()
        }
    }

    private fun showButtonAndHideProgressBar() {
        authBtn.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

    private fun hideButtonAndShowProgressBar() {
        authBtn.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    private fun trySilentSignIn() {
        authParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setProfile()
            .setEmail()
            .setAccessToken()
            .setAuthorizationCode()
            .createParams()
        service = AccountAuthManager.getService(this, authParams)

        val task: Task<AuthAccount> = service.silentSignIn()

        task.addOnSuccessListener {
            authAccount = it
            moveToNextScreen()
        }
        task.addOnFailureListener {
            getSignedIn()
        }
    }

    private fun getSignedIn() {
        startActivityForResult(service.signInIntent, 8888)
    }

    private fun moveToNextScreen() {
        val i: Intent = Intent(
            this, MainActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 8888) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                authAccount = authAccountTask.result
                Log.i(TAG, "serverAuthCode:" + authAccount?.authorizationCode)
                moveToNextScreen()
            } else {
                showButtonAndHideProgressBar()
                Log.e(
                    TAG,
                    "sign in failed:" + (authAccountTask.exception as ApiException).statusCode
                )
            }
        }
    }
}