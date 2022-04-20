package NormalObjects;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@Entity(tableName = "users")
public class User implements Serializable {


    @PrimaryKey(autoGenerate = true)
    private int p_key;
    private String userUID;

    private String name,nickName,lastName,pictureLink;
    private String timeCreated,lastTimeLogIn,activityTime;
    @Ignore
    private ArrayList<String>conversations;
    @Ignore
    private ArrayList<String>blockedUsers;
    private String status;
    @Ignore
    private ArrayList<String>mutedConversations;
    @Ignore
    private ArrayList<String>mutedUsersUID;
    private String phoneNumber;
    @Ignore
    private HashMap<String,String>phoneNumbers;
    @Ignore
    private HashMap<String,String>meetUps;
    private String token;
    private boolean blocked;
    private boolean muted;

    public User() {
        conversations = new ArrayList<>();
        blockedUsers = new ArrayList<>();
        mutedConversations = new ArrayList<>();
        phoneNumbers = new HashMap<>();
        mutedUsersUID = new ArrayList<>();
        blocked = false;
        muted = false;
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
    }

    public int getP_key() {
        return p_key;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPictureLink() {
        return pictureLink;
    }

    public void setPictureLink(String pictureLink) {
        this.pictureLink = pictureLink;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getLastTimeLogIn() {
        return lastTimeLogIn;
    }

    public void setLastTimeLogIn(String lastTimeLogIn) {
        this.lastTimeLogIn = lastTimeLogIn;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    @NonNull
    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(@NonNull String userUID) {
        this.userUID = userUID;
    }

    public ArrayList<String> getConversations() {
        return conversations;
    }

    public void setConversations(ArrayList<String> conversations) {
        this.conversations = conversations;
    }

    public void addConversation(String conversationID){
        conversations.add(conversationID);
    }
    public String getConversationID(int conversationIndex){
        return conversations.get(conversationIndex);
    }

    public void addBlockedUser(String blockedUserUID){
        blockedUsers.add(blockedUserUID);
    }

    public String getBlockedUserUID(int blockedUserIndex){
        return blockedUsers.get(blockedUserIndex);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public ArrayList<String> getMutedConversations() {
        return mutedConversations;
    }

    public void setMutedConversations(ArrayList<String> mutedConversations) {
        this.mutedConversations = mutedConversations;
    }

    public void addMutedConversation(String conversationID){
        mutedConversations.add(conversationID);
    }

    public String getMutedConversation(int index)
    {
        return mutedConversations.get(index);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public HashMap<String, String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(HashMap<String, String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void addRecipientPhoneNumber(String recipientUID,String phoneNumber)
    {
        phoneNumbers.put(recipientUID,phoneNumber);
    }

    public String getRecipientPhoneNumber(String recipientUID)
    {
        return phoneNumbers.get(recipientUID);
    }

    public ArrayList<String> getMutedUsersUID() {
        return mutedUsersUID;
    }

    public void setMutedUsersUID(ArrayList<String> mutedUsersUID) {
        this.mutedUsersUID = mutedUsersUID;
    }

    public void addMutedUser(String mutedUserUID)
    {
        if(mutedUsersUID == null)
            mutedUsersUID = new ArrayList<>();
        mutedUsersUID.add(mutedUserUID);
    }

    public HashMap<String, String> getMeetUps() {
        return meetUps;
    }

    public void setMeetUps(HashMap<String, String> meetUps) {
        this.meetUps = meetUps;
    }
}
