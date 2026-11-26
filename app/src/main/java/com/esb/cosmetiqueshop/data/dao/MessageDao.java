package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Conversation;
import com.esb.cosmetiqueshop.model.Message;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    private final DatabaseHelper dbHelper;

    public MessageDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Conversation> findConversationsForClient(long clientUserId) {
        List<Conversation> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Conversations.TABLE, null,
                DbContract.Conversations.COL_CLIENT_USER_ID + "=?",
                new String[]{String.valueOf(clientUserId)}, null, null,
                DbContract.Conversations.COL_UPDATED_AT + " DESC")) {
            while (c.moveToNext()) {
                list.add(mapConversation(c));
            }
        }
        return list;
    }

    public List<Conversation> findAllConversations() {
        List<Conversation> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.*, u." + DbContract.Users.COL_FULL_NAME + " AS client_name, "
                + "u." + DbContract.Users.COL_EMAIL + " AS client_email, "
                + "(SELECT m." + DbContract.Messages.COL_BODY + " FROM " + DbContract.Messages.TABLE
                + " m WHERE m." + DbContract.Messages.COL_CONVERSATION_ID + " = c."
                + DbContract.Conversations.COL_ID + " ORDER BY m."
                + DbContract.Messages.COL_CREATED_AT + " DESC LIMIT 1) AS last_body, "
                + "(SELECT COUNT(*) FROM " + DbContract.Messages.TABLE + " m WHERE m."
                + DbContract.Messages.COL_CONVERSATION_ID + " = c." + DbContract.Conversations.COL_ID
                + " AND m." + DbContract.Messages.COL_IS_READ + "=0 AND m."
                + DbContract.Messages.COL_SENDER_USER_ID + " = c."
                + DbContract.Conversations.COL_CLIENT_USER_ID + ") AS unread_from_client "
                + "FROM " + DbContract.Conversations.TABLE + " c "
                + "LEFT JOIN " + DbContract.Users.TABLE + " u ON c."
                + DbContract.Conversations.COL_CLIENT_USER_ID + " = u." + DbContract.Users.COL_ID
                + " ORDER BY c." + DbContract.Conversations.COL_UPDATED_AT + " DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                Conversation conv = mapConversation(c);
                int nameIdx = c.getColumnIndex("client_name");
                if (nameIdx >= 0) {
                    conv.setClientName(c.getString(nameIdx));
                }
                int emailIdx = c.getColumnIndex("client_email");
                if (emailIdx >= 0) {
                    conv.setClientEmail(c.getString(emailIdx));
                }
                int bodyIdx = c.getColumnIndex("last_body");
                if (bodyIdx >= 0 && !c.isNull(bodyIdx)) {
                    conv.setLastMessagePreview(c.getString(bodyIdx));
                }
                int unreadIdx = c.getColumnIndex("unread_from_client");
                if (unreadIdx >= 0 && !c.isNull(unreadIdx)) {
                    conv.setUnreadCount(c.getInt(unreadIdx));
                }
                list.add(conv);
            }
        }
        return list;
    }

    public Conversation findConversationById(long conversationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Conversations.TABLE, null,
                DbContract.Conversations.COL_ID + "=?",
                new String[]{String.valueOf(conversationId)}, null, null, null)) {
            if (c.moveToFirst()) {
                return mapConversation(c);
            }
        }
        return null;
    }

    public long createConversation(long clientUserId, Long adminUserId, String subject) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Conversations.COL_CLIENT_USER_ID, clientUserId);
        if (adminUserId != null) {
            v.put(DbContract.Conversations.COL_ADMIN_USER_ID, adminUserId);
        }
        v.put(DbContract.Conversations.COL_SUBJECT, subject);
        v.put(DbContract.Conversations.COL_UPDATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Conversations.TABLE, null, v);
    }

    public List<Message> findMessages(long conversationId) {
        List<Message> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT m.*, u." + DbContract.Users.COL_FULL_NAME + " AS sender_name "
                + "FROM " + DbContract.Messages.TABLE + " m "
                + "LEFT JOIN " + DbContract.Users.TABLE + " u ON m."
                + DbContract.Messages.COL_SENDER_USER_ID + " = u." + DbContract.Users.COL_ID
                + " WHERE m." + DbContract.Messages.COL_CONVERSATION_ID + "=?"
                + " ORDER BY m." + DbContract.Messages.COL_CREATED_AT;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(conversationId)})) {
            while (c.moveToNext()) {
                list.add(mapMessage(c));
            }
        }
        return list;
    }

    public long sendMessage(long conversationId, long senderUserId, String body) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = DateUtils.now();
        ContentValues v = new ContentValues();
        v.put(DbContract.Messages.COL_CONVERSATION_ID, conversationId);
        v.put(DbContract.Messages.COL_SENDER_USER_ID, senderUserId);
        v.put(DbContract.Messages.COL_BODY, body);
        v.put(DbContract.Messages.COL_IS_READ, 0);
        v.put(DbContract.Messages.COL_CREATED_AT, now);
        long id = db.insert(DbContract.Messages.TABLE, null, v);

        ContentValues convUpdate = new ContentValues();
        convUpdate.put(DbContract.Conversations.COL_UPDATED_AT, now);
        db.update(DbContract.Conversations.TABLE, convUpdate,
                DbContract.Conversations.COL_ID + "=?", new String[]{String.valueOf(conversationId)});
        return id;
    }

    private Conversation mapConversation(Cursor c) {
        Conversation conv = new Conversation();
        conv.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Conversations.COL_ID)));
        conv.setClientUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Conversations.COL_CLIENT_USER_ID)));
        int adminIdx = c.getColumnIndex(DbContract.Conversations.COL_ADMIN_USER_ID);
        if (adminIdx >= 0 && !c.isNull(adminIdx)) {
            conv.setAdminUserId(c.getLong(adminIdx));
        }
        conv.setSubject(c.getString(c.getColumnIndexOrThrow(DbContract.Conversations.COL_SUBJECT)));
        conv.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Conversations.COL_UPDATED_AT)));
        return conv;
    }

    private Message mapMessage(Cursor c) {
        Message m = new Message();
        m.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Messages.COL_ID)));
        m.setConversationId(c.getLong(c.getColumnIndexOrThrow(DbContract.Messages.COL_CONVERSATION_ID)));
        m.setSenderUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Messages.COL_SENDER_USER_ID)));
        m.setBody(c.getString(c.getColumnIndexOrThrow(DbContract.Messages.COL_BODY)));
        m.setRead(c.getInt(c.getColumnIndexOrThrow(DbContract.Messages.COL_IS_READ)) == 1);
        m.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Messages.COL_CREATED_AT)));
        int nameIdx = c.getColumnIndex("sender_name");
        if (nameIdx >= 0) {
            m.setSenderName(c.getString(nameIdx));
        }
        return m;
    }
}