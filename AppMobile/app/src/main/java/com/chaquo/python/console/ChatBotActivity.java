package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.model.KetQuaPhanTich;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ChatAdapter adapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String, Object> userData = new HashMap<>();
    private volatile boolean isPythonReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Khởi tạo Python trong luồng phụ
        executor.execute(() -> {
            try {
                if (!Python.isStarted()) {
                    Python.start(new AndroidPlatform(this));
                }
                isPythonReady = true;
            } catch (Exception e) {
                Log.e("ChatBot", "Python Init Error: " + e.getMessage());
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        FloatingActionButton btnSendMessage = findViewById(R.id.btnSendMessage);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        loadUserFirestoreData();

        addBotMessage("Chào bạn! Tôi là **Trợ lý AI hướng nghiệp**. Tôi đã sẵn sàng hỗ trợ bạn.");

        btnSendMessage.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                if (!isPythonReady) {
                    Toast.makeText(this, "AI đang khởi động...", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void loadUserFirestoreData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("KetQuaPhanTich")
                .whereEqualTo("MaNguoiDung", uid)
                .orderBy("NgayThucHien", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        KetQuaPhanTich result = queryDocumentSnapshots.getDocuments().get(0).toObject(KetQuaPhanTich.class);
                        if (result != null) {
                            userData.put("test_results", result.getKetQuaChiTiet());
                            userData.put("predicted_career", result.getMaNganhPhuHop());
                        }
                    }
                });

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userData.put("profile", doc.getData());
                    }
                });
    }

    private void sendMessage(String text) {
        messageList.add(new Message(text, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);

        addBotMessage("Đang suy nghĩ...");
        final int loadingIndex = messageList.size() - 1;

        // DI CHUYỂN TẤT CẢ XỬ LÝ NẶNG VÀO LUỒNG PHỤ
        executor.execute(() -> {
            try {
                // 1. Chuyển đổi dữ liệu sang JSON (chạy ở luồng phụ để tránh treo UI)
                String userDataJson = new Gson().toJson(userData);

                List<Map<String, Object>> history = new ArrayList<>();
                int start = Math.max(0, messageList.size() - 12);
                for (int i = start; i < messageList.size() - 2; i++) {
                    Message m = messageList.get(i);
                    if (m.text.contains("suy nghĩ")) continue;
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("role", m.isUser ? "user" : "model");
                    List<Map<String, String>> parts = new ArrayList<>();
                    Map<String, String> part = new HashMap<>();
                    part.put("text", m.text);
                    parts.add(part);
                    entry.put("parts", parts);
                    history.add(entry);
                }
                String historyJson = new Gson().toJson(history);

                // 2. Gọi Python
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("chatbot_logic");
                String botResponse = pyModule.callAttr("get_bot_response", text, userDataJson, historyJson).toString();

                // 3. Cập nhật UI
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        messageList.get(loadingIndex).text = botResponse;
                        adapter.notifyItemChanged(loadingIndex);
                        rvChat.smoothScrollToPosition(loadingIndex);
                    }
                });

            } catch (Exception e) {
                Log.e("ChatBotError", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        messageList.get(loadingIndex).text = "Lỗi hệ thống AI. Vui lòng thử lại sau.";
                        adapter.notifyItemChanged(loadingIndex);
                    }
                });
            }
        });
    }

    private void addBotMessage(String response) {
        messageList.add(new Message(response, false));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
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
            int layoutId = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_bot;
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new ChatViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            String rawText = messages.get(position).text;
            String formattedText = rawText
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replaceAll("(?m)^\\s*\\*\\s+", "• ")
                .replace("\n", "<br>");
            holder.tvMessage.setText(HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY));
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
