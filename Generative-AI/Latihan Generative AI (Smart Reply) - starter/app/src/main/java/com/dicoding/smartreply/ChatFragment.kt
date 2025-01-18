package com.dicoding.smartreply

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.smartreply.databinding.FragmentChatBinding
import java.util.Calendar

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar as Toolbar)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupRecyclerViews()

        binding.btnSwitchUser.setOnClickListener { chatViewModel.switchUser() }
        binding.btnSend.setOnClickListener {
            val input = binding.tietInputTextEditText.text.toString()
            if (input.isNotEmpty()) {
                chatViewModel.addMessages(input)
                clearInputAndHideKeyboard(it)
            }
        }

        binding.rvChatHistory.setOnTouchListener { v, _ ->
            hideKeyboard(v)
            false
        }

        binding.rvSmartReplyOptions.setOnClickListener { v ->
            hideKeyboard(v)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chat_menu_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.generateBasicChatHistory -> {
                generateBasicChatHistory()
                true
            }
            R.id.generateSensitiveChatHistory -> {
                generateSensitiveChatHistory()
                true
            }
            R.id.clearChatHistory -> {
                chatViewModel.setMessages(ArrayList())
                true
            }
            else -> false
        }
    }

    private fun setupRecyclerViews() {
        binding.rvChatHistory.layoutManager = LinearLayoutManager(context)
        val chatAdapter = ChatHistoryAdapter()
        binding.rvChatHistory.adapter = chatAdapter

        binding.rvSmartReplyOptions.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val replyOptionsAdapter = ReplyOptionsAdapter(object : ReplyOptionsAdapter.OnItemClickCallback {
            override fun onOptionClicked(optionText: String) {
                binding.tietInputTextEditText.setText(optionText)
            }
        })
        binding.rvSmartReplyOptions.adapter = replyOptionsAdapter

        chatViewModel.chatHistory.observe(viewLifecycleOwner) { messages ->
            chatAdapter.setChatHistory(messages)
            if (messages.isNotEmpty()) binding.rvChatHistory.smoothScrollToPosition(messages.size - 1)
        }

        chatViewModel.smartReplyOptions.observe(viewLifecycleOwner) { options ->
            replyOptionsAdapter.setReplyOptions(options)
        }

        chatViewModel.pretendingAsAnotherUser.observe(viewLifecycleOwner) { isPretendingAsAnotherUser ->
            val text = if (isPretendingAsAnotherUser) R.string.chatting_as_evans else R.string.chatting_as_kai
            val color = if (isPretendingAsAnotherUser) R.color.red else R.color.blue
            binding.tvCurrentUser.setText(text)
            binding.tvCurrentUser.setTextColor(ContextCompat.getColor(requireContext(), color))
        }

        chatViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun initializeViewModel() {
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        if (chatViewModel.chatHistory.value == null) {
            chatViewModel.setMessages(arrayListOf(Message("Hello friend. How are you today?", false, System.currentTimeMillis())))
        }
    }

    private fun clearInputAndHideKeyboard(view: View) {
        binding.tietInputTextEditText.text?.clear()
        hideKeyboard(view)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun generateBasicChatHistory() {
        chatViewModel.setMessages(arrayListOf(
            Message("Hello!", false, Calendar.getInstance().timeInMillis - 2 * 60 * 1000),
            Message("Hi!", true, Calendar.getInstance().timeInMillis - 60 * 1000),
            Message("Good morning, how are you?", false, Calendar.getInstance().timeInMillis)
        ))
    }

    private fun generateSensitiveChatHistory() {
        chatViewModel.setMessages(arrayListOf(
            Message("Hello!", false, Calendar.getInstance().timeInMillis - 2 * 60 * 1000),
            Message("Hi!", true, Calendar.getInstance().timeInMillis - 60 * 1000),
            Message("Good morning, how are you?", false, Calendar.getInstance().timeInMillis),
            Message("My cat died", true, Calendar.getInstance().timeInMillis + 60 * 1000)
        ))
    }
}
