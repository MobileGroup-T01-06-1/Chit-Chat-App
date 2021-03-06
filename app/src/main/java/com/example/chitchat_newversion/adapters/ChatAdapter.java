package com.example.chitchat_newversion.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat_newversion.databinding.ItemContainerReceivedImageBinding;
import com.example.chitchat_newversion.databinding.ItemContainerReceivedMessageBinding;
import com.example.chitchat_newversion.databinding.ItemContainerSentMessageBinding;
import com.example.chitchat_newversion.databinding.ItemContainerSentImageBinding;
import com.example.chitchat_newversion.models.ChatMessage;

import java.math.BigInteger;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    private class VIEW_TYPE{
        public static final int SENT_MESSAGE = 1;
        public static final int SENT_IMAGE = 2;
        public static final int SENT_AUDIO = 3;
        public static final int SENT_LOCATION = 4;
        public static final int RECEIVED_MESSAGE = 5;
        public static final int RECEIVED_IMAGE = 6;
        public static final int RECEIVED_AUDIO = 7;
        public static final int RECEIVED_LOCATION = 8;
    };


    public void setReceiverProfileImage(Bitmap bitmap)
    {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE.SENT_MESSAGE:
                return new SentMessageViewHolder(
                        ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),
                                parent, false)
                );
            case VIEW_TYPE.RECEIVED_MESSAGE:
                return new ReceivedMessageViewHolder(
                        ItemContainerReceivedMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent, false
                        )
                );
            case VIEW_TYPE.SENT_IMAGE:
                return new SentImageViewHolder(
                        ItemContainerSentImageBinding.inflate(LayoutInflater.from(parent.getContext()),
                                parent, false
                        )
                );
            case VIEW_TYPE.RECEIVED_IMAGE:
                return new ReceivedImageViewHolder(
                        ItemContainerReceivedImageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent, false
                        )
                );
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch(getItemViewType(position))
        {   case VIEW_TYPE.SENT_MESSAGE:
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
            break;
            case VIEW_TYPE.RECEIVED_MESSAGE:
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
                break;
            case VIEW_TYPE.SENT_IMAGE:
                ((SentImageViewHolder) holder).setData(chatMessages.get(position));
                break;
            case VIEW_TYPE.RECEIVED_IMAGE:
                ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            switch (chatMessages.get(position).message_type) {
                case 1:
                    return VIEW_TYPE.SENT_MESSAGE;
                case 2:
                    return VIEW_TYPE.SENT_IMAGE;
                case 3:
                    return VIEW_TYPE.SENT_AUDIO;
                case 4:
                    return VIEW_TYPE.SENT_LOCATION;
            }
        } else {
            switch (chatMessages.get(position).message_type) {
                case 1:
                    return VIEW_TYPE.RECEIVED_MESSAGE;
                case 2:
                    return VIEW_TYPE.RECEIVED_IMAGE;
                case 3:
                    return VIEW_TYPE.RECEIVED_AUDIO;
                case 4:
                    return VIEW_TYPE.RECEIVED_LOCATION;
            }
        }
        return 0;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding)
        {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding)
        {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage != null)
            {
                binding.userImage.setImageBitmap(receiverProfileImage);
            }
        }
    }

    static class SentImageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerSentImageBinding binding;

        SentImageViewHolder(ItemContainerSentImageBinding itemContainerSentImageBinding)
        {
            super(itemContainerSentImageBinding.getRoot());
            binding = itemContainerSentImageBinding;
        }
        void setData(ChatMessage chatMessage)
        {
            binding.textImage.setImageBitmap(getBitmapFromEncodedString(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
        }
        private Bitmap getBitmapFromEncodedString(String encodedImage)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedImageBinding binding;

        ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedImageBinding)
        {
            super(itemContainerReceivedImageBinding.getRoot());
            binding = itemContainerReceivedImageBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage)
        {
            binding.textImage.setImageBitmap(getBitmapFromEncodedString(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.userImage.setImageBitmap(receiverProfileImage);



        }
        private Bitmap getBitmapFromEncodedString(String encodedImage)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }


}
