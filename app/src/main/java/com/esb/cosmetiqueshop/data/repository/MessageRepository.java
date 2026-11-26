package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.MessageDao;
import com.esb.cosmetiqueshop.data.dao.UserDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.Conversation;
import com.esb.cosmetiqueshop.model.Message;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.List;

public class MessageRepository {

    public interface ConversationsCallback {
        void onResult(List<Conversation> conversations);
    }

    public interface MessagesCallback {
        void onResult(List<Message> messages);
    }

    public interface LongCallback {
        void onResult(long id);
    }

    private final MessageDao messageDao;
    private final UserDao userDao;
    private final Context appContext;
    private final AppExecutors executors;

    public MessageRepository(Context context) {
        appContext = context.getApplicationContext();
        DatabaseHelper helper = new DatabaseHelper(context);
        messageDao = new MessageDao(helper);
        userDao = new UserDao(helper);
        executors = AppExecutors.getInstance();
    }

    public void listForClient(long clientUserId, ConversationsCallback callback) {
        executors.diskIO().execute(() -> {
            List<Conversation> list = messageDao.findConversationsForClient(clientUserId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void listAllAdmin(ConversationsCallback callback) {
        executors.diskIO().execute(() -> {
            List<Conversation> list = messageDao.findAllConversations();
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getMessages(long conversationId, MessagesCallback callback) {
        executors.diskIO().execute(() -> {
            List<Message> list = messageDao.findMessages(conversationId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void startConversation(long clientUserId, Long adminUserId, String subject, LongCallback callback) {
        executors.diskIO().execute(() -> {
            long id = messageDao.createConversation(clientUserId, adminUserId, subject);
            executors.mainThread().execute(() -> callback.onResult(id));
        });
    }

    public void send(long conversationId, long senderUserId, String body, LongCallback callback) {
        executors.diskIO().execute(() -> {
            long id = messageDao.sendMessage(conversationId, senderUserId, body);
            Conversation conv = messageDao.findConversationById(conversationId);
            User sender = userDao.findById(senderUserId);
            String preview = body.length() > 80 ? body.substring(0, 80) + "…" : body;
            NotificationRepository notif = new NotificationRepository(appContext);
            if (sender != null && sender.getRole() == UserRole.CLIENT) {
                notif.notifyAdminsMessage(preview, conversationId);
            } else if (conv != null) {
                notif.notifyMessage(conv.getClientUserId(), preview, conversationId);
            }
            executors.mainThread().execute(() -> callback.onResult(id));
        });
    }
}