package com.example.chitchat_newversion.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat_newversion.databinding.ItemContainerRecentConversationBinding;
import com.example.chitchat_newversion.listeners.ConversationListener;
import com.example.chitchat_newversion.models.ChatMessage;
import com.example.chitchat_newversion.models.Users;

import java.util.List;

public class RecentConversationsAadpter extends RecyclerView.Adapter<RecentConversationsAadpter.ConversionViewHolder>{


    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationsAadpter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder
    {

        ItemContainerRecentConversationBinding binding;
        ConversionViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.userImage.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                Users users = new Users();
                users.id = chatMessage.conversionId;
                users.name  = chatMessage.conversionName;
                users.image  = chatMessage.conversionImage;
                conversationListener.onConversionClicked(users);
            });
        }
    }



    private Bitmap getConversionImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
}
