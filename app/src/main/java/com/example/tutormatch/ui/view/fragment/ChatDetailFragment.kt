package com.example.tutormatch.ui.view.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tutormatch.databinding.FragmentChatDetailBinding
import com.example.tutormatch.ui.adapter.MessageAdapter
import com.example.tutormatch.ui.viewmodel.ChatDetailViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProvider

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null // Binding per il layout del fragment
    private val binding get() = _binding!!

    // Usa il delegato per inizializzare il ViewModel
    private lateinit var chatDetailViewModel: ChatDetailViewModel

    // Listener del layout, salvato per poterlo rimuovere quando il fragment viene distrutto
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        chatDetailViewModel = ViewModelProvider(this).get(ChatDetailViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera chatId ed email passati tramite il bundle
        val chatId = arguments?.getString("chatId")

        if (!chatId.isNullOrEmpty()) {
            chatDetailViewModel.setChatId(chatId)  // Usa il chatId passato
        }

        binding.viewModel = chatDetailViewModel
        binding.lifecycleOwner = this

        val adapter = MessageAdapter(emptyList())

        // Aggiungi il LayoutManager qui
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMessages.adapter = adapter

        // Osserva i messaggi aggiornati e scrolla alla fine
        chatDetailViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.updateData(messages)
            _binding?.recyclerViewMessages?.scrollToPosition(messages.size - 1)
        }

        // Listener per il layout (controllo se la tastiera è visibile)
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            _binding?.let { binding ->
                binding.root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height

                // Calcoliamo l'altezza della tastiera
                val keypadHeight = screenHeight - rect.bottom

                // Se l'altezza della tastiera è superiore a 200 pixel, significa che la tastiera è visibile
                if (keypadHeight > 200) {
                    // Scorri alla fine della lista dei messaggi
                    binding.recyclerViewMessages.scrollToPosition(chatDetailViewModel.messages.value?.size?.minus(1) ?: 0)
                }
            }
        }

        // Aggiungi il listener al rootView
        _binding?.root?.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)

        // Listener per chiudere la tastiera se l'utente scorre
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // Se l'utente inizia a scorrere la chat, nascondi la tastiera
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    // funzione per nascondere la tastiera
    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Rimuovi il listener del layout quando il fragment viene distrutto
        globalLayoutListener?.let {
            _binding?.root?.viewTreeObserver?.removeOnGlobalLayoutListener(it)
        }
        _binding = null
    }
}

