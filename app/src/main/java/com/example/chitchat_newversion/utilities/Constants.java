package com.example.chitchat_newversion.utilities;

import java.util.HashMap;

public class Constants {

        // firestore database new collection "users"
        public static final String KEY_COLLECTION_USERS = "users";

        public static final String KEY_NAME = "name";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_EMAIL = "email";
        public static final String KEY_IMAGE = "image";
        public static final String KEY_FCM_TOKEN = "fcmToken";
        public static final String KEY_SQUESTION = "safetyQuestion";
        public static final String KEY_SANSWER = "safetyAnswer";


        public static final String KEY_PREFERENCE_NAME = "preference";
        // verify successfully login or not
        public static final String KEY_IS_LOGINED = "isLogin";
        // user id
        public static final String KEY_USER_ID = "userID";
        public static final String KEY_USER = "user";

        // firestore database new collection "chat"
        public static final String KEY_COLLECTION_CHAT = "chat";

        public static final String KEY_SENDER_ID = "senderId";
        public static final String KEY_RECEIVED_ID = "receivedId";
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_TIMESTAMP = "timestamp";
        public static final String KEY_MESSAGE_IMAGE = "message_image";

        public static final String KEY_COLLECTION_CONVERSATION = "conversations";
        public static final String KEY_SENDER_NAME = "senderName";
        public static final String KEY_RECEIVER_NAME = "receiverName";
        public static final String KEY_SENDER_IMAGE = "senderImage";
        public static final String KEY_RECEIVER_IMAGE = "receiverImage";
        public static final String KEY_LAST_MESSAGE = "lastMessage";


        public static final String KEY_AVAILABILITY = "availability";

        public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
        public static final String REMOTE_MSG_TYPE = "Content-Type";
        public static final String REMOTE_MSG_DATA = "data";
        public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

        public static HashMap<String, String> remoteMsgHeaders = null;
        public static HashMap<String, String> getRemoteMsgHeaders(){
                if(remoteMsgHeaders == null)
                {
                        remoteMsgHeaders = new HashMap<>();
                        remoteMsgHeaders.put(
                                REMOTE_MSG_AUTHORIZATION,
                                "key=AAAAjgpZYVo:APA91bEz7368__M3cz4K78dnXJiZNqP6TMkK38cLz94VpmLJyY2RgrgSHv1U1JCLJwWcmEvs8WxhDlLbyvn3X0-DBTTng5uricB2cXbkWndKapQDz7s_PrUr592WdBfFaxXBInADFSEw");
                        remoteMsgHeaders.put(
                                REMOTE_MSG_TYPE,
                                "application/json"

                        );
                }
                return remoteMsgHeaders;
        }

}
