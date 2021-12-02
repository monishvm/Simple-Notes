package com.monishvm.simplenotes.ui.fragment

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monishvm.simplenotes.R
import com.monishvm.simplenotes.persistence.Note
import com.monishvm.simplenotes.ui.MainActivity
import com.monishvm.simplenotes.ui.NoteViewModel
import com.monishvm.simplenotes.ui.adapter.NoteAdapter
import com.monishvm.simplenotes.util.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_list.*
import javax.inject.Inject

class ListFragment : DaggerFragment(),
    NoteAdapter.Interaction {

    private lateinit var noteAdapter: NoteAdapter

    private lateinit var noteViewModel: NoteViewModel

    @Inject
    lateinit var viewmodelProviderFactory: ViewModelProviderFactory

    lateinit var allNotes: List<Note>


    // Method #1
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        allNotes = mutableListOf()

        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    // Method #2
    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()    // Step 1
        initRecyclerView()  // Step 2
        observerLiveData()  // Step 3
    }

    // Method #3
    private fun observerLiveData() {
        noteViewModel.getAllNotes().observe(viewLifecycleOwner, Observer { lisOfNotes ->
            lisOfNotes?.let {
                if (it.isNotEmpty()) {
                    noNotes.visibility = View.INVISIBLE
                    recyclerView.visibility = View.VISIBLE
                    allNotes = it
                    noteAdapter.swap(it)
                } else {
                    noNotes.visibility = View.VISIBLE
                    recyclerView.visibility = View.INVISIBLE
                }
            }
        })
    }

    // Method #4
    private fun initRecyclerView() {
        recyclerView.apply {
            noteAdapter = NoteAdapter(
                allNotes,
                this@ListFragment
            )
            layoutManager = LinearLayoutManager(this@ListFragment.context)
            adapter = noteAdapter
            val swipe = ItemTouchHelper(initSwipeToDelete())
            swipe.attachToRecyclerView(recyclerView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_ic -> {
                (activity as MainActivity).hideFloatingButton()
                this.findNavController()
                    .navigate(ListFragmentDirections.actionListFragmentToProfileFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Method #5
    private fun setupViewModel() {
        noteViewModel =
            ViewModelProvider(this, viewmodelProviderFactory).get(NoteViewModel::class.java)
    }

    // Method #6
    private fun initSwipeToDelete(): ItemTouchHelper.SimpleCallback {
        // Swipe recycler view items on RIGHT
        return object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                noteViewModel.delete(allNotes.get(position))
                Toast.makeText(activity, "Note Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method #7
    override fun onItemSelected(position: Int, item: Note) {
        (activity as MainActivity).hideFloatingButton()
        val navDirection = ListFragmentDirections.actionListFragmentToEditFragment(item)
        this.findNavController().navigate(navDirection)
    }
}


