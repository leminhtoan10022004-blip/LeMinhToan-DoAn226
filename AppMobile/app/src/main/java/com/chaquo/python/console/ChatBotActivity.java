package com.chaquo.python.console;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private FloatingActionButton btnSendMessage;
    private ChatAdapter adapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        messageList.add(new Message("Chào bạn! Tôi là trợ lý AI hướng nghiệp. Tôi có thể giúp gì cho bạn hôm nay?", false));
        adapter.notifyDataSetChanged();

        btnSendMessage.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void sendMessage(String text) {
        messageList.add(new Message(text, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);

        rvChat.postDelayed(() -> {
            String response = getBotResponse(text);
            messageList.add(new Message(response, false));
            adapter.notifyItemInserted(messageList.size() - 1);
            rvChat.scrollToPosition(messageList.size() - 1);
        }, 1000);
    }

    private String getBotResponse(String userText) {
        userText = userText.toLowerCase();
        if (userText.contains("nghề") || userText.contains("việc")) {
            return "Bạn nên hoàn thành các bài trắc nghiệm MBTI và Holland để tôi có cơ sở tư vấn chính xác nhất nhé!";
        } else if (userText.contains("mbti")) {
            return "Bài test MBTI giúp bạn hiểu về tính cách cốt lõi, từ đó chọn môi trường làm việc phù hợp.";
        } else if (userText.contains("lương")) {
            return "Mức lương phụ thuộc vào kỹ năng và kinh nghiệm. Bạn có thể xem chi tiết trong mục 'Thông tin nghề nghiệp'.";
        }
        return "Xin lỗi, tôi chưa hiểu ý bạn. Bạn có thể hỏi về nghề nghiệp, các bài test hoặc lộ trình phát triển được không?";
    }

    static class Message {
        String text;
        boolean isUser;
        Message(String text, boolean isUser) { this.text = text; this.isUser = isUser; }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        List<Message> messages;
        ChatAdapter(List<Message> messages) { this.messages = messages; }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                viewType == 1 ? R.layout.item_chat_user : R.layout.item_chat_bot, parent, false);
            return new ChatViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            holder.tvMessage.setText(messages.get(position).text);
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isUser ? 1 : 0;
        }

        @Override
        public int getItemCount() { return messages.size(); }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            ChatViewHolder(View v) { super(v); tvMessage = v.findViewById(R.id.tvMessage); }
        }
    }
}
