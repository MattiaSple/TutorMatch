package com.example.tutormatch.ui.view.fragment

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentChatDetailBinding
import com.example.tutormatch.ui.adapter.MessageAdapter
import com.example.tutormatch.ui.viewmodel.ChatDetailViewModel
import com.example.tutormatch.ui.viewmodel.SharedViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.ViewTreeObserver

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatDetailViewModel: ChatDetailViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Listener del layout, salvato per poterlo rimuovere quando il fragment viene distrutto
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatDetailViewModel = ViewModelProvider(this).get(ChatDetailViewModel::class.java)

        binding.viewModel = chatDetailViewModel
        binding.lifecycleOwner = this

        val adapter = MessageAdapter(emptyList())

        // Aggiungi il LayoutManager qui
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMessages.adapter = adapter

        // Osserva i messaggi aggiornati e scrolla alla fine
        chatDetailViewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            // Prima di aggiornare l'adapter, ordina i messaggi per timestamp
            val sortedMessages = messages.sortedBy { it.timestamp }

            // Aggiorna l'adapter con i messaggi ordinati
            adapter.updateData(sortedMessages)

            // Scorri automaticamente alla fine dei messaggi, solo se il binding esiste ancora
            _binding?.recyclerViewMessages?.scrollToPosition(sortedMessages.size - 1)
        })

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

        sharedViewModel.chatId.observe(viewLifecycleOwner, Observer { chatId ->
            if (!chatId.isNullOrEmpty()) {
                chatDetailViewModel.setChatId(chatId)
            } else {
                Log.e("ChatDetailFragment", "Received chatId is null or empty")
            }
        })

        // Aggiungi il listener per il pulsante di back
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

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
