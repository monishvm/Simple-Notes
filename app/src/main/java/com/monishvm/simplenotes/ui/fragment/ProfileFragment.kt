package com.monishvm.simplenotes.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.monishvm.simplenotes.R
import com.monishvm.simplenotes.ui.LoginActivity
import com.monishvm.simplenotes.ui.MainActivity
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity?)?.hideFloatingButton()
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (doesHaveImage())
            Picasso.with(context).load(LoginActivity.authAccount?.avatarUri)
                .placeholder(R.drawable.ic_profile)
                .into(view.findViewById<ImageView>(R.id.profile_image))

        view.findViewById<TextView>(R.id.nameText).text = (LoginActivity.authAccount?.givenName)
        view.findViewById<TextView>(R.id.emailText).text = (LoginActivity.authAccount?.email)

        view.findViewById<Button>(R.id.signoutBtn).setOnClickListener {
            signOut()
        }

        return view;
    }

    private fun doesHaveImage(): Boolean {
        return LoginActivity.authAccount?.avatarUri.toString() != ""
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showFloatingButton()
    }

    private fun signOut() {
        val signOutTask = LoginActivity.service.signOut()
        signOutTask.addOnCompleteListener {
            // Processing after the sign-out.
            Log.i(tag, "signOut complete")
            startActivity(
                Intent(
                    context,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra("signOut", true)
            )
        }
        signOutTask.addOnFailureListener {
            Toast.makeText(context, "Cannot SignOut", Toast.LENGTH_LONG).show()
        }
    }
}