package com.monishvm.simplenotes.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.monishvm.simplenotes.MLTextRecognition
import com.monishvm.simplenotes.R
import com.monishvm.simplenotes.persistence.Note
import com.monishvm.simplenotes.ui.MainActivity
import com.monishvm.simplenotes.ui.NoteViewModel
import com.monishvm.simplenotes.util.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_edit.*
import javax.inject.Inject


class EditFragment : DaggerFragment() {

    @Inject
    lateinit var viewmodelProviderFactory: ViewModelProviderFactory

    lateinit var noteViewModel: NoteViewModel

    lateinit var previousNote: Note

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity?)?.hideFloatingButton()

        arguments.let {
            previousNote = EditFragmentArgs.fromBundle(it!!).note!!
        }

        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    // Method #1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.title = previousNote.title

        prepareNoteForEditing()
        setupViewModel()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_save -> {
                findNavController().popBackStack()
            }
            R.id.option_scan -> {
                openGallery()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 1)
    }

    // Method #2
    private fun saveNoteToDatabase() {



        if (validations()) {
            Toast.makeText(activity, "Note is saved", Toast.LENGTH_SHORT).show()
            saveNote()
            val id: Int = EditFragmentArgs.fromBundle(arguments!!).note?.id!!
            Log.e("DEBUG", "saving note $id")

        } else {
//            Toast.makeText(activity, "Note is Discarded", Toast.LENGTH_SHORT).show()
            //Delete the note if all fields are empty (this is done by user)
            val id: Int = EditFragmentArgs.fromBundle(arguments!!).note?.id!!
            noteViewModel.deleteById(id)
            Log.e("DEBUG", "deleting note")
        }
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Method #3
    override fun onDestroyView() {
        hideKeyboard(activity!!)
        super.onDestroyView()
        if (previousNote.title != editTitle.text.trim().toString()
            || previousNote.description != editDescription.text.trim().toString()
        )
            saveNoteToDatabase()
        (activity as MainActivity?)?.showFloatingButton()
    }

    // Method #4
    private fun saveNote() {

        //getting the id from bundle , we are using that id to update/edit the note
        val id: Int? = EditFragmentArgs.fromBundle(arguments!!).note?.id

        val note =
            Note(id!!, editTitle.text.toString().trim(), editDescription.text.toString().trim())

        //If title is null set Empty Title
        if (editTitle.text.isNullOrEmpty()) {
            note.title = "Empty Title"

            //Call viewmodel to save the data
            noteViewModel.update(note)

        } else {
            //Call viewmodel to save the data
            Log.e("DEBUG", "saving note update is called")
            noteViewModel.update(note)
        }
    }

    // Method #5
    private fun validations(): Boolean {
        return !(editTitle.text.trim().isEmpty()
                && editDescription.text.trim().isEmpty()
                )
    }


    // Method #6
    private fun setupViewModel() {
        noteViewModel =
            ViewModelProvider(this, viewmodelProviderFactory).get(NoteViewModel::class.java)
    }


    // Method #7
    private fun prepareNoteForEditing() {
        // Getting the note from the bundle
        //Save args plugin is used as i believe bundle is not good for sending large data
        arguments?.let {
            val safeArgs = EditFragmentArgs.fromBundle(it)
            val note = safeArgs.note
            editTitle.setText(note?.title.toString())
            editDescription.setText(note?.description.toString())
        }
    }

    private fun onDetectionSuccess(detectedString: String?) {
        var oldDesc = editDescription.text.toString()
        if (oldDesc != "") oldDesc += "\n\n"
        val newDesc =
            "$oldDesc<- Recognised From Image ->\n$detectedString"
        editDescription.setText(newDesc)
    }

    private fun onDetectionFailure() {
        Toast.makeText(requireContext(), "Cannot Detect Text", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            val imageUri = data?.data
            val srcBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)

            MLTextRecognition.detectImage(srcBitmap).addOnSuccessListener {
                if (it.stringValue.trim().isNotEmpty())
                    onDetectionSuccess(it.stringValue.trim())
                else
                    onDetectionFailure()
            }.addOnFailureListener {
                onDetectionFailure()
            }
        }
    }
}

