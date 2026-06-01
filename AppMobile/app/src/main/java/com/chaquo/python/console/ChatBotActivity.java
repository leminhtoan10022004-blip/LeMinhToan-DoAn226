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
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView rvChat, rvSuggestions;
    private EditText etMessage;
    private ChatAdapter chatAdapter;
    private SuggestionAdapter suggestionAdapter;
    private List<Message> messageList;
    private List<String> suggestionList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String, Object> userData = new HashMap<>();
    private volatile boolean isPythonReady = false;

    private boolean isRoadmapMode = false;
    private String jobDetailJson = "";
    private String roadmapStepsJson = "";
    private String resourcesJson = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // NHẬN DIỆN CHẾ ĐỘ THÔNG MINH
        jobDetailJson = getIntent().getStringExtra("jobDetailJson");
        roadmapStepsJson = getIntent().getStringExtra("roadmapStepsJson");
        resourcesJson = getIntent().getStringExtra("resourcesJson");
        
        if (jobDetailJson != null && !jobDetailJson.isEmpty()) {
            isRoadmapMode = true;
        } else {
            isRoadmapMode = getIntent().getBooleanExtra("isRoadmapMode", false);
        }

        initViews();
        setupPython();
        loadUserFirestoreData();

        if (isRoadmapMode) {
            addBotMessage("Chào bạn! Tôi là **Cố vấn Lộ trình AI**. Tôi có thể giúp gì cho bạn về nghề nghiệp này?");
            showStarterQuestions();
        } else {
            addBotMessage("Chào bạn! Tôi là **Trợ lý AI hướng nghiệp**. Tôi đã sẵn sàng hỗ trợ bạn.");
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            if (isRoadmapMode) {
                getSupportActionBar().setTitle("Cố vấn Lộ trình AI");
            }
        }

        rvChat = findViewById(R.id.rvChat);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        etMessage = findViewById(R.id.etMessage);
        FloatingActionButton btnSendMessage = findViewById(R.id.btnSendMessage);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        suggestionList = new ArrayList<>();
        suggestionAdapter = new SuggestionAdapter(suggestionList, text -> {
            etMessage.setText(""); 
            sendMessage(text);
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setAdapter(suggestionAdapter);

        btnSendMessage.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void showStarterQuestions() {
        try {
            JSONObject job = new JSONObject(jobDetailJson != null ? jobDetailJson : "{}");
            String name = job.optString("TenCongViec", "ngành này");
            suggestionList.clear();
            suggestionList.add("Tìm hiểu " + name + " là gì?");
            suggestionList.add("Lộ trình học " + name);
            suggestionList.add("Tài liệu hỗ trợ?");
            suggestionAdapter.notifyDataSetChanged();
            rvSuggestions.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e("ChatBot", "Error showing starters: " + e.getMessage());
        }
    }

    private void setupPython() {
        executor.execute(() -> {
            try {
                if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
                isPythonReady = true;

                // XỬ LÝ TIN NHẮN TỰ ĐỘNG NẾU CÓ
                String preset = getIntent().getStringExtra("PRESET_MESSAGE");
                if (preset != null && !preset.isEmpty()) {
                    runOnUiThread(() -> sendMessage(preset));
                }
            } catch (Exception e) {
                Log.e("ChatBot", "Python Init Error: " + e.getMessage());
            }
        });
    }

    private void loadUserFirestoreData() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) userData.put("profile", doc.getData());
        });
    }

    private void sendMessage(String text) {
        if (!isPythonReady) {
            Toast.makeText(this, "AI đang khởi động...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.isEmpty()) return;

        messageList.add(new Message(text, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        
        requestAiResponse(text);
    }

    private void requestAiResponse(String text) {
        if (!text.isEmpty()) {
            runOnUiThread(() -> rvSuggestions.setVisibility(View.GONE));
        }

        addBotMessage("Đang suy nghĩ...");
        final int loadingIndex = messageList.size() - 1;

        executor.execute(() -> {
            try {
                String userDataJson = new Gson().toJson(userData);
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("chatbot_logic");
                String botResponse;

                if (isRoadmapMode) {
                    botResponse = pyModule.callAttr("get_roadmap_advice", 
                            text, jobDetailJson, roadmapStepsJson, resourcesJson, userDataJson).toString();
                } else {
                    botResponse = pyModule.callAttr("get_bot_response", text, userDataJson, "[]").toString();
                }

                List<String> suggestions = extractSuggestions(botResponse);
                String cleanContent = botResponse.contains("🔍") ? botResponse.split("🔍")[0].trim() : botResponse.trim();

                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        messageList.get(loadingIndex).text = cleanContent;
                        chatAdapter.notifyItemChanged(loadingIndex);
                        rvChat.smoothScrollToPosition(loadingIndex);
                        
                        if (!suggestions.isEmpty()) {
                            suggestionList.clear();
                            suggestionList.addAll(suggestions);
                            suggestionAdapter.notifyDataSetChanged();
                            rvSuggestions.setVisibility(View.VISIBLE);
                        } else if (isRoadmapMode && text.isEmpty()) {
                            showStarterQuestions();
                        }
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    messageList.get(loadingIndex).text = "AI gặp chút sự cố, hãy thử lại nhé!";
                    chatAdapter.notifyItemChanged(loadingIndex);
                });
            }
        });
    }

    private List<String> extractSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String s = matcher.group(1).trim();
            if (s.length() > 3) suggestions.add(s);
        }
        return suggestions;
    }

    private void addBotMessage(String response) {
        messageList.add(new Message(response, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    static class Message {
        String text;
        boolean isUser;
        Message(String text, boolean isUser) { this.text = text; this.isUser = isUser; }
    }

    static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
        List<String> suggestions;
        OnItemClickListener listener;
        interface OnItemClickListener { void onClick(String text); }
        SuggestionAdapter(List<String> s, OnItemClickListener l) { suggestions = s; listener = l; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_suggestion_chip, p, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            h.chip.setText(suggestions.get(pos));
            h.chip.setOnClickListener(v -> listener.onClick(suggestions.get(pos)));
        }
        @Override public int getItemCount() { return suggestions.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            Chip chip;
            ViewHolder(View v) { super(v); chip = v.findViewById(R.id.chipSuggestion); }
        }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        List<Message> messages;
        ChatAdapter(List<Message> messages) { this.messages = messages; }
        @NonNull @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_bot;
            return new ChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            String rawText = messages.get(position).text;
            String formattedText = rawText
                .replaceAll("### (.*?)\n", "<h3>$1</h3>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replace("\n", "<br>");
            holder.tvMessage.setText(HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
        @Override public int getItemViewType(int pos) { return messages.get(pos).isUser ? 1 : 0; }
        @Override public int getItemCount() { return messages.size(); }
        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            ChatViewHolder(View v) { super(v); tvMessage = v.findViewById(R.id.tvMessage); }
        }
    }
}
